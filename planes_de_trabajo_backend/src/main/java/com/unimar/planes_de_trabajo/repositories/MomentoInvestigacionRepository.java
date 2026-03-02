package com.unimar.planes_de_trabajo.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unimar.planes_de_trabajo.models.MomentoInvestigacion;

public interface MomentoInvestigacionRepository extends JpaRepository<MomentoInvestigacion,UUID>{
    
}