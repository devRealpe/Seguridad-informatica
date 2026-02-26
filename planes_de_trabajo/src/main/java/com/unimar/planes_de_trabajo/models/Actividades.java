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
@Table(name = "pt_actividades", schema = "pt", indexes = {
        @Index(name = "idx_act_seccion", columnList = "id_seccion") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Actividades {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name="id", nullable = false, updatable = false)
    @JsonView(Views.Simple.class)
    private UUID id;

    @Column(name = "nombre", nullable = false)
    @JsonView(Views.Simple.class)
    private String nombre;

    @Column(name = "tiene_asesorias", nullable = false)
    @JsonView(Views.Simple.class)
    private Boolean tieneAsesorias;

    @Column(name = "tiene_descripcion", nullable = false)
    @JsonView(Views.Simple.class)
    private Boolean tieneDescripcion;

    @Column(name = "horas_maximas", nullable = false)
    @JsonView(Views.Simple.class)
    private BigDecimal horasMaximas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seccion")
    @JsonIgnoreProperties({ "actividades", "investigacionesExtension", "plantillas", "padre", "hijos", "esPadre",
            "seccionCursos", "seccion_investigativa", "hibernateLazyInitializer", "handler" })
    @JsonView(Views.WithSeccion.class)
    private Secciones secciones;

    @OneToMany(mappedBy = "actividades", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnoreProperties({ "actividades", "planDeTrabajo", "hibernateLazyInitializer", "handler" })
    @JsonIgnore
    private List<ActividadesPlanDeTrabajo> actividades_pt = new ArrayList<>();
}