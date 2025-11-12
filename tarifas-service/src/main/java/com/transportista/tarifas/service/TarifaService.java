package com.transportista.tarifas.service;

import com.transportista.tarifas.dto.TarifaDTO;
import com.transportista.tarifas.entity.Tarifa;
import com.transportista.tarifas.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TarifaService {

    private final TarifaRepository tarifaRepository;

    public List<TarifaDTO> listarTodas() {
        return tarifaRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<TarifaDTO> listarActivas() {
        return tarifaRepository.findByActivoTrue().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public TarifaDTO obtenerPorId(Long id) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada con ID: " + id));
        return convertToDTO(tarifa);
    }

    public TarifaDTO crearTarifa(TarifaDTO dto) {
        Tarifa tarifa = convertToEntity(dto);
        tarifa.setFechaCreacion(LocalDateTime.now());
        tarifa.setActivo(true);

        Tarifa savedTarifa = tarifaRepository.save(tarifa);
        return convertToDTO(savedTarifa);
    }

    public TarifaDTO actualizarTarifa(Long id, TarifaDTO dto) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada con ID: " + id));

        tarifa.setTipoTramo(dto.getTipoTramo());
        tarifa.setCostoPorKm(dto.getCostoPorKm());
        tarifa.setGestionFija(dto.getGestionFija());
        tarifa.setConsumoCombustiblePorKm(dto.getConsumoCombustiblePorKm());
        tarifa.setPrecioCombustiblePorLitro(dto.getPrecioCombustiblePorLitro());
        tarifa.setTarifaEstadiaDepositoPorDia(dto.getTarifaEstadiaDepositoPorDia());
        tarifa.setTarifaBasePesoLigero(dto.getTarifaBasePesoLigero());
        tarifa.setTarifaBasePesoMedio(dto.getTarifaBasePesoMedio());
        tarifa.setTarifaBasePesoPesado(dto.getTarifaBasePesoPesado());
        tarifa.setTarifaBaseVolumenPequeno(dto.getTarifaBaseVolumenPequeno());
        tarifa.setTarifaBaseVolumenMediano(dto.getTarifaBaseVolumenMediano());
        tarifa.setTarifaBaseVolumenGrande(dto.getTarifaBaseVolumenGrande());
        tarifa.setCargoGestionPorTramo(dto.getCargoGestionPorTramo());
        tarifa.setFechaActualizacion(LocalDateTime.now());

        Tarifa updatedTarifa = tarifaRepository.save(tarifa);
        return convertToDTO(updatedTarifa);
    }

    public void eliminarTarifa(Long id) {
        Tarifa tarifa = tarifaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada con ID: " + id));

        tarifa.setActivo(false);
        tarifa.setFechaActualizacion(LocalDateTime.now());
        tarifaRepository.save(tarifa);
    }

    public TarifaDTO obtenerPorTipoTramo(String tipoTramo) {
        Tarifa tarifa = tarifaRepository.findByTipoTramoAndActivoTrue(tipoTramo)
                .orElseThrow(() -> new RuntimeException("Tarifa no encontrada para tipo de tramo: " + tipoTramo));
        return convertToDTO(tarifa);
    }

    private TarifaDTO convertToDTO(Tarifa tarifa) {
        TarifaDTO dto = new TarifaDTO();
        dto.setId(tarifa.getId());
        dto.setTipoTramo(tarifa.getTipoTramo());
        dto.setCostoPorKm(tarifa.getCostoPorKm());
        dto.setGestionFija(tarifa.getGestionFija());
        dto.setConsumoCombustiblePorKm(tarifa.getConsumoCombustiblePorKm());
        dto.setPrecioCombustiblePorLitro(tarifa.getPrecioCombustiblePorLitro());
        dto.setTarifaEstadiaDepositoPorDia(tarifa.getTarifaEstadiaDepositoPorDia());
        dto.setTarifaBasePesoLigero(tarifa.getTarifaBasePesoLigero());
        dto.setTarifaBasePesoMedio(tarifa.getTarifaBasePesoMedio());
        dto.setTarifaBasePesoPesado(tarifa.getTarifaBasePesoPesado());
        dto.setTarifaBaseVolumenPequeno(tarifa.getTarifaBaseVolumenPequeno());
        dto.setTarifaBaseVolumenMediano(tarifa.getTarifaBaseVolumenMediano());
        dto.setTarifaBaseVolumenGrande(tarifa.getTarifaBaseVolumenGrande());
        dto.setCargoGestionPorTramo(tarifa.getCargoGestionPorTramo());
        dto.setFechaCreacion(tarifa.getFechaCreacion());
        dto.setFechaActualizacion(tarifa.getFechaActualizacion());
        dto.setActivo(tarifa.getActivo());
        return dto;
    }

    private Tarifa convertToEntity(TarifaDTO dto) {
        Tarifa tarifa = new Tarifa();
        tarifa.setTipoTramo(dto.getTipoTramo());
        tarifa.setCostoPorKm(dto.getCostoPorKm());
        tarifa.setGestionFija(dto.getGestionFija());
        tarifa.setConsumoCombustiblePorKm(dto.getConsumoCombustiblePorKm());
        tarifa.setPrecioCombustiblePorLitro(dto.getPrecioCombustiblePorLitro());
        tarifa.setTarifaEstadiaDepositoPorDia(dto.getTarifaEstadiaDepositoPorDia());
        tarifa.setTarifaBasePesoLigero(dto.getTarifaBasePesoLigero());
        tarifa.setTarifaBasePesoMedio(dto.getTarifaBasePesoMedio());
        tarifa.setTarifaBasePesoPesado(dto.getTarifaBasePesoPesado());
        tarifa.setTarifaBaseVolumenPequeno(dto.getTarifaBaseVolumenPequeno());
        tarifa.setTarifaBaseVolumenMediano(dto.getTarifaBaseVolumenMediano());
        tarifa.setTarifaBaseVolumenGrande(dto.getTarifaBaseVolumenGrande());
        tarifa.setCargoGestionPorTramo(dto.getCargoGestionPorTramo());
        return tarifa;
    }
}
