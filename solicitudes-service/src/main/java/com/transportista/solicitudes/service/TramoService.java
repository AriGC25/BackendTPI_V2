package com.transportista.solicitudes.service;

import com.transportista.solicitudes.dto.TramoDTO;
import com.transportista.solicitudes.dto.TramoRequestDTO;
import com.transportista.solicitudes.entity.*;
import com.transportista.solicitudes.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TramoService {

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
        tramo.setEstado("PENDIENTE");

        // Asociar tramo a la ruta usando el helper para mantener consistencia
        ruta.addTramo(tramo);

        Tramo saved = tramoRepository.save(tramo);
        rutaRepository.save(ruta);

        return convertirADTO(saved);
    }

    @Transactional
    public TramoDTO asignarCamion(Long tramoId, Long camionId, String transportistaId) {
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new IllegalArgumentException("Tramo no encontrado"));

        if (!"PENDIENTE".equals(tramo.getEstado())) {
            throw new IllegalStateException("El tramo no está en estado PENDIENTE");
        }

        // VALIDACIÓN OBLIGATORIA: Verificar capacidad del camión
        Solicitud solicitud = solicitudRepository.findById(tramo.getRuta().getSolicitud().getId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        Contenedor contenedor = solicitud.getContenedor();

        validarCapacidadCamion(camionId, contenedor.getPeso(), contenedor.getVolumen());

        // Asignar
        tramo.setCamionId(camionId);
        tramo.setTransportistaId(transportistaId);
        tramo.setEstado("ASIGNADO");

        tramo = tramoRepository.save(tramo);
        return convertirADTO(tramo);
    }

    /**
     * VALIDACIÓN OBLIGATORIA según RF-11
     */
    private void validarCapacidadCamion(Long camionId, BigDecimal pesoContenedor, BigDecimal volumenContenedor) {
        try {
            String url = "http://logistica-service:8082/camiones/" + camionId;

            var camion = webClientBuilder.build()
                    .get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(CamionCapacidad.class)
                    .block();

            if (camion == null) {
                throw new IllegalArgumentException("Camión no encontrado");
            }

            if (pesoContenedor.compareTo(camion.getCapacidadPeso()) > 0) {
                throw new IllegalArgumentException(
                        String.format("El camión no soporta el peso del contenedor. Capacidad: %s kg, Contenedor: %s kg",
                                camion.getCapacidadPeso(), pesoContenedor)
                );
            }

            if (volumenContenedor.compareTo(camion.getCapacidadVolumen()) > 0) {
                throw new IllegalArgumentException(
                        String.format("El camión no soporta el volumen del contenedor. Capacidad: %s m³, Contenedor: %s m³",
                                camion.getCapacidadVolumen(), volumenContenedor)
                );
            }

        } catch (Exception e) {
            throw new IllegalStateException("Error al validar capacidad del camión: " + e.getMessage());
        }
    }

    /**
     * Marca el inicio del tramo. Sólo se puede iniciar si está en estado ASIGNADO.
     */
    @Transactional
    public TramoDTO iniciarTramo(Long tramoId) {
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + tramoId));

        if (!"ASIGNADO".equals(tramo.getEstado())) {
            throw new RuntimeException("Solo se pueden iniciar tramos en estado ASIGNADO");
        }

        tramo.setEstado("EN_CURSO");
        tramo.setFechaInicio(LocalDateTime.now());
        tramo.setFechaActualizacion(LocalDateTime.now());

        Tramo updated = tramoRepository.save(tramo);
        return convertirADTO(updated);
    }

    /**
     * Marca la finalización del tramo. Sólo se puede finalizar si está en estado EN_CURSO.
     */
    @Transactional
    public TramoDTO finalizarTramo(Long tramoId) {
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + tramoId));

        if (!"EN_CURSO".equals(tramo.getEstado())) {
            throw new RuntimeException("Solo se pueden finalizar tramos en estado EN_CURSO");
        }

        tramo.setEstado("COMPLETADO");
        tramo.setFechaFin(LocalDateTime.now());
        tramo.setFechaActualizacion(LocalDateTime.now());

        Tramo updated = tramoRepository.save(tramo);
        return convertirADTO(updated);
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