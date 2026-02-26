package com.unimar.planes_de_trabajo.services;

import com.unimar.planes_de_trabajo.models.ExcelRowData;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class ExcelGeneratorService {

    private static final String[] HEADERS = {
        "EMPRESA", "EMPLEADO", "CONCEPTO", "SECUENCIA", "PERIODO",
        "CENTRO_COSTO", "ORGANIZACION", "ESTADO", "ORDEN_PERIODO",
        "CANTIDAD_FIJO", "FONDO", "FUENTE_FUNCION", "OBSERVACIONES", "CATEGORIA"
    };

    public byte[] generateExcel(List<ExcelRowData> rows) {
        try (SXSSFWorkbook workbook = new SXSSFWorkbook(2000);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream(rows.size() * 256)) {

            workbook.setCompressTempFiles(true);
            Sheet sheet = workbook.createSheet("Profesores");

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < HEADERS.length; i++) {
                headerRow.createCell(i).setCellValue(HEADERS[i]);
            }

            int rowNum = 1;
            for (ExcelRowData rowData : rows) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowData.getEmpresa());
                row.createCell(1).setCellValue(rowData.getEmpleado());
                row.createCell(2).setCellValue(rowData.getConcepto());
                row.createCell(3).setCellValue(rowData.getSecuencia());
                row.createCell(4).setCellValue(rowData.getPeriodo());
                row.createCell(5).setCellValue(rowData.getCentroCosto());
                row.createCell(6).setCellValue(rowData.getOrganizacion());
                row.createCell(7).setCellValue(rowData.getEstado());
                row.createCell(8).setCellValue(rowData.getOrdenPeriodo());
                row.createCell(9).setCellValue(rowData.getCantidadFijo() != null ? rowData.getCantidadFijo().doubleValue() : 0.0);
                row.createCell(10).setCellValue(rowData.getFondo());
                row.createCell(11).setCellValue(rowData.getFuenteFuncion());
                row.createCell(12).setCellValue(rowData.getObservaciones());
                row.createCell(13).setCellValue(rowData.getCategoria());
            }

            workbook.write(outputStream);
            workbook.dispose();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error al generar el archivo Excel", e);
        }
    }
}