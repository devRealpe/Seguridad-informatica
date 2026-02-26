package com.unimar.planes_de_trabajo.models;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="pt_reporte_excel", schema = "pt")
@IdClass(InformExcelId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InformExcel {

    @Id
    @Column(name="id")
    private UUID id;

    @Column(name="periodo_academico")
    private String periodo;

    @Column(name="facultad")
    private String facultad;

    @Column(name="programa")
    private String programa;

    @Column(name="profesor")
    private String profesor;

    @Column(name="horas")
    private BigDecimal horas;

    @Id
    @Column(name="concepto")
    private String concepto;
}