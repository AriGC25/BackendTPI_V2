package com.transportista.tarifas.repository;

import com.transportista.tarifas.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    Optional<Cliente> findByEmail(String email);
    
    Optional<Cliente> findByDni(String dni);
    
    boolean existsByEmail(String email);
    
    boolean existsByDni(String dni);
}
