package com.unimar.planes_de_trabajo.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unimar.planes_de_trabajo.models.InvestigacionExtension;

@Repository
public interface InvestigacionExtensionRepository extends JpaRepository<InvestigacionExtension, UUID> {
    
    @EntityGraph(attributePaths = {"grupo", "momentoInvestigacion", "productos", "productos.tipoProducto"})
    @Override
    List<InvestigacionExtension> findAll();

    @EntityGraph(attributePaths = {"grupo", "momentoInvestigacion", "productos", "productos.tipoProducto"})
    List<InvestigacionExtension> findByPtIdAndSeccionId(UUID ptId, UUID SeccionId);

    @EntityGraph(attributePaths = {"grupo", "momentoInvestigacion", "productos", "productos.tipoProducto"})
    List<InvestigacionExtension> findByPtId(UUID ptId);
    
}