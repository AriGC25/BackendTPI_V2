package com.transportista.solicitudes.repository;

import com.transportista.solicitudes.entity.Tramo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TramoRepository extends JpaRepository<Tramo, Long> {
    
    List<Tramo> findByTransportistaId(String transportistaId);

    List<Tramo> findByEstado(String estado);
    
    List<Tramo> findByTransportistaIdAndEstado(String transportistaId, String estado);

    @Query("SELECT t FROM Tramo t WHERE t.transportistaId = :transportistaId AND t.estado IN ('ASIGNADO', 'EN_CURSO')")
    List<Tramo> findTramosActivosByTransportista(@Param("transportistaId") String transportistaId);

    List<Tramo> findByRutaIdOrderByOrdenTramo(Long rutaId);

    List<Tramo> findByCamionId(Long camionId);
}
