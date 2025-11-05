package com.transportista.tarifas.controller;

import com.transportista.tarifas.dto.TarifaDTO;
import com.transportista.tarifas.entity.Tarifa;
import com.transportista.tarifas.repository.TarifaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/tarifas")
@Tag(name = "Tarifas", description = "Gestión de tarifas de transporte")
public class TarifaController {

    @Autowired
    private TarifaRepository tarifaRepository;

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar todas las tarifas")
    public ResponseEntity<List<TarifaDTO>> listarTarifas() {
        List<Tarifa> tarifas = tarifaRepository.findAll();
        return ResponseEntity.ok(tarifas.stream().map(this::convertirADTO).collect(Collectors.toList()));
    }

    @GetMapping("/activas")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar tarifas activas")
    public ResponseEntity<List<TarifaDTO>> listarTarifasActivas() {
        List<Tarifa> tarifas = tarifaRepository.findByActivoTrue();
        return ResponseEntity.ok(tarifas.stream().map(this::convertirADTO).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Obtener tarifa por ID")
    public ResponseEntity<TarifaDTO> obtenerPorId(@PathVariable Long id) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada"));
        return ResponseEntity.ok(convertirADTO(tarifa));
    }

    @GetMapping("/tipo/{tipoTramo}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Obtener tarifa por tipo de tramo")
    public ResponseEntity<TarifaDTO> obtenerPorTipoTramo(@PathVariable String tipoTramo) {
        Tarifa tarifa = tarifaRepository.findByTipoTramoAndActivoTrue(tipoTramo)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada para el tipo: " + tipoTramo));
        return ResponseEntity.ok(convertirADTO(tarifa));
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear nueva tarifa")
    public ResponseEntity<TarifaDTO> crearTarifa(@Valid @RequestBody TarifaDTO dto) {
        Tarifa tarifa = new Tarifa();
        tarifa.setTipoTramo(dto.getTipoTramo());
        tarifa.setCostoPorKm(dto.getCostoPorKm());
        tarifa.setGestionFija(dto.getGestionFija());
        tarifa.setConsumoCombustiblePorKm(dto.getConsumoCombustiblePorKm());
        tarifa.setPrecioCombustiblePorLitro(dto.getPrecioCombustiblePorLitro());
        tarifa.setTarifaEstadiaDepositoPorDia(dto.getTarifaEstadiaDepositoPorDia());
        tarifa.setActivo(true);

        tarifa = tarifaRepository.save(tarifa);
        return ResponseEntity.status(HttpStatus.CREATED).body(convertirADTO(tarifa));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar tarifa existente")
    public ResponseEntity<TarifaDTO> actualizarTarifa(@PathVariable Long id, @Valid @RequestBody TarifaDTO dto) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada"));

        tarifa.setCostoPorKm(dto.getCostoPorKm());
        tarifa.setGestionFija(dto.getGestionFija());
        tarifa.setConsumoCombustiblePorKm(dto.getConsumoCombustiblePorKm());
        tarifa.setPrecioCombustiblePorLitro(dto.getPrecioCombustiblePorLitro());
        tarifa.setTarifaEstadiaDepositoPorDia(dto.getTarifaEstadiaDepositoPorDia());

        tarifa = tarifaRepository.save(tarifa);
        return ResponseEntity.ok(convertirADTO(tarifa));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Desactivar tarifa")
    public ResponseEntity<Void> eliminarTarifa(@PathVariable Long id) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada"));

        tarifa.setActivo(false);
        tarifaRepository.save(tarifa);

        return ResponseEntity.noContent().build();
    }

    private TarifaDTO convertirADTO(Tarifa tarifa) {
        TarifaDTO dto = new TarifaDTO();
        dto.setId(tarifa.getId());
        dto.setTipoTramo(tarifa.getTipoTramo());
        dto.setCostoPorKm(tarifa.getCostoPorKm());
        dto.setGestionFija(tarifa.getGestionFija());
        dto.setConsumoCombustiblePorKm(tarifa.getConsumoCombustiblePorKm());
        dto.setPrecioCombustiblePorLitro(tarifa.getPrecioCombustiblePorLitro());
        dto.setTarifaEstadiaDepositoPorDia(tarifa.getTarifaEstadiaDepositoPorDia());
        dto.setActivo(tarifa.getActivo());
        return dto;
    }
}