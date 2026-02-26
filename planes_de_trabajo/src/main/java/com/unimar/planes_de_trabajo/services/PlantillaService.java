package com.unimar.planes_de_trabajo.services;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unimar.planes_de_trabajo.models.Plantilla;
import com.unimar.planes_de_trabajo.repositories.PlantillaRepository;

@Service
public class PlantillaService {
    
    @Autowired
    private PlantillaRepository plantillaRepository;

    public List<Plantilla> getPlantillas(){
        return plantillaRepository.findAll();
    }

    public List<Plantilla> getPlantillasHabilitadas(){
        return plantillaRepository.findByEstado(true);
    }

    @Transactional
    public Plantilla crear(String nombre) {
        
        Plantilla nuevaPlantilla = new Plantilla();
        nuevaPlantilla.setNombre(nombre);
        
        Plantilla saved = plantillaRepository.save(nuevaPlantilla);
        plantillaRepository.flush(); 
        
        return plantillaRepository.findById(saved.getId())
            .orElseThrow(() -> new RuntimeException("Error al recuperar la plantilla creada"));
    }

    @Transactional 
    public Plantilla editarEstadoPlantilla(UUID id, boolean estado) {
        Plantilla plantilla = plantillaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Plantilla no encontrada con ID: " + id));
        plantilla.setEstado(estado);
        return plantillaRepository.save(plantilla);
    }
}
