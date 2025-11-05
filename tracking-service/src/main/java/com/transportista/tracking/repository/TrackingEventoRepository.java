package com.transportista.tracking.repository;

import com.transportista.tracking.entity.TrackingEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackingEventoRepository extends JpaRepository<TrackingEvento, Long> {
    
    List<TrackingEvento> findByContenedorIdOrderByFechaEventoDesc(Long contenedorId);
    
    List<TrackingEvento> findBySolicitudIdOrderByFechaEventoDesc(Long solicitudId);
}
