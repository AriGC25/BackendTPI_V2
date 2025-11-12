package com.transportista.solicitudes.service;

import com.transportista.solicitudes.dto.TramoDTO;
import com.transportista.solicitudes.dto.TramoRequestDTO;
import com.transportista.solicitudes.entity.*;
import com.transportista.solicitudes.exception.BusinessException;
import com.transportista.solicitudes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TramoService {

    private static final Logger log = LoggerFactory.getLogger(TramoService.class);

    @Autowired
    private TramoRepository tramoRepository;

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    /**
     * Crear un tramo a partir de TramoRequestDTO. Sólo se mapean los campos existentes en la entidad Tramo.
     */
    @Transactional
    public TramoDTO crearTramo(TramoRequestDTO dto) {
        Ruta ruta = rutaRepository.findById(dto.getRutaId())
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada con ID: " + dto.getRutaId()));

        Tramo tramo = new Tramo();
        tramo.setTipoTramo(dto.getTipo());
        tramo.setOrdenTramo(dto.getNumeroOrden());
        tramo.setDireccionOrigen(dto.getDireccionOrigen());
        tramo.setLatitudOrigen(dto.getLatitudOrigen());
        tramo.setLongitudOrigen(dto.getLongitudOrigen());
        tramo.setDireccionDestino(dto.getDireccionDestino());
        tramo.setLatitudDestino(dto.getLatitudDestino());
        tramo.setLongitudDestino(dto.getLongitudDestino());
        tramo.setDistanciaKm(dto.getDistanciaKm());
        tramo.setCostoAproximado(dto.getCostoAproximado());
        tramo.setCostoReal(dto.getCostoReal());
        tramo.setEstado("estimado"); // Estado inicial: estimado

        // Asociar tramo a la ruta usando el helper para mantener consistencia
        ruta.addTramo(tramo);

        Tramo saved = tramoRepository.save(tramo);
        rutaRepository.save(ruta);

        return convertirADTO(saved);
    }

    @Transactional
    public TramoDTO asignarCamion(Long tramoId, Long camionId, String transportistaId) {
        log.info("Iniciando asignación de camión {} al tramo {} por transportista {}", camionId, tramoId, transportistaId);

        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new IllegalArgumentException("Tramo no encontrado"));

        // Solo se pueden asignar camiones a tramos en estado ESTIMADO
        if (!"estimado".equals(tramo.getEstado())) {
            log.error("Intento de asignar camión a tramo en estado inválido. Tramo ID: {}, Estado actual: {}", tramoId, tramo.getEstado());
            throw new IllegalStateException(
                String.format("No se puede asignar camión al tramo. Estado actual: '%s'. Solo se pueden asignar camiones a tramos en estado 'estimado'.",
                    tramo.getEstado())
            );
        }

        // VALIDACIÓN OBLIGATORIA: Verificar capacidad del camión
        Solicitud solicitud = solicitudRepository.findById(tramo.getRuta().getSolicitud().getId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        Contenedor contenedor = solicitud.getContenedor();

        validarCapacidadCamion(camionId, contenedor.getPeso(), contenedor.getVolumen());

        // Asignar
        tramo.setCamionId(camionId);
        tramo.setTransportistaId(transportistaId);
        tramo.setEstado("asignado");

        tramo = tramoRepository.save(tramo);

        log.info("Camión {} asignado exitosamente al tramo {}", camionId, tramoId);

        return convertirADTO(tramo);
    }

    /**
     * VALIDACIÓN OBLIGATORIA según RF-11: Un camión no puede transportar contenedores que superen su peso o volumen máximo
     */
    private void validarCapacidadCamion(Long camionId, BigDecimal pesoContenedor, BigDecimal volumenContenedor) {
        log.info("Iniciando validación de capacidad para camión ID: {} - Peso contenedor: {} kg, Volumen contenedor: {} m³",
                 camionId, pesoContenedor, volumenContenedor);

        try {
            // 1. Obtener el token del contexto de seguridad
            JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
            String token = authentication.getToken().getTokenValue();
            String url = "http://logistica-service:8082/camiones/" + camionId;

            log.debug("Consultando capacidad del camión en: {}", url);

            var camion = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> {
                                log.error("Error al consultar camión ID {}: {}", camionId, response.statusCode());
                                return response.bodyToMono(String.class).map(body ->
                                    new BusinessException("Error al consultar camión ID " + camionId + ": " + response.statusCode())
                                );
                            })
                    .bodyToMono(CamionCapacidad.class)
                    .block();

            if (camion == null) {
                log.error("Camión no encontrado con ID: {}", camionId);
                throw new BusinessException("Camión no encontrado con ID: " + camionId);
            }

            log.info("Capacidad del camión ID {}: Peso máximo: {} kg, Volumen máximo: {} m³",
                     camionId, camion.getCapacidadPeso(), camion.getCapacidadVolumen());

            // Validar peso
            if (pesoContenedor.compareTo(camion.getCapacidadPeso()) > 0) {
                BigDecimal exceso = pesoContenedor.subtract(camion.getCapacidadPeso());
                log.error("VALIDACIÓN FALLIDA - Peso excedido. Camión ID: {}, Capacidad: {} kg, Contenedor: {} kg, Exceso: {} kg",
                         camionId, camion.getCapacidadPeso(), pesoContenedor, exceso);
                throw new BusinessException(
                        String.format("El peso del contenedor (%.2f kg) excede la capacidad máxima del camión (%.2f kg). Exceso: %.2f kg",
                                pesoContenedor, camion.getCapacidadPeso(), exceso)
                );
            }

            // Validar volumen
            if (volumenContenedor.compareTo(camion.getCapacidadVolumen()) > 0) {
                BigDecimal exceso = volumenContenedor.subtract(camion.getCapacidadVolumen());
                log.error("VALIDACIÓN FALLIDA - Volumen excedido. Camión ID: {}, Capacidad: {} m³, Contenedor: {} m³, Exceso: {} m³",
                         camionId, camion.getCapacidadVolumen(), volumenContenedor, exceso);
                throw new BusinessException(
                        String.format("El volumen del contenedor (%.2f m³) excede la capacidad máxima del camión (%.2f m³). Exceso: %.2f m³",
                                volumenContenedor, camion.getCapacidadVolumen(), exceso)
                );
            }

            log.info("✓ VALIDACIÓN EXITOSA - El camión ID: {} tiene capacidad suficiente para transportar el contenedor", camionId);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error inesperado al validar capacidad del camión ID {}: {}", camionId, e.getMessage(), e);
            throw new BusinessException("Error al validar capacidad del camión: " + e.getMessage(), e);
        }
    }

    /**
     * Marca el inicio del tramo. Sólo se puede iniciar si está en estado ASIGNADO.
     */
    @Transactional
    public TramoDTO iniciarTramo(Long tramoId) {
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + tramoId));

        if (!"asignado".equals(tramo.getEstado())) {
            throw new RuntimeException("Solo se pueden iniciar tramos en estado asignado");
        }

        tramo.setEstado("iniciado");
        tramo.setFechaInicio(LocalDateTime.now());
        tramo.setFechaActualizacion(LocalDateTime.now());

        Tramo updated = tramoRepository.save(tramo);
        return convertirADTO(updated);
    }

    /**
     * Marca la finalización del tramo. Sólo se puede finalizar si está en estado iniciado.
     * Cuando todos los tramos de la ruta están completados, actualiza la solicitud.
     */
    @Transactional
    public TramoDTO finalizarTramo(Long tramoId) {
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + tramoId));

        if (!"iniciado".equals(tramo.getEstado())) {
            throw new RuntimeException("Solo se pueden finalizar tramos en estado iniciado");
        }

        tramo.setEstado("finalizado");
        tramo.setFechaFin(LocalDateTime.now());
        tramo.setFechaActualizacion(LocalDateTime.now());

        Tramo updated = tramoRepository.save(tramo);

        // Verificar si todos los tramos de la ruta están completados
        verificarYCompletarSolicitud(tramo.getRuta());

        return convertirADTO(updated);
    }

    /**
     * Verifica si todos los tramos de una ruta están completados.
     * Si es así, marca la solicitud como COMPLETADA y calcula tiempoRealHoras y costoTotal.
     */
    private void verificarYCompletarSolicitud(Ruta ruta) {
        List<Tramo> tramos = ruta.getTramos();

        // Verificar si todos los tramos están completados
        boolean todosCompletados = tramos.stream()
                .allMatch(t -> "finalizado".equals(t.getEstado()));

        if (todosCompletados) {
            Solicitud solicitud = ruta.getSolicitud();

            // Actualizar estado a COMPLETADA
            solicitud.setEstado("COMPLETADA");
            solicitud.setFechaActualizacion(LocalDateTime.now());

            // Calcular tiempo real en horas
            if (solicitud.getFechaSolicitud() != null) {
                java.time.Duration duracion = java.time.Duration.between(
                    solicitud.getFechaSolicitud(),
                    LocalDateTime.now()
                );
                BigDecimal tiempoRealHoras = BigDecimal.valueOf(duracion.toMinutes())
                    .divide(BigDecimal.valueOf(60), 2, java.math.RoundingMode.HALF_UP);
                solicitud.setTiempoRealHoras(tiempoRealHoras);
            }

            // Usar costoEstimado como costoTotal si no está establecido
            if (solicitud.getCostoTotal() == null && solicitud.getCostoEstimado() != null) {
                solicitud.setCostoTotal(solicitud.getCostoEstimado());
            }

            solicitudRepository.save(solicitud);

            log.info("Solicitud ID {} marcada como COMPLETADA. Tiempo real: {} horas, Costo total: {}",
                     solicitud.getId(), solicitud.getTiempoRealHoras(), solicitud.getCostoTotal());
        }
    }

    public List<TramoDTO> listarTramosPorTransportista(String transportistaId) {
        List<Tramo> tramos = tramoRepository.findByTransportistaId(transportistaId);
        return tramos.stream().map(this::convertirADTO).collect(Collectors.toList());
    }

    public List<TramoDTO> listarTodos() {
        return tramoRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public TramoDTO obtenerPorId(Long id) {
        Tramo tramo = tramoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tramo no encontrado"));
        return convertirADTO(tramo);
    }

    private TramoDTO convertirADTO(Tramo tramo) {
        TramoDTO dto = new TramoDTO();
        dto.setId(tramo.getId());
        dto.setRutaId(tramo.getRuta() != null ? tramo.getRuta().getId() : null);
        dto.setTipoTramo(tramo.getTipoTramo());
        dto.setOrdenTramo(tramo.getOrdenTramo());
        dto.setEstado(tramo.getEstado());
        dto.setDireccionOrigen(tramo.getDireccionOrigen());
        dto.setLatitudOrigen(tramo.getLatitudOrigen());
        dto.setLongitudOrigen(tramo.getLongitudOrigen());
        dto.setDireccionDestino(tramo.getDireccionDestino());
        dto.setLatitudDestino(tramo.getLatitudDestino());
        dto.setLongitudDestino(tramo.getLongitudDestino());
        dto.setDistanciaKm(tramo.getDistanciaKm());
        dto.setTiempoEstimadoMinutos(tramo.getTiempoEstimadoMinutos());
        dto.setCostoAproximado(tramo.getCostoAproximado());
        dto.setCostoReal(tramo.getCostoReal());
        dto.setDiasEstadiaDeposito(tramo.getDiasEstadiaDeposito());
        dto.setCamionId(tramo.getCamionId());
        dto.setTransportistaId(tramo.getTransportistaId());
        dto.setFechaInicio(tramo.getFechaInicio());
        dto.setFechaFin(tramo.getFechaFin());
        dto.setFechaCreacion(tramo.getFechaCreacion());
        dto.setFechaActualizacion(tramo.getFechaActualizacion());
        return dto;
    }

    static class CamionCapacidad {
        private BigDecimal capacidadPeso;
        private BigDecimal capacidadVolumen;

        public BigDecimal getCapacidadPeso() { return capacidadPeso; }
        public void setCapacidadPeso(BigDecimal capacidadPeso) { this.capacidadPeso = capacidadPeso; }
        public BigDecimal getCapacidadVolumen() { return capacidadVolumen; }
        public void setCapacidadVolumen(BigDecimal capacidadVolumen) { this.capacidadVolumen = capacidadVolumen; }
    }
}
