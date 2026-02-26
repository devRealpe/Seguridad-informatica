package com.unimar.planes_de_trabajo.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.unimar.planes_de_trabajo.models.TipoProducto;
import com.unimar.planes_de_trabajo.repositories.TipoProductoRepository;

@Service
public class TipoProductoService {

    @Autowired
    private TipoProductoRepository tipoProductoRepository;

    public List<TipoProducto> getAll(){
        return tipoProductoRepository.findByHijosIsNotEmpty();
    }
    
}