package com.unimar.planes_de_trabajo.models;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pt_secciones", schema = "pt", indexes = { @Index(name = "idx_secciones_id", columnList = "id") })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Secciones {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    @JsonView(Views.Simple.class)
    private UUID id;

    @Column(name = "nombre", nullable = false)
    @JsonView(Views.Simple.class)
    private String nombre;

    @Column(name = "concepto", nullable = true)
    @JsonView(Views.Simple.class)
    private String concepto;

    @Column(name = "es_padre", nullable = false)
    @JsonView(Views.Detailed.class)
    private boolean esPadre;

    @Column(name = "tiene_seccion_cursos", nullable = false)
    @JsonView(Views.Detailed.class)
    private boolean seccionCursos;

    @Column(name = "tiene_seccion_investigativa", nullable = false)
    @JsonView(Views.Detailed.class)
    private boolean seccionInvestigativa;

    @OneToMany(mappedBy = "secciones", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("actividades_secciones")
    @JsonView(Views.Detailed.class)
    private List<Actividades> actividades;

    @OneToMany(mappedBy = "seccion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<InvestigacionExtension> investigacionesExtension;

    @ManyToMany
    @JoinTable(name = "pt_secciones_plantilla", schema = "pt", joinColumns = @JoinColumn(name = "id_seccion"), inverseJoinColumns = @JoinColumn(name = "id_plantilla"))
    @JsonBackReference("plantilla_secciones_ref")
    @JsonIgnore
    private List<Plantilla> plantillas;

    @ManyToOne
    @JoinColumn(name = "id_seccion_padre", nullable = true)
    @JsonBackReference("pt_secciones_ref")
    @JsonIgnore
    private Secciones padre;

    @OneToMany(mappedBy = "padre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("pt_secciones_ref")
    @JsonView(Views.Detailed.class)
    private List<Secciones> hijos;
}