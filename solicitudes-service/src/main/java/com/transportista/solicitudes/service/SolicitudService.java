package com.transportista.solicitudes.service;

import com.transportista.solicitudes.dto.SolicitudRequestDTO;
import com.transportista.solicitudes.dto.SolicitudResponseDTO;
import com.transportista.solicitudes.dto.SeguimientoEstadoDTO;
import com.transportista.solicitudes.entity.Contenedor;
import com.transportista.solicitudes.entity.Solicitud;
import com.transportista.solicitudes.entity.SeguimientoEstado;
import com.transportista.solicitudes.repository.ContenedorRepository;
import com.transportista.solicitudes.repository.SolicitudRepository;
import com.transportista.solicitudes.repository.SeguimientoEstadoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class SolicitudService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private ContenedorRepository contenedorRepository;

    @Autowired
    private SeguimientoEstadoRepository seguimientoEstadoRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    public SolicitudResponseDTO crearSolicitud(SolicitudRequestDTO dto) {
        // Crear contenedor
        Contenedor contenedor = new Contenedor();
        contenedor.setPeso(dto.getPesoContenedor());
        contenedor.setVolumen(dto.getVolumenContenedor());
        contenedor.setDescripcion(dto.getDescripcionContenedor());
        contenedor.setClienteId(dto.getClienteId());
        contenedor.setEstado("PENDIENTE");
        contenedor = contenedorRepository.save(contenedor);

        // Crear solicitud
        Solicitud solicitud = new Solicitud();
        // Generar número de solicitud único
        String numero = "SOL-" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        solicitud.setNumeroSolicitud(numero);
        solicitud.setContenedor(contenedor);
        solicitud.setClienteId(dto.getClienteId());
        solicitud.setDireccionOrigen(dto.getDireccionOrigen());
        solicitud.setLatitudOrigen(dto.getLatitudOrigen());
        solicitud.setLongitudOrigen(dto.getLongitudOrigen());
        solicitud.setDireccionDestino(dto.getDireccionDestino());
        solicitud.setLatitudDestino(dto.getLatitudDestino());
        solicitud.setLongitudDestino(dto.getLongitudDestino());
        solicitud.setEstado("PENDIENTE");

        solicitud = solicitudRepository.save(solicitud);

        // Registrar el estado inicial en el seguimiento
        registrarEstado(solicitud, "PENDIENTE", "Solicitud creada");

        // Registrar evento en tracking-service
        registrarEventoTracking(
            contenedor.getId(),
            solicitud.getId(),
            "PENDIENTE",
            dto.getDireccionOrigen(),
            "Solicitud de transporte creada - Contenedor registrado"
        );

        return toResponseDTO(solicitud);
    }

    public SolicitudResponseDTO obtenerSolicitud(Long id) {
        Solicitud solicitud = solicitudRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        return toResponseDTO(solicitud);
    }

    public SolicitudResponseDTO obtenerSolicitudPorNumero(String numeroSolicitud) {
        Solicitud solicitud = solicitudRepository.findByNumeroSolicitud(numeroSolicitud)
            .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
        return toResponseDTO(solicitud);
    }

    public List<SolicitudResponseDTO> listarSolicitudes() {
        return solicitudRepository.findAll().stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    public List<SolicitudResponseDTO> listarSolicitudesPorCliente(Long clienteId) {
        return solicitudRepository.findByClienteId(clienteId).stream()
            .map(this::toResponseDTO)
            .collect(Collectors.toList());
    }

    public SolicitudResponseDTO actualizarEstado(Long id, String estado) {
        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + id));

        String estadoAnterior = solicitud.getEstado();
        solicitud.setEstado(estado);
        solicitud.setFechaActualizacion(java.time.LocalDateTime.now());

        // Si la solicitud se marca como COMPLETADA, calcular el tiempo real
        if ("COMPLETADA".equalsIgnoreCase(estado) && solicitud.getFechaSolicitud() != null) {
            java.time.Duration duracion = java.time.Duration.between(
                solicitud.getFechaSolicitud(),
                java.time.LocalDateTime.now()
            );
            BigDecimal tiempoRealHoras = BigDecimal.valueOf(duracion.toMinutes())
                .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
            solicitud.setTiempoRealHoras(tiempoRealHoras);
        }

        Solicitud solicitudActualizada = solicitudRepository.save(solicitud);

        // Registrar el cambio de estado en el seguimiento
        String descripcion = "Estado cambiado de " + estadoAnterior + " a " + estado;
        registrarEstado(solicitudActualizada, estado, descripcion);

        // Registrar evento en tracking-service
        String ubicacion = obtenerUbicacionSegunEstado(solicitudActualizada, estado);
        registrarEventoTracking(
            solicitudActualizada.getContenedor().getId(),
            solicitudActualizada.getId(),
            estado,
            ubicacion,
            descripcion
        );

        return toResponseDTO(solicitudActualizada);
    }

    public List<SolicitudResponseDTO> listarSolicitudesPorEstado(String estado) {
        String normalized = estado == null ? "" : estado.trim().toUpperCase();
        List<Solicitud> solicitudes = solicitudRepository.findByEstado(normalized);
        return solicitudes.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtiene el seguimiento de estados de una solicitud en orden cronológico.
     * Cumple con la regla de negocio: "El seguimiento debe mostrar los estados del envío en orden cronológico"
     */
    public List<SeguimientoEstadoDTO> obtenerSeguimiento(Long solicitudId) {
        // Verificar que la solicitud existe
        solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + solicitudId));

        // Obtener seguimiento ordenado cronológicamente (del más antiguo al más reciente)
        return seguimientoEstadoRepository.findBySolicitudId(solicitudId)
                .stream()
                .sorted(Comparator.comparing(SeguimientoEstado::getFechaRegistro))
                .map(this::toSeguimientoDTO)
                .collect(Collectors.toList());
    }

    /**
     * Registra un cambio de estado en el historial de seguimiento
     */
    private void registrarEstado(Solicitud solicitud, String estado, String descripcion) {
        SeguimientoEstado seguimiento = new SeguimientoEstado();
        seguimiento.setSolicitud(solicitud);
        seguimiento.setEstado(estado);
        seguimiento.setDescripcion(descripcion);
        seguimiento.setFechaRegistro(java.time.LocalDateTime.now());
        seguimientoEstadoRepository.save(seguimiento);
    }

    private SeguimientoEstadoDTO toSeguimientoDTO(SeguimientoEstado seguimiento) {
        SeguimientoEstadoDTO dto = new SeguimientoEstadoDTO();
        dto.setId(seguimiento.getId());
        dto.setSolicitudId(seguimiento.getSolicitud().getId());
        dto.setEstado(seguimiento.getEstado());
        dto.setDescripcion(seguimiento.getDescripcion());
        dto.setFechaRegistro(seguimiento.getFechaRegistro());
        dto.setUsuarioRegistro(seguimiento.getUsuarioRegistro());
        return dto;
    }

    private SolicitudResponseDTO toResponseDTO(Solicitud solicitud) {
        SolicitudResponseDTO dto = new SolicitudResponseDTO();
        dto.setId(solicitud.getId());
        dto.setNumeroSolicitud(solicitud.getNumeroSolicitud());
        dto.setContenedorId(solicitud.getContenedor().getId());
        dto.setClienteId(solicitud.getClienteId());
        dto.setDireccionOrigen(solicitud.getDireccionOrigen());
        dto.setLatitudOrigen(solicitud.getLatitudOrigen());
        dto.setLongitudOrigen(solicitud.getLongitudOrigen());
        dto.setDireccionDestino(solicitud.getDireccionDestino());
        dto.setLatitudDestino(solicitud.getLatitudDestino());
        dto.setLongitudDestino(solicitud.getLongitudDestino());
        dto.setEstado(solicitud.getEstado());
        dto.setCostoEstimado(solicitud.getCostoEstimado());
        dto.setCostoTotal(solicitud.getCostoTotal());
        dto.setTiempoEstimadoHoras(solicitud.getTiempoEstimadoHoras());
        dto.setTiempoRealHoras(solicitud.getTiempoRealHoras());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud());
        dto.setFechaEstimadaEntrega(solicitud.getFechaEstimadaEntrega());
        dto.setFechaCreacion(solicitud.getFechaCreacion());
        dto.setFechaActualizacion(solicitud.getFechaActualizacion());
        return dto;
    }

    /**
     * Registra un evento en el tracking-service
     */
    private void registrarEventoTracking(Long contenedorId, Long solicitudId, String estado, String ubicacion, String descripcion) {
        try {
            System.out.println("🔔 Intentando registrar evento de tracking - ContenedorId: " + contenedorId + ", Estado: " + estado);

            Map<String, Object> eventoData = Map.of(
                "contenedorId", contenedorId,
                "solicitudId", solicitudId,
                "estado", estado,
                "ubicacion", ubicacion != null ? ubicacion : "Sin ubicación",
                "descripcion", descripcion
            );

            webClientBuilder.build()
                .post()
                .uri("http://tracking-service:8084/tracking/registrar")
                .bodyValue(eventoData)
                .retrieve()
                .bodyToMono(Void.class)
                .block();

            System.out.println("✅ Evento de tracking registrado correctamente");
        } catch (Exception e) {
            // Log error pero no fallar la operación principal
            System.err.println("❌ Error al registrar evento de tracking: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene la ubicación según el estado de la solicitud
     */
    private String obtenerUbicacionSegunEstado(Solicitud solicitud, String estado) {
        switch (estado.toUpperCase()) {
            case "PENDIENTE":
                return solicitud.getDireccionOrigen();
            case "RUTA_ASIGNADA":
                return "Ruta planificada - " + solicitud.getDireccionOrigen();
            case "EN_TRANSITO":
                return "En ruta - " + solicitud.getDireccionOrigen() + " → " + solicitud.getDireccionDestino();
            case "EN_DEPOSITO":
                return "Depósito intermedio";
            case "ENTREGADA":
            case "COMPLETADA":
                return solicitud.getDireccionDestino();
            default:
                return solicitud.getDireccionOrigen();
        }
    }
}
