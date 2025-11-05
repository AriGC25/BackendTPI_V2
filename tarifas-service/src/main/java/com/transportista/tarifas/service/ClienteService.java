package com.transportista.tarifas.service;

import com.transportista.tarifas.dto.ClienteDTO;
import com.transportista.tarifas.entity.Cliente;
import com.transportista.tarifas.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public ClienteDTO crearCliente(ClienteDTO dto) {
        if (clienteRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Ya existe un cliente con ese email");
        }
        if (clienteRepository.existsByDni(dto.getDni())) {
            throw new RuntimeException("Ya existe un cliente con ese DNI");
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setDni(dto.getDni());
        cliente.setDomicilio(dto.getDomicilio());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setActivo(true);

        Cliente saved = clienteRepository.save(cliente);
        return toDTO(saved);
    }

    public ClienteDTO obtenerCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        return toDTO(cliente);
    }

    public List<ClienteDTO> listarClientes() {
        return clienteRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public ClienteDTO actualizarCliente(Long id, ClienteDTO dto) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setDomicilio(dto.getDomicilio());
        cliente.setTelefono(dto.getTelefono());
        if (dto.getActivo() != null) {
            cliente.setActivo(dto.getActivo());
        }

        Cliente updated = clienteRepository.save(cliente);
        return toDTO(updated);
    }

    public void eliminarCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
        cliente.setActivo(false);
        clienteRepository.save(cliente);
    }

    private ClienteDTO toDTO(Cliente cliente) {
        ClienteDTO dto = new ClienteDTO();
        dto.setId(cliente.getId());
        dto.setNombre(cliente.getNombre());
        dto.setApellido(cliente.getApellido());
        dto.setDni(cliente.getDni());
        dto.setDomicilio(cliente.getDomicilio());
        dto.setTelefono(cliente.getTelefono());
        dto.setEmail(cliente.getEmail());
        dto.setActivo(cliente.getActivo());
        return dto;
    }
}
