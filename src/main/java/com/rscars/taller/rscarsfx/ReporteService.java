package com.rscars.taller.rscarsfx;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import java.awt.Desktop;
import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.kernel.colors.ColorConstants;

public class ReporteService {

    // --- MÉTODO MODIFICADO ---
    public static void generarReporteCitas() {
        try {
            // 1. Crear un archivo temporal para el reporte
            File file = File.createTempFile("ReporteCitas_", ".pdf");
            System.out.println("Creando reporte temporal en: " + file.getAbsolutePath());

            // 2. Crear el documento PDF (el resto del código iText es similar)
            PdfWriter writer = new PdfWriter(file);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 3. Añadir contenido mejorado
            // Título
            Paragraph titulo = new Paragraph("Reporte General de Citas - RsCars")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(20);
            document.add(titulo);

            // Fecha de generación
            document.add(new Paragraph("Fecha de generación: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).setTextAlignment(TextAlignment.CENTER).setFontSize(9).setItalic());

            // Tabla de datos
            Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 3, 3, 2}));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(20);

            // Encabezados de la tabla con estilo
            table.addHeaderCell(new Cell().add(new Paragraph("ID").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Fecha").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Cliente").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Vehículo").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            table.addHeaderCell(new Cell().add(new Paragraph("Estado").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            // 4. Obtener datos de la BD
            String sql = "SELECT c.idCita, c.fechaHora, c.estado, cl.nombre, cl.apellido, v.marca, v.modelo " +
                    "FROM tbCitas c " +
                    "LEFT JOIN tbVehiculos v ON c.idVehiculo = v.idVehiculo " +
                    "LEFT JOIN tbClientes cl ON v.idCliente = cl.idCliente " +
                    "ORDER BY c.fechaHora DESC";
            Connection cnx = ConexionDB.obtenerInstancia().getCnx();
            try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    table.addCell(String.valueOf(rs.getInt("idCita")));
                    table.addCell(rs.getDate("fechaHora").toString());
                    table.addCell(rs.getString("nombre").trim() + " " + rs.getString("apellido").trim());
                    table.addCell(rs.getString("marca").trim() + " " + rs.getString("modelo").trim());
                    table.addCell(rs.getString("estado").trim());
                }
            }

            document.add(table);
            document.close();

            // 5. Abrir el archivo generado en el visor de PDF del sistema
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Aquí podrías mostrar una alerta de error al usuario
        }
    }
}