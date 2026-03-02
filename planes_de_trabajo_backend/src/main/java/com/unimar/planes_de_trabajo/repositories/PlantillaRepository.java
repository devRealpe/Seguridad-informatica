package com.unimar.planes_de_trabajo.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unimar.planes_de_trabajo.models.Plantilla;

public interface PlantillaRepository extends JpaRepository<Plantilla,UUID>{
    List<Plantilla> findByEstado(Boolean estado);
}
