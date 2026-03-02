package com.unimar.planes_de_trabajo.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AsesoriasRequest {
    
    private String titulo;
    private UUID idActividadPT;
    private UUID idMomento;
}