package com.transportista.solicitudes.repository;

import com.transportista.solicitudes.entity.SeguimientoEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeguimientoEstadoRepository extends JpaRepository<SeguimientoEstado, Long> {
    List<SeguimientoEstado> findBySolicitudId(Long solicitudId);
}

