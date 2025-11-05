package com.transportista.solicitudes.repository;

import com.transportista.solicitudes.entity.Contenedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContenedorRepository extends JpaRepository<Contenedor, Long> {
    
    List<Contenedor> findByClienteId(Long clienteId);
    
    List<Contenedor> findByEstado(String estado);
    
    @Query("SELECT c FROM Contenedor c WHERE c.estado = 'PENDIENTE'")
    List<Contenedor> findContenedoresPendientes();
}
