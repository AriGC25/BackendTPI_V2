package com.transportista.logistica.service;

import com.transportista.logistica.dto.DepositoDTO;
import com.transportista.logistica.entity.Deposito;
import com.transportista.logistica.repository.DepositoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DepositoService {

    @Autowired
    private DepositoRepository depositoRepository;

    public DepositoDTO crearDeposito(DepositoDTO dto) {
        Deposito deposito = new Deposito();
        deposito.setNombre(dto.getNombre());
        deposito.setDireccion(dto.getDireccion());
        deposito.setLatitud(dto.getLatitud());
        deposito.setLongitud(dto.getLongitud());
        deposito.setCapacidadMaxima(dto.getCapacidadMaxima());
        deposito.setCapacidadActual(dto.getCapacidadActual() != null ? dto.getCapacidadActual() : 0);
        deposito.setActivo(true);

        Deposito saved = depositoRepository.save(deposito);
        return toDTO(saved);
    }

    public DepositoDTO obtenerDeposito(Long id) {
        Deposito deposito = depositoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Depósito no encontrado"));
        return toDTO(deposito);
    }

    public List<DepositoDTO> listarDepositos() {
        return depositoRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<DepositoDTO> listarDepositosActivos() {
        return depositoRepository.findAllByActivoTrue().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public DepositoDTO actualizarDeposito(Long id, DepositoDTO dto) {
        Deposito deposito = depositoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Depósito no encontrado"));

        deposito.setNombre(dto.getNombre());
        deposito.setDireccion(dto.getDireccion());
        deposito.setLatitud(dto.getLatitud());
        deposito.setLongitud(dto.getLongitud());
        if (dto.getCapacidadMaxima() != null) {
            deposito.setCapacidadMaxima(dto.getCapacidadMaxima());
        }
        if (dto.getActivo() != null) {
            deposito.setActivo(dto.getActivo());
        }

        Deposito updated = depositoRepository.save(deposito);
        return toDTO(updated);
    }

    public void eliminarDeposito(Long id) {
        Deposito deposito = depositoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Depósito no encontrado"));
        deposito.setActivo(false);
        depositoRepository.save(deposito);
    }

    private DepositoDTO toDTO(Deposito deposito) {
        DepositoDTO dto = new DepositoDTO();
        dto.setId(deposito.getId());
        dto.setNombre(deposito.getNombre());
        dto.setDireccion(deposito.getDireccion());
        dto.setLatitud(deposito.getLatitud());
        dto.setLongitud(deposito.getLongitud());
        dto.setCapacidadMaxima(deposito.getCapacidadMaxima());
        dto.setCapacidadActual(deposito.getCapacidadActual());
        dto.setActivo(deposito.getActivo());
        return dto;
    }
}
