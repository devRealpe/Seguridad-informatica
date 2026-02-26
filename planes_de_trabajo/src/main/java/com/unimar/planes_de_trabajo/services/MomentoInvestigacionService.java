package com.unimar.planes_de_trabajo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unimar.planes_de_trabajo.models.MomentoInvestigacion;
import com.unimar.planes_de_trabajo.repositories.MomentoInvestigacionRepository;

@Service
public class MomentoInvestigacionService {

    @Autowired
    private MomentoInvestigacionRepository momentoInvestigacionRepository;

    public List<MomentoInvestigacion> getAll(){
        return momentoInvestigacionRepository.findAll();
    }
    
}