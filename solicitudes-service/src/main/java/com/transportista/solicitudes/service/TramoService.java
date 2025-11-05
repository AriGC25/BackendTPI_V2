package com.transportista.solicitudes.service;

import com.transportista.solicitudes.dto.TramoDTO;
import com.transportista.solicitudes.dto.TramoRequestDTO;
import com.transportista.solicitudes.entity.Tramo;
import com.transportista.solicitudes.entity.Ruta;
import com.transportista.solicitudes.repository.TramoRepository;
import com.transportista.solicitudes.repository.RutaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TramoService {

    private final TramoRepository tramoRepository;
    private final RutaRepository rutaRepository;

    public List<TramoDTO> listarTodos() {
        return tramoRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public List<TramoDTO> obtenerTramosPorTransportista(String transportistaId) {
        return tramoRepository.findByTransportistaId(transportistaId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    public TramoDTO obtenerPorId(Long id) {
        Tramo tramo = tramoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + id));
        return convertirADTO(tramo);
    }

    /**
     * Crear un tramo a partir de TramoRequestDTO. Sólo se mapean los campos existentes en la entidad Tramo.
     */
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

    /**
     * Asigna un camión y transportista a un tramo que esté en estado PENDIENTE
     */
    public TramoDTO asignarCamion(Long tramoId, Long camionId, String transportistaId) {
        Tramo tramo = tramoRepository.findById(tramoId)
                .orElseThrow(() -> new RuntimeException("Tramo no encontrado con ID: " + tramoId));

        if (!"PENDIENTE".equals(tramo.getEstado())) {
            throw new RuntimeException("Solo se pueden asignar camiones a tramos en estado PENDIENTE");
        }

        tramo.setCamionId(camionId);
        tramo.setTransportistaId(transportistaId);
        tramo.setEstado("ASIGNADO");
        tramo.setFechaActualizacion(LocalDateTime.now());

        Tramo updated = tramoRepository.save(tramo);
        return convertirADTO(updated);
    }

    /**
     * Marca el inicio del tramo. Sólo se puede iniciar si está en estado ASIGNADO.
     */
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
}
