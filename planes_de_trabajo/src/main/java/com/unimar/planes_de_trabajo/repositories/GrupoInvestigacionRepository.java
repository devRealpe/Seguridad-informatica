package com.unimar.planes_de_trabajo.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unimar.planes_de_trabajo.models.GrupoInvestigacion;

@Repository
public interface GrupoInvestigacionRepository extends JpaRepository<GrupoInvestigacion,UUID>{
    List<GrupoInvestigacion> findByFacultad(String facultad);
}