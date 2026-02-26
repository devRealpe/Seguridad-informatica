package com.unimar.planes_de_trabajo.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unimar.planes_de_trabajo.models.InformExcel;
import com.unimar.planes_de_trabajo.models.InformExcelId;

@Repository
public interface InformExcelRepository extends JpaRepository<InformExcel, InformExcelId>{
    
    List<InformExcel> findByPeriodo(String periodo);
    List<InformExcel> findByProgramaAndPeriodo(String programa, String periodo);
    List<InformExcel> findByFacultadAndPeriodo(String facultad, String periodo);
}