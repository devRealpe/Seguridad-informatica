package com.unimar.planes_de_trabajo.services;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.unimar.planes_de_trabajo.models.InvestigacionExtension;
import com.unimar.planes_de_trabajo.models.ProductoEsperado;
import com.unimar.planes_de_trabajo.models.TipoProducto;
import com.unimar.planes_de_trabajo.repositories.InvestigacionExtensionRepository;
import com.unimar.planes_de_trabajo.repositories.ProductoEsperadoRepository;
import com.unimar.planes_de_trabajo.repositories.TipoProductoRepository;

@Service
public class ProductoEsperadoService {

    @Autowired
    private ProductoEsperadoRepository productoEsperadoRepository;
    
    @Autowired
    private TipoProductoRepository tipoProductoRepository;
    
    @Autowired
    private InvestigacionExtensionRepository investigacionExtensionRepository;
    
    @Transactional
    public ProductoEsperado crear(String nombre, UUID idTipoProducto, UUID idInvestigacionExtension) {

        TipoProducto tipoProducto = tipoProductoRepository.findById(idTipoProducto)
            .orElseThrow(() -> new RuntimeException(
                "Tipo de producto no encontrado con ID: " + idTipoProducto));

        InvestigacionExtension investigacion = investigacionExtensionRepository.findById(idInvestigacionExtension)
            .orElseThrow(() -> new RuntimeException(
                "Investigación/Extensión no encontrada con ID: " + idInvestigacionExtension));
        
        ProductoEsperado nuevoProducto = new ProductoEsperado();
        nuevoProducto.setNombre(nombre);
        nuevoProducto.setTipoProducto(tipoProducto);
        nuevoProducto.setInvestigacion_extension(investigacion);
        
        ProductoEsperado saved = productoEsperadoRepository.save(nuevoProducto);
        productoEsperadoRepository.flush();
        
        return productoEsperadoRepository.findById(saved.getId())
            .orElseThrow(() -> new RuntimeException("Error al recuperar el producto esperado creado"));
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!productoEsperadoRepository.existsById(id)) {
            throw new RuntimeException(
                "Investigacion extension no encontrada con ID: " + id);
        }
        productoEsperadoRepository.deleteById(id);
    }
}