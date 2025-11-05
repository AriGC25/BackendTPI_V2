package com.transportista.tarifas.controller;

import com.transportista.tarifas.dto.TarifaDTO;
import com.transportista.tarifas.service.TarifaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tarifas")
@RequiredArgsConstructor
@Tag(name = "Tarifas", description = "Gestión de tarifas del sistema")
@SecurityRequirement(name = "bearer-jwt")
public class TarifaController {

    private final TarifaService tarifaService;

    @GetMapping
    @Operation(summary = "Listar todas las tarifas", description = "Obtiene todas las tarifas del sistema")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<TarifaDTO>> listarTodas() {
        List<TarifaDTO> tarifas = tarifaService.listarTodas();
        return ResponseEntity.ok(tarifas);
    }

    @GetMapping("/activas")
    @Operation(summary = "Listar tarifas activas", description = "Obtiene solo las tarifas activas")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<TarifaDTO>> listarActivas() {
        List<TarifaDTO> tarifas = tarifaService.listarActivas();
        return ResponseEntity.ok(tarifas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tarifa por ID", description = "Obtiene una tarifa específica por su ID")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TarifaDTO> obtenerPorId(@PathVariable Long id) {
        TarifaDTO tarifa = tarifaService.obtenerPorId(id);
        return ResponseEntity.ok(tarifa);
    }

    @GetMapping("/tipo/{tipoTramo}")
    @Operation(summary = "Obtener tarifa por tipo de tramo", description = "Obtiene la tarifa activa para un tipo de tramo específico")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TarifaDTO> obtenerPorTipoTramo(@PathVariable String tipoTramo) {
        TarifaDTO tarifa = tarifaService.obtenerPorTipoTramo(tipoTramo);
        return ResponseEntity.ok(tarifa);
    }

    @PostMapping
    @Operation(summary = "Crear nueva tarifa", description = "Crea una nueva tarifa en el sistema")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TarifaDTO> crearTarifa(@Valid @RequestBody TarifaDTO tarifaDTO) {
        TarifaDTO nuevaTarifa = tarifaService.crearTarifa(tarifaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevaTarifa);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tarifa", description = "Actualiza una tarifa existente")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<TarifaDTO> actualizarTarifa(@PathVariable Long id, @Valid @RequestBody TarifaDTO tarifaDTO) {
        TarifaDTO tarifaActualizada = tarifaService.actualizarTarifa(id, tarifaDTO);
        return ResponseEntity.ok(tarifaActualizada);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tarifa", description = "Marca una tarifa como inactiva")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<Void> eliminarTarifa(@PathVariable Long id) {
        tarifaService.eliminarTarifa(id);
        return ResponseEntity.noContent().build();
    }
}
