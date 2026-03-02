package com.unimar.planes_de_trabajo.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.unimar.planes_de_trabajo.views.Views;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pt_actividades_plan_de_trabajo", schema = "pt", indexes = {
        @Index(name = "idx_apdt_lookup", columnList = "id_actividades, id_pt") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class ActividadesPlanDeTrabajo implements ModeloGenerico {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    @JsonView(Views.Simple.class)
    private UUID id;

    @Column(name = "descripcion", nullable = true)
    @JsonView(Views.Simple.class)
    private String descripcion;

    @Column(name = "horas", nullable = false)
    @JsonView(Views.Simple.class)
    private BigDecimal horas;

    @Column(name = "numero_proyectos_jurado", nullable = true)
    @JsonView(Views.Simple.class)
    private BigDecimal numeroProyectosJurado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pt")
    @JsonIgnoreProperties({ "actividades_pt", "extensionInvestigacionPt", "novedades", "plantilla",
            "hibernateLazyInitializer", "handler" })
    @JsonView(Views.WithSeccion.class)
    private PlanDeTrabajo planDeTrabajo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividades")
    @JsonIgnoreProperties({ "actividades_pt", "hibernateLazyInitializer", "handler" })
    @JsonView(Views.Simple.class)
    private Actividades actividades;

    @OneToMany(mappedBy = "actividad_asesoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnoreProperties({ "actividad_asesoria", "hibernateLazyInitializer", "handler" })
    @JsonView(Views.Simple.class)
    private List<Asesorias> asesorias = new ArrayList<>();
}