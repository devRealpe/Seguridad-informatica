package com.unimar.planes_de_trabajo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfesorRequest {
    private String numIdentificacion;
    private String tipoIdentificacion;
    private String nombres;
    private String apellidos;
    private String nivelEducativo;
    private String escalafon;
    private String dedicacion;
    private String tipoDedicacion;
    private String vinculacion;
    private String empresa;
    private String empleado;
    private String centroCosto;
    private String organizacion;
    private String nomOrganizacion;
    private String estado;
    private String fondo;
    private String categoria;
    private String facultad;
    private String programa;
    private String cargo;
}
