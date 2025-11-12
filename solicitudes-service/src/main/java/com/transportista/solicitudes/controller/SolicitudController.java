package com.transportista.solicitudes.controller;

import com.transportista.solicitudes.dto.SolicitudRequestDTO;
import com.transportista.solicitudes.dto.SolicitudResponseDTO;
import com.transportista.solicitudes.dto.SeguimientoEstadoDTO;
import com.transportista.solicitudes.service.SolicitudService;
import com.transportista.solicitudes.service.CalculoCostoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/solicitudes")
@RequiredArgsConstructor
@Tag(name = "Solicitudes", description = "API para gestión de solicitudes de transporte")
@SecurityRequirement(name = "bearer-jwt")
public class SolicitudController {

    private final SolicitudService solicitudService;
    private final CalculoCostoService calculoCostoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR')")
    @Operation(summary = "Crear solicitud", description = "Crea una nueva solicitud de transporte")
    public ResponseEntity<SolicitudResponseDTO> crearSolicitud(@Valid @RequestBody SolicitudRequestDTO solicitudDTO) {
        SolicitudResponseDTO created = solicitudService.crearSolicitud(solicitudDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener solicitud", description = "Obtiene una solicitud por su ID")
    public ResponseEntity<SolicitudResponseDTO> obtenerSolicitud(@PathVariable("id") Long id) {
        SolicitudResponseDTO solicitud = solicitudService.obtenerSolicitud(id);
        return ResponseEntity.ok(solicitud);
    }

    @GetMapping("/numero/{numeroSolicitud}")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener solicitud por número", description = "Obtiene una solicitud por su número")
    public ResponseEntity<SolicitudResponseDTO> obtenerSolicitudPorNumero(@PathVariable("numeroSolicitud") String numeroSolicitud) {
        SolicitudResponseDTO solicitud = solicitudService.obtenerSolicitudPorNumero(numeroSolicitud);
        return ResponseEntity.ok(solicitud);
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar solicitudes", description = "Lista todas las solicitudes")
    public ResponseEntity<List<SolicitudResponseDTO>> listarSolicitudes() {
        List<SolicitudResponseDTO> solicitudes = solicitudService.listarSolicitudes();
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/estado/{estado}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Filtrar solicitudes por estado", description = "Obtiene solicitudes filtradas por estado")
    public ResponseEntity<List<SolicitudResponseDTO>> listarSolicitudesPorEstado(@PathVariable("estado") String estado) {
        List<SolicitudResponseDTO> solicitudes = solicitudService.listarSolicitudesPorEstado(estado);
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/cliente/{clienteId}")
    @PreAuthorize("hasAnyRole('CLIENTE','OPERADOR')")
    @Operation(summary = "Listar solicitudes por cliente", description = "Lista todas las solicitudes de un cliente por su ID")
    public ResponseEntity<List<SolicitudResponseDTO>> listarSolicitudesPorCliente(@PathVariable("clienteId") Long clienteId) {
        List<SolicitudResponseDTO> solicitudes = solicitudService.listarSolicitudesPorCliente(clienteId);
        return ResponseEntity.ok(solicitudes);
    }

    @GetMapping("/{id}/costo")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR')")
    @Operation(summary = "Calcular costo total", description = "Calcula el costo total de la solicitud incluyendo distancia, combustible, estadía y factores de peso/volumen")
    public ResponseEntity<Map<String, Object>> calcularCostoTotal(@PathVariable("id") Long id) {
        BigDecimal costoTotal = calculoCostoService.calcularCostoTotal(id);
        BigDecimal tiempoEstimado = calculoCostoService.calcularTiempoEstimado(id);

        Map<String, Object> resultado = Map.of(
                "solicitudId", id,
                "costoTotal", costoTotal,
                "tiempoEstimadoHoras", tiempoEstimado,
                "moneda", "ARS"
        );

        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{id}/tiempo-estimado")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR')")
    @Operation(summary = "Calcular tiempo estimado", description = "Calcula el tiempo estimado de entrega basado en distancias y velocidad promedio")
    public ResponseEntity<Map<String, Object>> calcularTiempoEstimado(@PathVariable("id") Long id) {
        BigDecimal tiempoEstimado = calculoCostoService.calcularTiempoEstimado(id);

        Map<String, Object> resultado = Map.of(
                "solicitudId", id,
                "tiempoEstimadoHoras", tiempoEstimado,
                "tiempoEstimadoDias", tiempoEstimado.divide(new BigDecimal("24"), 1, java.math.RoundingMode.HALF_UP)
        );

        return ResponseEntity.ok(resultado);
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar estado de solicitud", description = "Actualiza el estado de una solicitud")
    public ResponseEntity<SolicitudResponseDTO> actualizarEstado(@PathVariable("id") Long id, @RequestParam("estado") String estado) {
        SolicitudResponseDTO solicitudActualizada = solicitudService.actualizarEstado(id, estado);
        return ResponseEntity.ok(solicitudActualizada);
    }

    @GetMapping("/{id}/seguimiento")
    @PreAuthorize("hasAnyRole('CLIENTE', 'OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Obtener seguimiento de solicitud", description = "Obtiene el historial de estados de una solicitud en orden cronológico")
    public ResponseEntity<List<SeguimientoEstadoDTO>> obtenerSeguimiento(@PathVariable("id") Long id) {
        List<SeguimientoEstadoDTO> seguimiento = solicitudService.obtenerSeguimiento(id);
        return ResponseEntity.ok(seguimiento);
    }
}
