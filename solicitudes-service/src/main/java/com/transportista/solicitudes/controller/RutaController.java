package com.transportista.solicitudes.controller;

import com.transportista.solicitudes.dto.RutaDTO;
import com.transportista.solicitudes.service.RutaService;
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
@RequestMapping("/rutas")
@RequiredArgsConstructor
@Tag(name = "Rutas", description = "Gestión de rutas de transporte")
@SecurityRequirement(name = "bearer-jwt")
public class RutaController {

    private final RutaService rutaService;

    @GetMapping("/tentativas/{solicitudId}")
    @Operation(summary = "Consultar rutas tentativas", description = "Obtiene rutas tentativas con todos los tramos sugeridos y tiempo/costo estimados")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<List<RutaDTO>> consultarRutasTentativas(@PathVariable Long solicitudId) {
        List<RutaDTO> rutasTentativas = rutaService.consultarRutasTentativas(solicitudId);
        return ResponseEntity.ok(rutasTentativas);
    }

    @PostMapping("/asignar/{solicitudId}")
    @Operation(summary = "Asignar ruta a solicitud", description = "Asigna una ruta con todos sus tramos a la solicitud")
    @PreAuthorize("hasRole('OPERADOR')")
    public ResponseEntity<RutaDTO> asignarRuta(@PathVariable Long solicitudId, @Valid @RequestBody RutaDTO rutaDTO) {
        RutaDTO rutaAsignada = rutaService.asignarRuta(solicitudId, rutaDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(rutaAsignada);
    }

    @GetMapping("/solicitud/{solicitudId}")
    @Operation(summary = "Obtener ruta por solicitud", description = "Obtiene la ruta asignada a una solicitud específica")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    public ResponseEntity<RutaDTO> obtenerRutaPorSolicitud(@PathVariable Long solicitudId) {
        RutaDTO ruta = rutaService.obtenerRutaPorSolicitud(solicitudId);
        return ResponseEntity.ok(ruta);
    }
}
