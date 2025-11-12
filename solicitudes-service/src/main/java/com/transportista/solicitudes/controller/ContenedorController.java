package com.transportista.solicitudes.controller;

import com.transportista.solicitudes.dto.ContenedorDTO;
import com.transportista.solicitudes.dto.ContenedorConUbicacionDTO;
import com.transportista.solicitudes.service.ContenedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/contenedores")
@Tag(name = "Contenedores", description = "API para gestión de contenedores")
public class ContenedorController {

    @Autowired
    private ContenedorService contenedorService;

    @PostMapping
    @Operation(summary = "Crear contenedor", description = "Registra un nuevo contenedor en el sistema")
    public ResponseEntity<ContenedorDTO> crearContenedor(@Valid @RequestBody ContenedorDTO contenedorDTO) {
        ContenedorDTO created = contenedorService.crearContenedor(contenedorDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener contenedor", description = "Obtiene un contenedor por su ID")
    public ResponseEntity<ContenedorDTO> obtenerContenedor(@PathVariable("id") Long id) {
        ContenedorDTO contenedor = contenedorService.obtenerContenedor(id);
        return ResponseEntity.ok(contenedor);
    }

    @GetMapping
    @Operation(summary = "Listar contenedores", description = "Lista todos los contenedores")
    public ResponseEntity<List<ContenedorDTO>> listarContenedores() {
        List<ContenedorDTO> contenedores = contenedorService.listarContenedores();
        return ResponseEntity.ok(contenedores);
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar contenedores por cliente", description = "Lista todos los contenedores de un cliente específico")
    public ResponseEntity<List<ContenedorDTO>> listarContenedoresPorCliente(@PathVariable("clienteId") Long clienteId) {
        List<ContenedorDTO> contenedores = contenedorService.listarContenedoresPorCliente(clienteId);
        return ResponseEntity.ok(contenedores);
    }

    @GetMapping("/estado/{estado}")
    @Operation(summary = "Listar contenedores por estado", description = "Lista todos los contenedores con un estado específico")
    public ResponseEntity<List<ContenedorDTO>> listarContenedoresPorEstado(@PathVariable("estado") String estado) {
        List<ContenedorDTO> contenedores = contenedorService.listarContenedoresPorEstado(estado);
        return ResponseEntity.ok(contenedores);
    }

    @GetMapping("/pendientes")
    @Operation(summary = "Listar contenedores pendientes", description = "Lista todos los contenedores en estado PENDIENTE")
    public ResponseEntity<List<ContenedorDTO>> listarContenedoresPendientes() {
        List<ContenedorDTO> contenedores = contenedorService.listarContenedoresPendientes();
        return ResponseEntity.ok(contenedores);
    }

    @GetMapping("/pendientes/con-ubicacion")
    @Operation(summary = "Listar contenedores pendientes con ubicación",
               description = "Lista todos los contenedores en estado PENDIENTE con información detallada de ubicación, origen, destino y estado de solicitud")
    public ResponseEntity<List<ContenedorConUbicacionDTO>> listarContenedoresPendientesConUbicacion() {
        List<ContenedorConUbicacionDTO> contenedores = contenedorService.listarContenedoresPendientesConUbicacion();
        return ResponseEntity.ok(contenedores);
    }

    @GetMapping("/estado/{estado}/con-ubicacion")
    @Operation(summary = "Listar contenedores por estado con ubicación",
               description = "Filtra contenedores por estado específico con información detallada de ubicación")
    public ResponseEntity<List<ContenedorConUbicacionDTO>> listarContenedoresPorEstadoConUbicacion(@PathVariable("estado") String estado) {
        List<ContenedorConUbicacionDTO> contenedores = contenedorService.listarContenedoresPorEstadoConUbicacion(estado);
        return ResponseEntity.ok(contenedores);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar contenedor", description = "Actualiza los datos de un contenedor")
    public ResponseEntity<ContenedorDTO> actualizarContenedor(@PathVariable("id") Long id, @Valid @RequestBody ContenedorDTO contenedorDTO) {
        ContenedorDTO updated = contenedorService.actualizarContenedor(id, contenedorDTO);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado del contenedor", description = "Actualiza únicamente el estado de un contenedor")
    public ResponseEntity<ContenedorDTO> actualizarEstado(@PathVariable("id") Long id, @RequestParam("estado") String estado) {
        ContenedorDTO updated = contenedorService.actualizarEstado(id, estado);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar contenedor", description = "Elimina un contenedor del sistema")
    public ResponseEntity<Void> eliminarContenedor(@PathVariable("id") Long id) {
        contenedorService.eliminarContenedor(id);
        return ResponseEntity.noContent().build();
    }
}
