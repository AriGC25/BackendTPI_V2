package com.transportista.logistica.repository;

import com.transportista.logistica.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CamionRepository extends JpaRepository<Camion, Long> {
    
    Optional<Camion> findByPatente(String patente);
    
    List<Camion> findAllByDisponibleTrueAndActivoTrue();
    
    List<Camion> findAllByTransportista(String transportista);
    
    boolean existsByPatente(String patente);
}
