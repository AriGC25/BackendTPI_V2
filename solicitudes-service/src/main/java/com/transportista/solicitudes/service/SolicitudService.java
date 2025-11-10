package com.transportista.solicitudes.service;

import com.transportista.solicitudes.dto.SolicitudRequestDTO;
import com.transportista.solicitudes.dto.SolicitudResponseDTO;
import com.transportista.solicitudes.entity.Contenedor;
import com.transportista.solicitudes.entity.Solicitud;
import com.transportista.solicitudes.repository.ContenedorRepository;
import com.transportista.solicitudes.repository.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SolicitudService {

    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private ContenedorRepository contenedorRepository;

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

        solicitud.setEstado(estado);
        solicitud.setFechaActualizacion(java.time.LocalDateTime.now());

        Solicitud solicitudActualizada = solicitudRepository.save(solicitud);
        return toResponseDTO(solicitudActualizada);
    }

    public List<SolicitudResponseDTO> listarSolicitudesPorEstado(String estado) {
        String normalized = estado == null ? "" : estado.trim().toUpperCase();
        List<Solicitud> solicitudes = solicitudRepository.findByEstado(normalized);
        return solicitudes.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
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
        dto.setCostoTotal(solicitud.getCostoTotal());
        dto.setTiempoEstimadoHoras(solicitud.getTiempoEstimadoHoras());
        dto.setFechaSolicitud(solicitud.getFechaSolicitud());
        dto.setFechaEstimadaEntrega(solicitud.getFechaEstimadaEntrega());
        dto.setFechaCreacion(solicitud.getFechaCreacion());
        dto.setFechaActualizacion(solicitud.getFechaActualizacion());
        return dto;
    }
}
