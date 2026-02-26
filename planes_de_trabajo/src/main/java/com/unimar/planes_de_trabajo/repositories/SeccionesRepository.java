package com.unimar.planes_de_trabajo.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unimar.planes_de_trabajo.models.Secciones;

public interface SeccionesRepository extends JpaRepository<Secciones,UUID>{
    
     List<Secciones> findByPlantillasIdAndEsPadre(UUID plantillaId, boolean esPadre);
     List <Secciones> findBySeccionCursos(boolean seccionCursos);
}
