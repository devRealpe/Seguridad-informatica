package com.unimar.planes_de_trabajo.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.unimar.planes_de_trabajo.models.ProductoEsperado;

public interface ProductoEsperadoRepository extends JpaRepository<ProductoEsperado,UUID>{
    
}