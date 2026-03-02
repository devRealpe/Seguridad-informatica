package com.unimar.planes_de_trabajo.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

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
@Table(name = "pt_grupo_investigacion", schema = "pt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GrupoInvestigacion implements ModeloGenerico{

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "sigla", nullable = false)
    private String sigla;

    @Column(name="fecha_creacion", nullable = false)
    @CreationTimestamp 
    private LocalDateTime fechaCreacion;

    @Column(name = "ultima_categoria_minciencias", nullable = false)
    private String ultimaCategoriaMinciencias;

    @Column(name = "codigo", nullable = false)
    private String codigo;

    @Column(name = "facultad", nullable = false)
    private String facultad;
 
    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("grupoInvestigacionRef")
    @JsonIgnore
    private List<InvestigacionExtension> investigaciones;

}
