package com.transportista.tarifas.controller;

import com.transportista.tarifas.dto.ClienteDTO;
import com.transportista.tarifas.service.ClienteService;
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
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "API para gestión de clientes")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    @PostMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Crear cliente", description = "Crea un nuevo cliente en el sistema")
    public ResponseEntity<ClienteDTO> crearCliente(@Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO created = clienteService.crearCliente(clienteDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERADOR', 'CLIENTE')")
    @Operation(summary = "Obtener cliente", description = "Obtiene un cliente por su ID")
    public ResponseEntity<ClienteDTO> obtenerCliente(@PathVariable Long id) {
        ClienteDTO cliente = clienteService.obtenerCliente(id);
        return ResponseEntity.ok(cliente);
    }

    @GetMapping
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Listar clientes", description = "Lista todos los clientes")
    public ResponseEntity<List<ClienteDTO>> listarClientes() {
        List<ClienteDTO> clientes = clienteService.listarClientes();
        return ResponseEntity.ok(clientes);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Actualizar cliente", description = "Actualiza los datos de un cliente")
    public ResponseEntity<ClienteDTO> actualizarCliente(@PathVariable Long id, @Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO updated = clienteService.actualizarCliente(id, clienteDTO);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OPERADOR')")
    @Operation(summary = "Eliminar cliente", description = "Desactiva un cliente del sistema")
    public ResponseEntity<Void> eliminarCliente(@PathVariable Long id) {
        clienteService.eliminarCliente(id);
        return ResponseEntity.noContent().build();
    }
}
