package com.transportista.solicitudes.controller;

import com.transportista.solicitudes.dto.RutaDTO;
import com.transportista.solicitudes.service.RutaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rutas")
@Tag(name = "Rutas", description = "Gestión de rutas y opciones tentativas")
public class RutaController {

    @Autowired
    private RutaService rutaService;

    @GetMapping("/tentativas/{solicitudId}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Consultar rutas tentativas con múltiples opciones")
    public ResponseEntity<List<RutaDTO>> consultarRutasTentativas(@PathVariable Long solicitudId) {
        List<RutaDTO> rutas = rutaService.consultarRutasTentativas(solicitudId);
        return ResponseEntity.ok(rutas);
    }

    @PostMapping("/asignar/{solicitudId}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Asignar ruta definitiva a solicitud")
    public ResponseEntity<RutaDTO> asignarRuta(
            @PathVariable Long solicitudId,
            @RequestBody RutaDTO rutaDTO) {
        RutaDTO ruta = rutaService.asignarRuta(solicitudId, rutaDTO);
        return ResponseEntity.ok(ruta);
    }

    @GetMapping("/solicitud/{solicitudId}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Obtener ruta asignada de una solicitud")
    public ResponseEntity<RutaDTO> obtenerRutaDeSolicitud(@PathVariable Long solicitudId) {
        RutaDTO ruta = rutaService.obtenerRutaPorSolicitud(solicitudId);
        return ResponseEntity.ok(ruta);
    }
}