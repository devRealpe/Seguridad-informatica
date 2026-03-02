package com.unimar.planes_de_trabajo.repositories;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.unimar.planes_de_trabajo.models.VistaResumenPtAprobado;

@Repository
public interface VistaResumenPtAprobadoRepository extends JpaRepository<VistaResumenPtAprobado, UUID> {
    List<VistaResumenPtAprobado> findByIdPlanTrabajo(UUID idPlanTrabajo);
    List<VistaResumenPtAprobado> findByIdPlanTrabajoIn(List<UUID> ids);
}