package com.unimar.planes_de_trabajo.repositories;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unimar.planes_de_trabajo.models.PlanDeTrabajo;

public interface PlanDeTrabajoRepository extends JpaRepository<PlanDeTrabajo,UUID>{
    List<PlanDeTrabajo> findByIdProfesorOrderByFechaCreacionDesc(String idProfesor);
    Optional<PlanDeTrabajo> findByIdProfesorAndAnioAndPeriodo(String idProfesor, BigDecimal anio, BigDecimal periodo);
    Optional<PlanDeTrabajo> findByIdProfesor(String idProfesor);
    List<PlanDeTrabajo> findByAnioAndPeriodoAndEstado(Integer anio, Integer periodo, String estado);
}

