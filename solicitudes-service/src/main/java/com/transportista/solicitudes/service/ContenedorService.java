package com.transportista.solicitudes.service;

import com.transportista.solicitudes.dto.ContenedorDTO;
import com.transportista.solicitudes.dto.ContenedorConUbicacionDTO;
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
public class ContenedorService {

    @Autowired
    private ContenedorRepository contenedorRepository;

    @Autowired
    private SolicitudRepository solicitudRepository;

    public ContenedorDTO crearContenedor(ContenedorDTO dto) {
        Contenedor contenedor = new Contenedor();
        contenedor.setPeso(dto.getPeso());
        contenedor.setVolumen(dto.getVolumen());
        contenedor.setEstado(dto.getEstado() != null ? dto.getEstado() : "PENDIENTE");
        contenedor.setClienteId(dto.getClienteId());
        contenedor.setDescripcion(dto.getDescripcion());

        Contenedor saved = contenedorRepository.save(contenedor);
        return toDTO(saved);
    }

    public ContenedorDTO obtenerContenedor(Long id) {
        Contenedor contenedor = contenedorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contenedor no encontrado"));
        return toDTO(contenedor);
    }

    public List<ContenedorDTO> listarContenedores() {
        return contenedorRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<ContenedorDTO> listarContenedoresPorCliente(Long clienteId) {
        return contenedorRepository.findByClienteId(clienteId).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<ContenedorDTO> listarContenedoresPorEstado(String estado) {
        return contenedorRepository.findByEstado(estado).stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<ContenedorDTO> listarContenedoresPendientes() {
        return contenedorRepository.findContenedoresPendientes().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public ContenedorDTO actualizarContenedor(Long id, ContenedorDTO dto) {
        Contenedor contenedor = contenedorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contenedor no encontrado"));

        contenedor.setPeso(dto.getPeso());
        contenedor.setVolumen(dto.getVolumen());
        if (dto.getEstado() != null) {
            contenedor.setEstado(dto.getEstado());
        }
        contenedor.setDescripcion(dto.getDescripcion());

        Contenedor updated = contenedorRepository.save(contenedor);
        return toDTO(updated);
    }

    public ContenedorDTO actualizarEstado(Long id, String nuevoEstado) {
        Contenedor contenedor = contenedorRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Contenedor no encontrado"));

        contenedor.setEstado(nuevoEstado);
        Contenedor updated = contenedorRepository.save(contenedor);
        return toDTO(updated);
    }

    public void eliminarContenedor(Long id) {
        if (!contenedorRepository.existsById(id)) {
            throw new RuntimeException("Contenedor no encontrado");
        }
        contenedorRepository.deleteById(id);
    }

    private ContenedorDTO toDTO(Contenedor contenedor) {
        ContenedorDTO dto = new ContenedorDTO();
        dto.setId(contenedor.getId());
        dto.setPeso(contenedor.getPeso());
        dto.setVolumen(contenedor.getVolumen());
        dto.setEstado(contenedor.getEstado());
        dto.setClienteId(contenedor.getClienteId());
        dto.setDescripcion(contenedor.getDescripcion());
        dto.setFechaCreacion(contenedor.getFechaCreacion());
        dto.setFechaActualizacion(contenedor.getFechaActualizacion());
        return dto;
    }

    public List<ContenedorConUbicacionDTO> listarContenedoresPendientesConUbicacion() {
        return contenedorRepository.findContenedoresPendientes().stream()
            .map(this::toDTOConUbicacion)
            .collect(Collectors.toList());
    }

    public List<ContenedorConUbicacionDTO> listarContenedoresPorEstadoConUbicacion(String estado) {
        return contenedorRepository.findByEstado(estado).stream()
            .map(this::toDTOConUbicacion)
            .collect(Collectors.toList());
    }

    private ContenedorConUbicacionDTO toDTOConUbicacion(Contenedor contenedor) {
        ContenedorConUbicacionDTO dto = new ContenedorConUbicacionDTO();

        // Datos básicos del contenedor
        dto.setId(contenedor.getId());
        dto.setPeso(contenedor.getPeso());
        dto.setVolumen(contenedor.getVolumen());
        dto.setEstado(contenedor.getEstado());
        dto.setClienteId(contenedor.getClienteId());
        dto.setDescripcion(contenedor.getDescripcion());
        dto.setFechaCreacion(contenedor.getFechaCreacion());

        // Buscar la solicitud asociada al contenedor
        Solicitud solicitud = solicitudRepository.findAll().stream()
            .filter(s -> s.getContenedor() != null && s.getContenedor().getId().equals(contenedor.getId()))
            .findFirst()
            .orElse(null);

        if (solicitud != null) {
            dto.setSolicitudId(solicitud.getId());
            dto.setNumeroSolicitud(solicitud.getNumeroSolicitud());
            dto.setEstadoSolicitud(solicitud.getEstado());

            // Ubicaciones de origen y destino
            dto.setDireccionOrigen(solicitud.getDireccionOrigen());
            dto.setLatitudOrigen(solicitud.getLatitudOrigen());
            dto.setLongitudOrigen(solicitud.getLongitudOrigen());

            dto.setDireccionDestino(solicitud.getDireccionDestino());
            dto.setLatitudDestino(solicitud.getLatitudDestino());
            dto.setLongitudDestino(solicitud.getLongitudDestino());

            // Información adicional
            dto.setTiempoEstimadoHoras(solicitud.getTiempoEstimadoHoras());
            dto.setFechaEstimadaEntrega(solicitud.getFechaEstimadaEntrega());

            // Determinar ubicación actual basada en el estado
            determinarUbicacionActual(dto, contenedor, solicitud);
        } else {
            // Si no hay solicitud asociada, el contenedor está en espera
            dto.setUbicacionActual("En espera de asignación");
            dto.setDescripcionUbicacion("Contenedor registrado pero sin solicitud de transporte asignada");
        }

        return dto;
    }

    private void determinarUbicacionActual(ContenedorConUbicacionDTO dto, Contenedor contenedor, Solicitud solicitud) {
        String estado = contenedor.getEstado();
        String estadoSolicitud = solicitud.getEstado();

        switch (estado) {
            case "PENDIENTE":
                if ("PENDIENTE".equals(estadoSolicitud)) {
                    dto.setUbicacionActual(solicitud.getDireccionOrigen());
                    dto.setDescripcionUbicacion("En origen, esperando asignación de ruta");
                } else if ("RUTA_ASIGNADA".equals(estadoSolicitud)) {
                    dto.setUbicacionActual(solicitud.getDireccionOrigen());
                    dto.setDescripcionUbicacion("En origen, ruta asignada, esperando inicio de transporte");
                } else {
                    dto.setUbicacionActual(solicitud.getDireccionOrigen());
                    dto.setDescripcionUbicacion("En origen");
                }
                break;

            case "EN_TRANSITO":
                dto.setUbicacionActual("En tránsito");
                dto.setDescripcionUbicacion(String.format("En camino desde %s hacia %s",
                    solicitud.getDireccionOrigen(),
                    solicitud.getDireccionDestino()));
                break;

            case "EN_DEPOSITO":
                dto.setUbicacionActual("En depósito intermedio");
                dto.setDescripcionUbicacion("Almacenado temporalmente en depósito");
                break;

            case "ENTREGADO":
                dto.setUbicacionActual(solicitud.getDireccionDestino());
                dto.setDescripcionUbicacion("Entregado en destino final");
                break;

            default:
                dto.setUbicacionActual("Ubicación desconocida");
                dto.setDescripcionUbicacion("Estado no reconocido");
        }
    }
}
