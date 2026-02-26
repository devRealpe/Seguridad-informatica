package com.unimar.planes_de_trabajo.services;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.unimar.planes_de_trabajo.dto.ResumenPtExportacionDTO;
import com.unimar.planes_de_trabajo.models.VistaResumenPtAprobado;
import com.unimar.planes_de_trabajo.repositories.VistaResumenPtAprobadoRepository;

@Service
public class ExportacionPtService {

    @Autowired
    private VistaResumenPtAprobadoRepository vistaRepository;

    @Transactional(readOnly = true)
    public List<ResumenPtExportacionDTO> obtenerResumenPorIdsPlanTrabajo(List<UUID> idsPlanTrabajo) {
        List<VistaResumenPtAprobado> resultados = vistaRepository.findByIdPlanTrabajoIn(idsPlanTrabajo);
        return resultados.stream()
            .map(v -> new ResumenPtExportacionDTO(
                v.getIdPlanTrabajo(),
                v.getPrograma(),
                v.getSeccionPadre(),
                v.getSeccion(),
                v.getHorasTotales()
            ))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResumenPtExportacionDTO> obtenerResumenPorPlanTrabajo(UUID idPlanTrabajo) {
        return obtenerResumenPorIdsPlanTrabajo(List.of(idPlanTrabajo));
    }
}