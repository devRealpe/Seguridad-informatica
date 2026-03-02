package com.unimar.planes_de_trabajo.models;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformExcelId implements Serializable {

    private UUID id;
    private String concepto;
}