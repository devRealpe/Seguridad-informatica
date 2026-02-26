package com.unimar.planes_de_trabajo.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
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
@Table(name = "pt_planes_de_trabajo", schema = "pt", indexes = {
        @Index(name = "idx_pdt_approved", columnList = "estado") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class PlanDeTrabajo implements ModeloGenerico {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    @JsonView(Views.Simple.class)
    private UUID id;

    @Column(name = "id_facultad", nullable = false)
    @JsonView(Views.Detailed.class)
    private String idFacultad;

    @Column(name = "id_decano", nullable = false)
    @JsonView(Views.Detailed.class)
    private String idDecano;

    @Column(name = "id_programa", nullable = false)
    @JsonView(Views.Detailed.class)
    private String idPrograma;

    @Column(name = "id_director", nullable = false)
    @JsonView(Views.Detailed.class)
    private String idDirector;

    @Column(name = "id_profesor", nullable = false)
    @JsonView(Views.Detailed.class)
    private String idProfesor;

    @Column(name = "es_director", nullable = false)
    @JsonView(Views.Detailed.class)
    private Boolean esDirector;

    @Column(name = "enviado_profesor", nullable = false)
    @JsonView(Views.Detailed.class)
    private Boolean enviadoProfesor;

    @Column(name = "firma_profesor", nullable = false)
    @JsonView(Views.Detailed.class)
    private Boolean firmaProfesor;

    @Column(name = "firma_director", nullable = false)
    @JsonView(Views.Detailed.class)
    private Boolean firmaDirector;

    @Column(name = "firma_decano", nullable = false)
    @JsonView(Views.Detailed.class)
    private Boolean firmaDecano;

    @Column(name = "rechazado", nullable = false)
    @JsonView(Views.Detailed.class)
    private Boolean rechazado;

    @Column(name = "motivo_rechazo", columnDefinition = "TEXT")
    @JsonView(Views.Detailed.class)
    private String motivoRechazo;

    @Column(name = "anio", nullable = false)
    @JsonView(Views.Detailed.class)
    private BigDecimal anio;

    @Column(name = "periodo", nullable = false)
    @JsonView(Views.Detailed.class)
    private BigDecimal periodo;

    @Column(name = "estado", nullable = false)
    @JsonView(Views.Detailed.class)
    private String estado;

    @Column(name = "fecha_creacion", nullable = false)
    @CreationTimestamp
    @JsonView(Views.Detailed.class)
    private LocalDateTime fechaCreacion;

    @Column(name = "novedades_activas", nullable = false)
    @JsonView(Views.Detailed.class)
    private Boolean novedadesActivas = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_plantilla")
    @JsonView(Views.Detailed.class)
    private Plantilla plantilla;

    @OneToMany(mappedBy = "planDeTrabajo", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    @JsonIgnoreProperties({ "planDeTrabajo", "hibernateLazyInitializer", "handler" })
    private List<ActividadesPlanDeTrabajo> actividades_pt = new ArrayList<>();

    @OneToMany(mappedBy = "pt", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    @JsonIgnoreProperties({ "pt", "hibernateLazyInitializer", "handler" })
    private List<InvestigacionExtension> extensionInvestigacionPt = new ArrayList<>();

    @OneToMany(mappedBy = "planDeTrabajo", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    @JsonIgnoreProperties({ "planDeTrabajo", "hibernateLazyInitializer", "handler" })
    private List<Novedad> novedades = new ArrayList<>();

    @OneToMany(mappedBy = "idPt", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    @JsonIgnoreProperties({ "planDeTrabajo", "hibernateLazyInitializer", "handler" })
    private List<AuditoriaReportes> auditoriaReportes = new ArrayList<>();
}