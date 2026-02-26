package com.unimar.planes_de_trabajo.models;

import java.util.List;
import java.util.UUID;

import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "pt_tipos_producto", schema = "pt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class TipoProducto {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", nullable = false,  columnDefinition = "TEXT")
    private String descripcion;

    @OneToMany(mappedBy = "tipoProducto", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference("tipoProductoRef")
    @JsonIgnore
    private List<ProductoEsperado> productos;

    @ManyToOne
    @JoinColumn(name = "id_padre")
    @JsonBackReference("tipoProductoFirst")
    private TipoProducto padre;

    @OneToMany(mappedBy = "padre", cascade = CascadeType.ALL, fetch = FetchType.LAZY )
    @JsonManagedReference("tipoProductoFirst")
    private List<TipoProducto> hijos;

}