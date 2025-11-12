package com.transportista.tarifas.repository;

import com.transportista.tarifas.entity.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
    
    List<Tarifa> findByActivoTrue();

    Optional<Tarifa> findByTipoTramoAndActivoTrue(String tipoTramo);
    
    List<Tarifa> findByTipoTramo(String tipoTramo);

    // Nuevo método para obtener la tarifa activa más reciente
    Optional<Tarifa> findFirstByActivoTrueOrderByIdDesc();
}
