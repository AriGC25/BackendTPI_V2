package com.transportista.solicitudes.controller;

import com.transportista.solicitudes.dto.TramoDTO;
import com.transportista.solicitudes.dto.TramoRequestDTO;
import com.transportista.solicitudes.service.TramoService;
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
@RequestMapping("/tramos")
@RequiredArgsConstructor
@Tag(name = "Tramos", description = "Gestión de tramos de transporte")
@SecurityRequirement(name = "bearer-jwt")
public class TramoController {

    private final TramoService tramoService;

    @GetMapping
    @Operation(summary = "Listar tramos", description = "Obtiene todos los tramos del sistema")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<TramoDTO>> listarTramos() {
        List<TramoDTO> tramos = tramoService.listarTodos();
        return ResponseEntity.ok(tramos);
    }

    @GetMapping("/transportista/{transportistaId}")
    @Operation(summary = "Ver tramos asignados", description = "Obtiene los tramos asignados a un transportista específico")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    public ResponseEntity<List<TramoDTO>> obtenerTramosPorTransportista(@PathVariable String transportistaId) {
        List<TramoDTO> tramos = tramoService.obtenerTramosPorTransportista(transportistaId);
        return ResponseEntity.ok(tramos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tramo por ID", description = "Obtiene un tramo específico por su ID")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    public ResponseEntity<TramoDTO> obtenerTramoPorId(@PathVariable Long id) {
        TramoDTO tramo = tramoService.obtenerPorId(id);
        return ResponseEntity.ok(tramo);
    }

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear tramo", description = "Crea y asigna un tramo a una ruta")
    public ResponseEntity<TramoDTO> crearTramo(@Valid @RequestBody TramoRequestDTO dto) {
        TramoDTO created = tramoService.crearTramo(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}/asignar-camion")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Asignar camión", description = "Asigna un camión a un tramo específico")
    public ResponseEntity<TramoDTO> asignarCamion(
            @PathVariable Long id,
            @RequestParam Long camionId,
            @RequestParam String transportista) {
        TramoDTO updated = tramoService.asignarCamion(id, camionId, transportista);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/iniciar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Iniciar tramo", description = "Marca el inicio de un tramo de transporte")
    public ResponseEntity<TramoDTO> iniciarTramo(@PathVariable Long id) {
        TramoDTO updated = tramoService.iniciarTramo(id);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('OPERADOR', 'TRANSPORTISTA')")
    @Operation(summary = "Finalizar tramo", description = "Marca la finalización de un tramo de transporte")
    public ResponseEntity<TramoDTO> finalizarTramo(@PathVariable Long id) {
        TramoDTO updated = tramoService.finalizarTramo(id);
        return ResponseEntity.ok(updated);
    }
}
