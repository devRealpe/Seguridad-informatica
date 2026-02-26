package com.unimar.planes_de_trabajo.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unimar.planes_de_trabajo.models.AuditoriaReportes;

public interface AuditoriaReportesRepository extends JpaRepository<AuditoriaReportes,UUID>{
    List<AuditoriaReportes> findByIdPtIdOrderByFechaDesc(UUID seccionId);
}
