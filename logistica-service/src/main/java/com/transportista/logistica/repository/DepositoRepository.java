package com.transportista.logistica.repository;

import com.transportista.logistica.entity.Deposito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositoRepository extends JpaRepository<Deposito, Long> {
    
    Optional<Deposito> findByNombre(String nombre);
    
    List<Deposito> findAllByActivoTrue();
}
