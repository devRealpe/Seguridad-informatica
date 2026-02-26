package com.unimar.planes_de_trabajo.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UuidGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name="pt_auditoria_reportes", schema = "pt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AuditoriaReportes {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name="id", nullable = false, updatable = false)
    private UUID id;    

    @Column(name="tipo_cambio", nullable = false)
    private String tipoCambio;

    @Column(name="accion", nullable = false, columnDefinition = "TEXT")
    private String accion;

    @Column(name="fecha", nullable = false)
    @CreationTimestamp
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pt")
    @JsonIgnore
    private PlanDeTrabajo idPt;

}