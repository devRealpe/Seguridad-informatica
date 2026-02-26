package com.unimar.planes_de_trabajo.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unimar.planes_de_trabajo.models.GrupoInvestigacion;
import com.unimar.planes_de_trabajo.repositories.GrupoInvestigacionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GrupoInvestigacionService {
    
    @Autowired
    private GrupoInvestigacionRepository grupoInvestigacionRepository;

    @Transactional(readOnly = true)
    public List<GrupoInvestigacion> getAll(){
        return grupoInvestigacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<GrupoInvestigacion> getById(UUID id){
        return grupoInvestigacionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<GrupoInvestigacion> getGruposByFac(String facultad){
        return grupoInvestigacionRepository.findByFacultad(facultad);
    }

}