package com.unimar.planes_de_trabajo.models;

import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonView;
import com.unimar.planes_de_trabajo.views.Views;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="pt_asesorias", schema = "pt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Asesorias {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name="id", nullable = false, updatable = false)
    @JsonView(Views.Simple.class)
    private UUID id;

    @Column(name="titulo", nullable = false)
    @JsonView(Views.Simple.class)
    private String titulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_momento")
    @JsonIgnoreProperties({"asesorias", "investigaciones", "hibernateLazyInitializer", "handler"})
    @JsonView(Views.Simple.class)
    private MomentoInvestigacion momento_asesoria;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_actividad_pt")
    @JsonIgnoreProperties({"asesorias", "planDeTrabajo", "actividades", "hibernateLazyInitializer", "handler"})
    @JsonIgnore
    private ActividadesPlanDeTrabajo actividad_asesoria;
}