package com.unimar.planes_de_trabajo.repositories;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.unimar.planes_de_trabajo.models.Asesorias;

@Repository
public interface AsesoriasRepository extends JpaRepository<Asesorias,UUID>{
}