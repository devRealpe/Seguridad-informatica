package com.unimar.planes_de_trabajo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExcelRowData {
    private String empresa;
    private String empleado;
    private String concepto;
    private String secuencia;
    private String periodo;
    private String centroCosto;
    private String organizacion;
    private String estado;
    private String ordenPeriodo;
    private BigDecimal cantidadFijo;
    private String fondo;
    private String fuenteFuncion;
    private String observaciones;
    private String categoria;
    private int profesorOrder;
    private int conceptoNum;
}
