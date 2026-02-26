package com.unimar.planes_de_trabajo.models;

import java.math.BigDecimal;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pt_consolidado_pdf", schema = "pt") 
@Getter
@Setter
@NoArgsConstructor
public class VistaResumenPtAprobado {

    @Id
    @Column(name = "id")
    private UUID idPlanTrabajo;

    @Column(name = "programa")
    private String programa;

    @Column(name = "seccion_padre")
    private String seccionPadre;

    @Column(name = "seccion")
    private String seccion;

    @Column(name = "horas_totales")
    private BigDecimal horasTotales;
}