package com.transportista.tracking.service;

import com.transportista.tracking.dto.TrackingEventoDTO;
import com.transportista.tracking.entity.TrackingEvento;
import com.transportista.tracking.repository.TrackingEventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TrackingService {

    @Autowired
    private TrackingEventoRepository trackingEventoRepository;

    public TrackingEventoDTO registrarEvento(Long contenedorId, Long solicitudId, String estado, String ubicacion, String descripcion) {
        TrackingEvento evento = new TrackingEvento();
        evento.setContenedorId(contenedorId);
        evento.setSolicitudId(solicitudId);
        evento.setEstado(estado);
        evento.setUbicacion(ubicacion);
        evento.setDescripcion(descripcion);
        evento.setFechaEvento(LocalDateTime.now());

        TrackingEvento saved = trackingEventoRepository.save(evento);
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
