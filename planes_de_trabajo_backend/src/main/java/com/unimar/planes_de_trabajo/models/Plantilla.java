package com.unimar.planes_de_trabajo.models;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity
@Table(name="pt_plantilla", schema = "pt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Plantilla {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name="id", nullable = false, updatable = false)
    private UUID id;

    @Column(name="nombre", nullable = false)
    private String nombre;

    @Column(name="estado", nullable = false)
    private boolean estado;

    @OneToMany(mappedBy = "plantilla", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("plantilla_pt_ref")
    @JsonIgnoreProperties({"plantilla", "actividades_pt", "hibernateLazyInitializer", "handler"})    
    @JsonIgnore
    private List<PlanDeTrabajo> planes_de_trabajo; 
    
    @ManyToMany(mappedBy = "plantillas", fetch = FetchType.LAZY)
    @JsonManagedReference("plantilla_secciones_ref")
    @JsonIgnoreProperties({"plantillas", "actividades", "hibernateLazyInitializer", "handler"})
    @JsonIgnore
    private List<Secciones> secciones;
}