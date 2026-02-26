package com.unimar.planes_de_trabajo.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unimar.planes_de_trabajo.models.ActividadesPlanDeTrabajo;

@Repository
public interface ActividadesPTRepository extends JpaRepository<ActividadesPlanDeTrabajo, UUID>{
    
    @EntityGraph(attributePaths = {"actividades", "actividades.secciones", "asesorias", "asesorias.momento_asesoria", "planDeTrabajo"})
    List<ActividadesPlanDeTrabajo> findByPlanDeTrabajoId(UUID id_pt);

    @EntityGraph(attributePaths = {"actividades", "actividades.secciones", "asesorias", "asesorias.momento_asesoria", "planDeTrabajo"})
    List<ActividadesPlanDeTrabajo> findByActividadesId(UUID actividadId);
    
}