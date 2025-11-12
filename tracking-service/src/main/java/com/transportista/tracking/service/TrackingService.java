package com.transportista.tracking.service;

import com.transportista.tracking.dto.TrackingEventoDTO;
import com.transportista.tracking.entity.TrackingEvento;
import com.transportista.tracking.repository.TrackingEventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class TrackingService {

    @Autowired
    private TrackingEventoRepository trackingEventoRepository;

    @Autowired
    private WebClient.Builder webClientBuilder;

    public TrackingEventoDTO registrarEvento(Long contenedorId, Long solicitudId, String estado, String ubicacion, String descripcion) {
        System.out.println("📍 Registrando evento de tracking - ContenedorId: " + contenedorId + ", SolicitudId: " + solicitudId + ", Estado: " + estado);

        TrackingEvento evento = new TrackingEvento();
        evento.setContenedorId(contenedorId);
        evento.setSolicitudId(solicitudId);
        evento.setEstado(estado);
        evento.setUbicacion(ubicacion);
        evento.setDescripcion(descripcion);
        evento.setFechaEvento(LocalDateTime.now());

        TrackingEvento saved = trackingEventoRepository.save(evento);
        System.out.println("✅ Evento de tracking registrado exitosamente - ID: " + saved.getId());
        return toDTO(saved);
    }

    public List<TrackingEventoDTO> obtenerHistorialContenedor(Long contenedorId) {
        return trackingEventoRepository.findByContenedorIdOrderByFechaEventoDesc(contenedorId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<TrackingEventoDTO> obtenerHistorialSolicitud(Long solicitudId) {
        return trackingEventoRepository.findBySolicitudIdOrderByFechaEventoDesc(solicitudId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    /**
     * Inicializa eventos de tracking para solicitudes existentes que no tienen eventos
     */
    @SuppressWarnings("unchecked")
    public int inicializarEventosExistentes() {
        try {
            // Obtener todas las solicitudes desde solicitudes-service
            List<?> rawList = webClientBuilder.build()
                .get()
                .uri("http://solicitudes-service:8081/solicitudes")
                .retrieve()
                .bodyToFlux(Map.class)
                .collectList()
                .block();

            if (rawList == null || rawList.isEmpty()) {
                return 0;
            }

            List<Map<String, Object>> solicitudes = (List<Map<String, Object>>) rawList;
            int eventosCreados = 0;

            for (Map<String, Object> solicitud : solicitudes) {
                Long solicitudId = ((Number) solicitud.get("id")).longValue();
                Long contenedorId = ((Number) solicitud.get("contenedorId")).longValue();
                String estado = (String) solicitud.get("estado");
                String direccionOrigen = (String) solicitud.get("direccionOrigen");
                String direccionDestino = (String) solicitud.get("direccionDestino");

                // Verificar si ya existe un evento para este contenedor
                List<TrackingEvento> eventosExistentes = trackingEventoRepository
                    .findByContenedorIdOrderByFechaEventoDesc(contenedorId);

                if (eventosExistentes.isEmpty()) {
                    // Crear evento inicial PENDIENTE
                    TrackingEvento eventoPendiente = new TrackingEvento();
                    eventoPendiente.setContenedorId(contenedorId);
                    eventoPendiente.setSolicitudId(solicitudId);
                    eventoPendiente.setEstado("PENDIENTE");
                    eventoPendiente.setUbicacion(direccionOrigen);
                    eventoPendiente.setDescripcion("Solicitud de transporte creada - Contenedor registrado");
                    eventoPendiente.setFechaEvento(LocalDateTime.now().minusHours(2)); // 2 horas atrás
                    trackingEventoRepository.save(eventoPendiente);
                    eventosCreados++;

                    // Si el estado actual no es PENDIENTE, crear evento para el estado actual
                    if (!"PENDIENTE".equals(estado)) {
                        TrackingEvento eventoActual = new TrackingEvento();
                        eventoActual.setContenedorId(contenedorId);
                        eventoActual.setSolicitudId(solicitudId);
                        eventoActual.setEstado(estado);
                        eventoActual.setUbicacion(obtenerUbicacionPorEstado(estado, direccionOrigen, direccionDestino));
                        eventoActual.setDescripcion(obtenerDescripcionPorEstado(estado, solicitud));
                        eventoActual.setFechaEvento(LocalDateTime.now().minusMinutes(30)); // 30 min atrás
                        trackingEventoRepository.save(eventoActual);
                        eventosCreados++;
                    }
                }
            }

            return eventosCreados;
        } catch (Exception e) {
            System.err.println("Error al inicializar eventos: " + e.getMessage());
            return 0;
        }
    }

    private String obtenerUbicacionPorEstado(String estado, String origen, String destino) {
        switch (estado.toUpperCase()) {
            case "PENDIENTE":
                return origen;
            case "RUTA_ASIGNADA":
                return "Ruta planificada - " + origen;
            case "EN_TRANSITO":
                return "En ruta - " + origen + " → " + destino;
            case "EN_DEPOSITO":
                return "Depósito intermedio";
            case "ENTREGADA":
            case "COMPLETADA":
                return destino;
            default:
                return origen;
        }
    }

    private String obtenerDescripcionPorEstado(String estado, Map<String, Object> solicitud) {
        switch (estado.toUpperCase()) {
            case "RUTA_ASIGNADA":
                Object tiempoEstimado = solicitud.get("tiempoEstimadoHoras");
                Object costoEstimado = solicitud.get("costoEstimado");
                return "Ruta asignada - Tiempo estimado: " +
                       (tiempoEstimado != null ? tiempoEstimado : "0") + "h - Costo estimado: $" +
                       (costoEstimado != null ? costoEstimado : "0");
            case "EN_TRANSITO":
                return "Contenedor en tránsito hacia destino";
            case "EN_DEPOSITO":
                return "Contenedor en depósito intermedio";
            case "ENTREGADA":
                return "Contenedor entregado en destino";
            case "COMPLETADA":
                return "Solicitud completada exitosamente";
            default:
                return "Estado cambiado a " + estado;
        }
    }

    private TrackingEventoDTO toDTO(TrackingEvento evento) {
        TrackingEventoDTO dto = new TrackingEventoDTO();
        dto.setId(evento.getId());
        dto.setContenedorId(evento.getContenedorId());
        dto.setSolicitudId(evento.getSolicitudId());
        dto.setEstado(evento.getEstado());
        dto.setUbicacion(evento.getUbicacion());
        dto.setDescripcion(evento.getDescripcion());
        dto.setFechaEvento(evento.getFechaEvento());
        return dto;
    }
}
