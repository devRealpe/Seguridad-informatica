package com.unimar.planes_de_trabajo.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unimar.planes_de_trabajo.models.Novedad;
import com.unimar.planes_de_trabajo.models.PlanDeTrabajo;

public interface NovedadRepository extends JpaRepository<Novedad, UUID> {
    
    List<Novedad> findByPlanDeTrabajo_IdOrderByFechaRegistroDesc(UUID planDeTrabajoId);

    List<Novedad> findByPlanDeTrabajo_IdAndEstadoOrderByFechaRegistroDesc(UUID planDeTrabajoId, String estado);
    Long countByPlanDeTrabajo_IdAndEstado(UUID planDeTrabajoId, String estado);
    List<Novedad> findByEstadoOrderByFechaRegistroDesc(String estado);
    List<Novedad> findAllByOrderByFechaRegistroDesc();
    Optional<Novedad> findByplanDeTrabajoAndEstado(Optional<PlanDeTrabajo> planDeTrabajoId, String estado);
}

