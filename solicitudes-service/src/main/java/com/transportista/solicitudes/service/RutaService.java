package com.transportista.solicitudes.service;

import com.transportista.solicitudes.dto.RutaDTO;
import com.transportista.solicitudes.dto.TramoDTO;
import com.transportista.solicitudes.entity.Ruta;
import com.transportista.solicitudes.entity.Solicitud;
import com.transportista.solicitudes.entity.Tramo;
import com.transportista.solicitudes.repository.RutaRepository;
import com.transportista.solicitudes.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RutaService {

    private final RutaRepository rutaRepository;
    private final SolicitudRepository solicitudRepository;

    public List<RutaDTO> consultarRutasTentativas(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + solicitudId));

        // Generar rutas tentativas basadas en la solicitud
        List<RutaDTO> rutasTentativas = new ArrayList<>();

        // Ruta directa (origen -> destino)
        RutaDTO rutaDirecta = generarRutaDirecta(solicitud);
        rutasTentativas.add(rutaDirecta);

        // Ruta con un depósito intermedio
        RutaDTO rutaConDeposito = generarRutaConDeposito(solicitud);
        if (rutaConDeposito != null) {
            rutasTentativas.add(rutaConDeposito);
        }

        return rutasTentativas;
    }

    public RutaDTO asignarRuta(Long solicitudId, RutaDTO rutaDTO) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + solicitudId));

        // Verificar que no tenga ya una ruta asignada
        if (rutaRepository.findBySolicitudId(solicitudId).isPresent()) {
            throw new RuntimeException("La solicitud ya tiene una ruta asignada");
        }

        Ruta ruta = new Ruta();
        ruta.setSolicitud(solicitud);
        ruta.setCantidadTramos(rutaDTO.getCantidadTramos() != null ? rutaDTO.getCantidadTramos() : 0);
        ruta.setCantidadDepositos(rutaDTO.getCantidadDepositos() != null ? rutaDTO.getCantidadDepositos() : 0);

        Ruta rutaGuardada = rutaRepository.save(ruta);

        // Actualizar estado de la solicitud
        solicitud.setEstado("RUTA_ASIGNADA");
        solicitud.setFechaActualizacion(LocalDateTime.now());
        solicitudRepository.save(solicitud);

        return convertToDTO(rutaGuardada);
    }

    public RutaDTO obtenerRutaPorSolicitud(Long solicitudId) {
        Ruta ruta = rutaRepository.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new RuntimeException("No se encontró ruta para la solicitud ID: " + solicitudId));
        return convertToDTO(ruta);
    }

    private RutaDTO generarRutaDirecta(Solicitud solicitud) {
        RutaDTO ruta = new RutaDTO();
        ruta.setSolicitudId(solicitud.getId());
        ruta.setCantidadTramos(1);
        ruta.setCantidadDepositos(0);

        List<TramoDTO> tramos = new ArrayList<>();
        TramoDTO tramo = new TramoDTO();
        tramo.setTipoTramo("ORIGEN_DESTINO");
        tramo.setOrdenTramo(1);
        tramo.setEstado("PENDIENTE");
        tramo.setLatitudOrigen(solicitud.getLatitudOrigen());
        tramo.setLongitudOrigen(solicitud.getLongitudOrigen());
        tramo.setLatitudDestino(solicitud.getLatitudDestino());
        tramo.setLongitudDestino(solicitud.getLongitudDestino());
        tramo.setDireccionOrigen(solicitud.getDireccionOrigen());
        tramo.setDireccionDestino(solicitud.getDireccionDestino());

        tramos.add(tramo);
        ruta.setTramos(tramos);

        return ruta;
    }

    private RutaDTO generarRutaConDeposito(Solicitud solicitud) {
        // Aquí podrías implementar lógica para encontrar depósitos intermedios
        // Por simplicidad, generamos una ruta ejemplo con un depósito ficticio
        RutaDTO ruta = new RutaDTO();
        ruta.setSolicitudId(solicitud.getId());
        ruta.setCantidadTramos(2);
        ruta.setCantidadDepositos(1);

        List<TramoDTO> tramos = new ArrayList<>();

        // Tramo 1: Origen -> Depósito
        TramoDTO tramo1 = new TramoDTO();
        tramo1.setTipoTramo("ORIGEN_DEPOSITO");
        tramo1.setOrdenTramo(1);
        tramo1.setEstado("PENDIENTE");
        tramo1.setLatitudOrigen(solicitud.getLatitudOrigen());
        tramo1.setLongitudOrigen(solicitud.getLongitudOrigen());
        tramo1.setDireccionOrigen(solicitud.getDireccionOrigen());
        // Coordenadas de depósito ejemplo
        tramo1.setLatitudDestino(new BigDecimal("-34.6037"));
        tramo1.setLongitudDestino(new BigDecimal("-58.3816"));
        tramo1.setDireccionDestino("Depósito Central - Av. General Paz 1000");

        // Tramo 2: Depósito -> Destino
        TramoDTO tramo2 = new TramoDTO();
        tramo2.setTipoTramo("DEPOSITO_DESTINO");
        tramo2.setOrdenTramo(2);
        tramo2.setEstado("PENDIENTE");
        tramo2.setLatitudOrigen(new BigDecimal("-34.6037"));
        tramo2.setLongitudOrigen(new BigDecimal("-58.3816"));
        tramo2.setDireccionOrigen("Depósito Central - Av. General Paz 1000");
        tramo2.setLatitudDestino(solicitud.getLatitudDestino());
        tramo2.setLongitudDestino(solicitud.getLongitudDestino());
        tramo2.setDireccionDestino(solicitud.getDireccionDestino());

        tramos.add(tramo1);
        tramos.add(tramo2);
        ruta.setTramos(tramos);

        return ruta;
    }

    private RutaDTO convertToDTO(Ruta ruta) {
        RutaDTO dto = new RutaDTO();
        dto.setId(ruta.getId());
        dto.setSolicitudId(ruta.getSolicitud().getId());
        dto.setCantidadTramos(ruta.getCantidadTramos());
        dto.setCantidadDepositos(ruta.getCantidadDepositos());
        dto.setFechaCreacion(ruta.getFechaCreacion());
        dto.setFechaActualizacion(ruta.getFechaActualizacion());

        if (ruta.getTramos() != null && !ruta.getTramos().isEmpty()) {
            List<TramoDTO> tramosDTO = ruta.getTramos().stream()
                    .map(this::convertTramoToDTO)
                    .collect(Collectors.toList());
            dto.setTramos(tramosDTO);
        }

        return dto;
    }

    private TramoDTO convertTramoToDTO(Tramo tramo) {
        TramoDTO dto = new TramoDTO();
        dto.setId(tramo.getId());
        dto.setRutaId(tramo.getRuta() != null ? tramo.getRuta().getId() : null);
        dto.setTipoTramo(tramo.getTipoTramo());
        dto.setOrdenTramo(tramo.getOrdenTramo());
        dto.setEstado(tramo.getEstado());
        dto.setLatitudOrigen(tramo.getLatitudOrigen());
        dto.setLongitudOrigen(tramo.getLongitudOrigen());
        dto.setLatitudDestino(tramo.getLatitudDestino());
        dto.setLongitudDestino(tramo.getLongitudDestino());
        dto.setDireccionOrigen(tramo.getDireccionOrigen());
        dto.setDireccionDestino(tramo.getDireccionDestino());
        dto.setCamionId(tramo.getCamionId());
        dto.setTransportistaId(tramo.getTransportistaId());
        dto.setFechaInicio(tramo.getFechaInicio());
        dto.setFechaFin(tramo.getFechaFin());
        dto.setFechaCreacion(tramo.getFechaCreacion());
        dto.setFechaActualizacion(tramo.getFechaActualizacion());
        return dto;
    }
}
