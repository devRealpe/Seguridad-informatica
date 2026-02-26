package com.unimar.planes_de_trabajo.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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
@Table(name = "pt_investigacion_extension", schema = "pt", indexes = {
        @Index(name = "idx_investigacionlookup", columnList = "id_pt, id_grupo, id_momento, id_seccion") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class InvestigacionExtension {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "codigo", nullable = false)
    private String codigo;

    @Column(name = "nombre_proyecto", nullable = false, columnDefinition = "TEXT")
    private String nombreProyecto;

    @Column(name = "horas", nullable = false)
    private BigDecimal horas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pt")
    @JsonIgnore
    private PlanDeTrabajo pt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_grupo")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
    private GrupoInvestigacion grupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_momento")
    @JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "investigaciones", "asesorias" })
    private MomentoInvestigacion momentoInvestigacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seccion")
    @JsonIgnoreProperties({ "actividades", "investigacionesExtension", "plantillas", "padre", "hijos", "esPadre",
            "seccionCursos", "seccion_investigativa", "hibernateLazyInitializer", "handler" })
    private Secciones seccion;

    @OneToMany(mappedBy = "investigacion_extension", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("productosRef")
    private List<ProductoEsperado> productos;

}