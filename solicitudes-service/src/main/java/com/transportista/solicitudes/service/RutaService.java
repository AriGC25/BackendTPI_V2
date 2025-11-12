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
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RutaService {

    private final RutaRepository rutaRepository;
    private final SolicitudRepository solicitudRepository;
    private final GoogleMapsService googleMapsService;
    private final WebClient.Builder webClientBuilder;

    public List<RutaDTO> consultarRutasTentativas(Long solicitudId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada con ID: " + solicitudId));

        // Generar rutas tentativas basadas en la solicitud
        List<RutaDTO> rutasTentativas = new ArrayList<>();

        // Ruta directa (origen -> destino)
        RutaDTO rutaDirecta = generarRutaDirecta(solicitud);
        calcularEstimacionesRuta(rutaDirecta, solicitud);
        rutasTentativas.add(rutaDirecta);

        // Ruta con un depósito intermedio
        RutaDTO rutaConDeposito = generarRutaConDeposito(solicitud);
        if (rutaConDeposito != null) {
            calcularEstimacionesRuta(rutaConDeposito, solicitud);
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

        // Crear y guardar los tramos
        if (rutaDTO.getTramos() != null && !rutaDTO.getTramos().isEmpty()) {
            List<Tramo> tramos = new ArrayList<>();
            int orden = 1;
            for (TramoDTO tramoDTO : rutaDTO.getTramos()) {
                Tramo tramo = new Tramo();
                tramo.setRuta(rutaGuardada);
                tramo.setTipoTramo(tramoDTO.getTipoTramo());
                tramo.setOrdenTramo(tramoDTO.getOrdenTramo() != null ? tramoDTO.getOrdenTramo() : orden);
                tramo.setEstado("estimado");
                tramo.setDireccionOrigen(tramoDTO.getDireccionOrigen());
                tramo.setLatitudOrigen(tramoDTO.getLatitudOrigen());
                tramo.setLongitudOrigen(tramoDTO.getLongitudOrigen());
                tramo.setDireccionDestino(tramoDTO.getDireccionDestino());
                tramo.setLatitudDestino(tramoDTO.getLatitudDestino());
                tramo.setLongitudDestino(tramoDTO.getLongitudDestino());

                // *** PERSISTIR DISTANCIA Y TIEMPO ESTIMADO ***
                tramo.setDistanciaKm(tramoDTO.getDistanciaKm());
                tramo.setTiempoEstimadoMinutos(tramoDTO.getTiempoEstimadoMinutos());

                // *** PERSISTIR COSTO APROXIMADO ***
                tramo.setCostoAproximado(tramoDTO.getCostoAproximado());

                // *** ASIGNAR DÍAS DE ESTADÍA SI APLICA ***
                if (esTramoConDeposito(tramoDTO.getTipoTramo())) {
                    // Si el DTO tiene días de estadía, usarlos; si no, usar 1 día por defecto
                    tramo.setDiasEstadiaDeposito(
                        tramoDTO.getDiasEstadiaDeposito() != null
                            ? tramoDTO.getDiasEstadiaDeposito()
                            : 1
                    );
                }

                tramos.add(tramo);
                orden++;
            }
            rutaGuardada.setTramos(tramos);
            rutaGuardada = rutaRepository.save(rutaGuardada);
        }

        // Actualizar estado de la solicitud
        solicitud.setEstado("RUTA_ASIGNADA");
        solicitud.setCostoEstimado(rutaDTO.getCostoEstimado());
        solicitud.setTiempoEstimadoHoras(rutaDTO.getTiempoEstimadoHoras());

        // Calcular fecha estimada de entrega basada en el tiempo estimado
        if (rutaDTO.getTiempoEstimadoHoras() != null && solicitud.getFechaSolicitud() != null) {
            long horasEstimadas = rutaDTO.getTiempoEstimadoHoras().longValue();
            solicitud.setFechaEstimadaEntrega(
                solicitud.getFechaSolicitud().plusHours(horasEstimadas)
            );
        }

        solicitud.setFechaActualizacion(LocalDateTime.now());
        solicitudRepository.save(solicitud);

        // Registrar evento de tracking cuando se asigna la ruta
        registrarEventoTracking(
            solicitud.getContenedor().getId(),
            solicitud.getId(),
            "RUTA_ASIGNADA",
            "Ruta planificada con " + rutaDTO.getCantidadTramos() + " tramos",
            "Ruta asignada - Tiempo estimado: " + rutaDTO.getTiempoEstimadoHoras() + "h - Costo estimado: $" + rutaDTO.getCostoEstimado()
        );

        return convertToDTO(rutaGuardada);
    }

    public RutaDTO obtenerRutaPorSolicitud(Long solicitudId) {
        Ruta ruta = rutaRepository.findBySolicitudId(solicitudId)
                .orElseThrow(() -> new RuntimeException("No se encontró ruta para la solicitud ID: " + solicitudId));
        return convertToDTO(ruta);
    }

    /**
     * Calcula el costo, tiempo y distancia estimados de una ruta
     */
    private void calcularEstimacionesRuta(RutaDTO ruta, Solicitud solicitud) {
        BigDecimal distanciaTotal = BigDecimal.ZERO;
        BigDecimal costoTotal = BigDecimal.ZERO;
        BigDecimal tiempoTotal = BigDecimal.ZERO;

        final BigDecimal COSTO_POR_KM = BigDecimal.valueOf(120); // ARS por km
        final BigDecimal COSTO_COMBUSTIBLE_POR_KM = BigDecimal.valueOf(52.5); // 0.35 L/km * 150 ARS/L
        final BigDecimal VELOCIDAD_PROMEDIO = BigDecimal.valueOf(60); // km/h
        final BigDecimal TIEMPO_CARGA_DESCARGA = BigDecimal.valueOf(2); // horas por tramo
        final BigDecimal COSTO_ESTADIA_DEPOSITO = BigDecimal.valueOf(500); // ARS por depósito
        final BigDecimal HORAS_POR_DIA_ESTADIA = BigDecimal.valueOf(24); // 24 horas por día

        // Calcular distancia y tiempo por cada tramo
        for (TramoDTO tramo : ruta.getTramos()) {
            BigDecimal distanciaTramo = googleMapsService.calcularDistanciaReal(
                tramo.getLatitudOrigen(), tramo.getLongitudOrigen(),
                tramo.getLatitudDestino(), tramo.getLongitudDestino()
            );

            distanciaTotal = distanciaTotal.add(distanciaTramo);

            // *** ASIGNAR DISTANCIA AL TRAMO ***
            tramo.setDistanciaKm(distanciaTramo.setScale(2, RoundingMode.HALF_UP));

            // Costo del tramo = (distancia × costo_por_km) + (distancia × costo_combustible_por_km)
            BigDecimal costoTramo = distanciaTramo.multiply(COSTO_POR_KM.add(COSTO_COMBUSTIBLE_POR_KM));

            // *** ASIGNAR COSTO APROXIMADO AL TRAMO ***
            tramo.setCostoAproximado(costoTramo.setScale(2, RoundingMode.HALF_UP));

            costoTotal = costoTotal.add(costoTramo);

            // *** CALCULAR TIEMPO ESTIMADO DEL TRAMO ***
            // Tiempo del tramo = (distancia / velocidad) + tiempo de carga/descarga
            BigDecimal tiempoTramoHoras = distanciaTramo.divide(VELOCIDAD_PROMEDIO, 2, RoundingMode.HALF_UP)
                                                   .add(TIEMPO_CARGA_DESCARGA);
            // Convertir a minutos
            Integer tiempoTramoMinutos = tiempoTramoHoras.multiply(BigDecimal.valueOf(60))
                                                          .intValue();
            tramo.setTiempoEstimadoMinutos(tiempoTramoMinutos);

            tiempoTotal = tiempoTotal.add(tiempoTramoHoras);

            // *** ASIGNAR DÍAS DE ESTADÍA SI EL TRAMO INCLUYE DEPÓSITO ***
            if (esTramoConDeposito(tramo.getTipoTramo())) {
                tramo.setDiasEstadiaDeposito(1); // Por defecto 1 día
                // Agregar tiempo de estadía en depósito al tiempo total
                BigDecimal tiempoEstadia = HORAS_POR_DIA_ESTADIA.multiply(BigDecimal.valueOf(1));
                tiempoTotal = tiempoTotal.add(tiempoEstadia);
            }
        }

        // Agregar costo de estadías en depósitos
        if (ruta.getCantidadDepositos() != null && ruta.getCantidadDepositos() > 0) {
            BigDecimal costoEstadias = COSTO_ESTADIA_DEPOSITO.multiply(
                BigDecimal.valueOf(ruta.getCantidadDepositos())
            );
            costoTotal = costoTotal.add(costoEstadias);
        }

        // Aplicar factor por peso y volumen del contenedor
        if (solicitud.getContenedor() != null) {
            BigDecimal factor = calcularFactorContenedor(solicitud);
            costoTotal = costoTotal.multiply(factor);
        }

        // Asignar valores calculados al DTO
        ruta.setDistanciaTotal(distanciaTotal.setScale(2, RoundingMode.HALF_UP));
        ruta.setCostoEstimado(costoTotal.setScale(2, RoundingMode.HALF_UP));
        ruta.setTiempoEstimadoHoras(tiempoTotal.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * Calcula el factor multiplicador basado en peso y volumen del contenedor
     */
    private BigDecimal calcularFactorContenedor(Solicitud solicitud) {
        BigDecimal pesoFactor = solicitud.getContenedor().getPeso()
            .divide(BigDecimal.valueOf(20000), 4, RoundingMode.HALF_UP);
        BigDecimal volumenFactor = solicitud.getContenedor().getVolumen()
            .divide(BigDecimal.valueOf(50), 4, RoundingMode.HALF_UP);

        BigDecimal factor = BigDecimal.ONE.add(pesoFactor).add(volumenFactor);
        return factor.min(BigDecimal.valueOf(2.0)); // Máximo 2x el costo base
    }

    /**
     * Determina si un tipo de tramo incluye depósito
     */
    private boolean esTramoConDeposito(String tipoTramo) {
        return "ORIGEN_DEPOSITO".equals(tipoTramo) ||
               "DEPOSITO_DEPOSITO".equals(tipoTramo);
    }

    private RutaDTO generarRutaDirecta(Solicitud solicitud) {
        RutaDTO ruta = new RutaDTO();
        ruta.setSolicitudId(solicitud.getId());
        ruta.setCantidadTramos(1);
        ruta.setCantidadDepositos(0);
        ruta.setDescripcion("Ruta directa sin depósitos intermedios");

        List<TramoDTO> tramos = new ArrayList<>();
        TramoDTO tramo = new TramoDTO();
        tramo.setTipoTramo("ORIGEN_DESTINO");
        tramo.setOrdenTramo(1);
        tramo.setEstado("estimado");
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
        RutaDTO ruta = new RutaDTO();
        ruta.setSolicitudId(solicitud.getId());
        ruta.setCantidadTramos(2);
        ruta.setCantidadDepositos(1);
        ruta.setDescripcion("Ruta con 1 depósito intermedio (Depósito Central)");

        List<TramoDTO> tramos = new ArrayList<>();

        // Tramo 1: Origen -> Depósito
        TramoDTO tramo1 = new TramoDTO();
        tramo1.setTipoTramo("ORIGEN_DEPOSITO");
        tramo1.setOrdenTramo(1);
        tramo1.setEstado("estimado");
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
        tramo2.setEstado("estimado");
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
        dto.setDistanciaKm(tramo.getDistanciaKm()); // *** NUEVO ***
        dto.setTiempoEstimadoMinutos(tramo.getTiempoEstimadoMinutos()); // *** NUEVO ***
        dto.setCostoAproximado(tramo.getCostoAproximado()); // *** NUEVO ***
        dto.setCostoReal(tramo.getCostoReal()); // *** NUEVO ***
        dto.setDiasEstadiaDeposito(tramo.getDiasEstadiaDeposito()); // *** NUEVO ***
        dto.setCamionId(tramo.getCamionId());
        dto.setTransportistaId(tramo.getTransportistaId());
        dto.setFechaInicio(tramo.getFechaInicio());
        dto.setFechaFin(tramo.getFechaFin());
        dto.setFechaCreacion(tramo.getFechaCreacion());
        dto.setFechaActualizacion(tramo.getFechaActualizacion());
        return dto;
    }

    /**
     * Registra un evento en el tracking-service
     */
    private void registrarEventoTracking(Long contenedorId, Long solicitudId, String estado, String ubicacion, String descripcion) {
        try {
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
        } catch (Exception e) {
            // Log error pero no fallar la operación principal
            System.err.println("Error al registrar evento de tracking: " + e.getMessage());
        }
    }
}
