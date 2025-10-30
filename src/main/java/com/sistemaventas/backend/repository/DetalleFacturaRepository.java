package com.sistemaventas.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sistemaventas.backend.entity.DetalleFactura;

@Repository
public interface DetalleFacturaRepository extends JpaRepository<DetalleFactura, Integer> {
    
    /**
     * Busca todos los detalles de una factura específica
     */
    List<DetalleFactura> findByFactura_IdFactura(Integer idFactura);
}
