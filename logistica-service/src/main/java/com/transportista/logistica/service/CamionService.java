package com.transportista.logistica.service;

import com.transportista.logistica.dto.CamionDTO;
import com.transportista.logistica.entity.Camion;
import com.transportista.logistica.repository.CamionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class CamionService {

    @Autowired
    private CamionRepository camionRepository;

    public CamionDTO crearCamion(CamionDTO dto) {
        if (camionRepository.existsByPatente(dto.getPatente())) {
            throw new RuntimeException("Ya existe un camión con esa patente");
        }

        Camion camion = new Camion();
        camion.setPatente(dto.getPatente());
        camion.setTransportista(dto.getTransportista());
        camion.setCapacidadPeso(dto.getCapacidadPeso());
        camion.setCapacidadVolumen(dto.getCapacidadVolumen());
        camion.setCostoPorKm(dto.getCostoPorKm());
        camion.setDisponible(true);
        camion.setActivo(true);

        Camion saved = camionRepository.save(camion);
        return toDTO(saved);
    }

    public CamionDTO obtenerCamion(Long id) {
        Camion camion = camionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Camión no encontrado"));
        return toDTO(camion);
    }

    public List<CamionDTO> listarCamiones() {
        return camionRepository.findAll().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public List<CamionDTO> listarCamionesDisponibles() {
        return camionRepository.findAllByDisponibleTrueAndActivoTrue().stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public CamionDTO actualizarCamion(Long id, CamionDTO dto) {
        Camion camion = camionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Camión no encontrado"));

        camion.setTransportista(dto.getTransportista());
        camion.setCapacidadPeso(dto.getCapacidadPeso());
        camion.setCapacidadVolumen(dto.getCapacidadVolumen());
        camion.setCostoPorKm(dto.getCostoPorKm());
        if (dto.getDisponible() != null) {
            camion.setDisponible(dto.getDisponible());
        }
        if (dto.getActivo() != null) {
            camion.setActivo(dto.getActivo());
        }

        Camion updated = camionRepository.save(camion);
        return toDTO(updated);
    }

    public void eliminarCamion(Long id) {
        Camion camion = camionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Camión no encontrado"));
        camion.setActivo(false);
        camionRepository.save(camion);
    }

    private CamionDTO toDTO(Camion camion) {
        CamionDTO dto = new CamionDTO();
        dto.setId(camion.getId());
        dto.setPatente(camion.getPatente());
        dto.setTransportista(camion.getTransportista());
        dto.setCapacidadPeso(camion.getCapacidadPeso());
        dto.setCapacidadVolumen(camion.getCapacidadVolumen());
        dto.setCostoPorKm(camion.getCostoPorKm());
        dto.setDisponible(camion.getDisponible());
        dto.setActivo(camion.getActivo());
        return dto;
    }
}
