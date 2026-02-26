package com.unimar.planes_de_trabajo.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unimar.planes_de_trabajo.models.InformExcel;
import com.unimar.planes_de_trabajo.repositories.InformExcelRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InformExcelService {
    
    @Autowired
    private InformExcelRepository informExcelRepository;

    @Transactional(readOnly = true)
    public List<InformExcel> getByPeriodo(String periodo){
        return informExcelRepository.findByPeriodo(periodo);
    }

    @Transactional(readOnly = true)
    public List<InformExcel> getByFacultad(String facultad, String periodo){
        return informExcelRepository.findByFacultadAndPeriodo(facultad, periodo);
    }

    @Transactional(readOnly = true)
    public List<InformExcel> getByPrograma(String programa, String periodo){
        return informExcelRepository.findByProgramaAndPeriodo(programa, periodo);
    }

}