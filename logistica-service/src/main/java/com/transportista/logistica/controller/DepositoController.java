package com.transportista.logistica.controller;

import com.transportista.logistica.dto.DepositoDTO;
import com.transportista.logistica.service.DepositoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/depositos")
@Tag(name = "Depósitos", description = "API para gestión de depósitos")
public class DepositoController {

    @Autowired
    private DepositoService depositoService;

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear depósito", description = "Crea un nuevo depósito")
    public ResponseEntity<DepositoDTO> crearDeposito(@Valid @RequestBody DepositoDTO depositoDTO) {
        DepositoDTO created = depositoService.crearDeposito(depositoDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Obtener depósito", description = "Obtiene un depósito por su ID")
    public ResponseEntity<DepositoDTO> obtenerDeposito(@PathVariable Long id) {
        DepositoDTO deposito = depositoService.obtenerDeposito(id);
        return ResponseEntity.ok(deposito);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Listar depósitos", description = "Lista todos los depósitos")
    public ResponseEntity<List<DepositoDTO>> listarDepositos() {
        List<DepositoDTO> depositos = depositoService.listarDepositos();
        return ResponseEntity.ok(depositos);
    }

    @GetMapping("/activos")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Listar depósitos activos", description = "Lista todos los depósitos activos")
    public ResponseEntity<List<DepositoDTO>> listarDepositosActivos() {
        List<DepositoDTO> depositos = depositoService.listarDepositosActivos();
        return ResponseEntity.ok(depositos);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar depósito", description = "Actualiza los datos de un depósito")
    public ResponseEntity<DepositoDTO> actualizarDeposito(@PathVariable Long id, @Valid @RequestBody DepositoDTO depositoDTO) {
        DepositoDTO updated = depositoService.actualizarDeposito(id, depositoDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Eliminar depósito", description = "Desactiva un depósito")
    public ResponseEntity<Void> eliminarDeposito(@PathVariable Long id) {
        depositoService.eliminarDeposito(id);
        return ResponseEntity.noContent().build();
    }
}
