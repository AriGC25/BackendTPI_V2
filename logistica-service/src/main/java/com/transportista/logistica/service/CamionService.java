package com.transportista.logistica.service;

import com.transportista.logistica.dto.CamionDTO;
import com.transportista.logistica.entity.Camion;
import com.transportista.logistica.repository.CamionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
        camion.setModelo(dto.getModelo());
        camion.setCapacidadPeso(dto.getCapacidadPeso());
        camion.setCapacidadVolumen(dto.getCapacidadVolumen());
        camion.setConsumoCombustiblePorKm(dto.getConsumoCombustiblePorKm());
        camion.setNombreTransportista(dto.getNombreTransportista());
        camion.setTelefono(dto.getTelefono());
        camion.setDisponible(dto.getDisponible() != null ? dto.getDisponible() : true);
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

    public List<CamionDTO> obtenerCamionesElegibles(BigDecimal peso, BigDecimal volumen) {
        return camionRepository.findAllByDisponibleTrueAndActivoTrue().stream()
            .filter(camion -> camion.getCapacidadPeso().compareTo(peso) >= 0)
            .filter(camion -> camion.getCapacidadVolumen().compareTo(volumen) >= 0)
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    public CamionDTO actualizarCamion(Long id, CamionDTO dto) {
        Camion camion = camionRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Camión no encontrado"));

        camion.setModelo(dto.getModelo());
        camion.setCapacidadPeso(dto.getCapacidadPeso());
        camion.setCapacidadVolumen(dto.getCapacidadVolumen());
        camion.setConsumoCombustiblePorKm(dto.getConsumoCombustiblePorKm());
        camion.setNombreTransportista(dto.getNombreTransportista());
        camion.setTelefono(dto.getTelefono());
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
        dto.setModelo(camion.getModelo());
        dto.setCapacidadPeso(camion.getCapacidadPeso());
        dto.setCapacidadVolumen(camion.getCapacidadVolumen());
        dto.setConsumoCombustiblePorKm(camion.getConsumoCombustiblePorKm());
        dto.setNombreTransportista(camion.getNombreTransportista());
        dto.setTelefono(camion.getTelefono());
        dto.setDisponible(camion.getDisponible());
        dto.setActivo(camion.getActivo());
        return dto;
    }
}
