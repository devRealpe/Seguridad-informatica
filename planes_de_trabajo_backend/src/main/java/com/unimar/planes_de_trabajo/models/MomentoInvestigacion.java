package com.unimar.planes_de_trabajo.models;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import com.unimar.planes_de_trabajo.views.Views;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name ="pt_momento_investigacion", schema = "pt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MomentoInvestigacion {

    @Id
    @GeneratedValue
    @UuidGenerator 
    @Column(name = "id", nullable = false, updatable = false)
    @JsonView(Views.Simple.class)
    private UUID id;

    @Column(name = "nombre", nullable = false)
    @JsonView(Views.Simple.class)
    private String nombre; 

    @OneToMany(mappedBy = "momentoInvestigacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("momentoInvestigacionRef")
    @JsonIgnore
    private List<InvestigacionExtension> investigaciones;

    @OneToMany(mappedBy = "momento_asesoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("asesorias_momento")  
    @JsonIgnore
    private List<Asesorias> asesorias;
}
