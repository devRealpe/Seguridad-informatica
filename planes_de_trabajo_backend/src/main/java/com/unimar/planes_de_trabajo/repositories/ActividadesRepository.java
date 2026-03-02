package com.unimar.planes_de_trabajo.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unimar.planes_de_trabajo.models.Actividades;

public interface ActividadesRepository extends JpaRepository<Actividades,UUID>{
    List<Actividades> findBySeccionesId(UUID seccionId);
}
