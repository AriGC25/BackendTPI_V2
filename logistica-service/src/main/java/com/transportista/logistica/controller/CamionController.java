package com.transportista.logistica.controller;

import com.transportista.logistica.dto.CamionDTO;
import com.transportista.logistica.service.CamionService;
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
@RequestMapping("/camiones")
@Tag(name = "Camiones", description = "API para gestión de camiones")
public class CamionController {

    @Autowired
    private CamionService camionService;

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear camión", description = "Registra un nuevo camión en el sistema")
    public ResponseEntity<CamionDTO> crearCamion(@Valid @RequestBody CamionDTO camionDTO) {
        CamionDTO created = camionService.crearCamion(camionDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Obtener camión", description = "Obtiene un camión por su ID")
    public ResponseEntity<CamionDTO> obtenerCamion(@PathVariable Long id) {
        CamionDTO camion = camionService.obtenerCamion(id);
        return ResponseEntity.ok(camion);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Listar camiones", description = "Lista todos los camiones")
    public ResponseEntity<List<CamionDTO>> listarCamiones() {
        List<CamionDTO> camiones = camionService.listarCamiones();
        return ResponseEntity.ok(camiones);
    }

    @GetMapping("/disponibles")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Listar camiones disponibles", description = "Lista todos los camiones disponibles")
    public ResponseEntity<List<CamionDTO>> listarCamionesDisponibles() {
        List<CamionDTO> camiones = camionService.listarCamionesDisponibles();
        return ResponseEntity.ok(camiones);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar camión", description = "Actualiza los datos de un camión")
    public ResponseEntity<CamionDTO> actualizarCamion(@PathVariable Long id, @Valid @RequestBody CamionDTO camionDTO) {
        CamionDTO updated = camionService.actualizarCamion(id, camionDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Eliminar camión", description = "Desactiva un camión del sistema")
    public ResponseEntity<Void> eliminarCamion(@PathVariable Long id) {
        camionService.eliminarCamion(id);
        return ResponseEntity.noContent().build();
    }
}
