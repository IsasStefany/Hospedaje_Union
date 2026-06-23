package com.hospedaje.util;

import com.hospedaje.modelo.Huesped;
import com.hospedaje.modelo.Reserva;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import java.awt.Desktop;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BoletaGenerator {

    private static final DeviceRgb COLOR_OSCURO = new DeviceRgb(26, 26, 46);
    private static final DeviceRgb COLOR_GRIS   = new DeviceRgb(120, 120, 120);
    private static final DeviceRgb COLOR_VERDE  = new DeviceRgb(46, 125, 50);
    private static final DeviceRgb COLOR_AZUL   = new DeviceRgb(21, 101, 192);
    private static final DeviceRgb COLOR_YAPE   = new DeviceRgb(110, 37, 180);
    private static final DateTimeFormatter FMT  =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String generar(Reserva reserva, Huesped huesped, String nroBoleta) {
        String carpeta = System.getProperty("user.dir") + File.separator + "boletas";
        new File(carpeta).mkdirs();

        String nombreArchivo = "boleta_" + nroBoleta + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                + ".pdf";
        String rutaCompleta = carpeta + File.separator + nombreArchivo;

        try {
            PdfWriter   writer    = new PdfWriter(rutaCompleta);
            PdfDocument pdf       = new PdfDocument(writer);
            Document    documento = new Document(pdf);
            documento.setMargins(30, 40, 30, 40);

            // ── Encabezado ─────────────────────────────────────────────
            documento.add(new Paragraph("HOSPEDAJE TOCACHE")
                    .setFontSize(16).setBold()
                    .setFontColor(COLOR_OSCURO)
                    .setTextAlignment(TextAlignment.CENTER));

            documento.add(new Paragraph("Tocache, San Martin - Peru")
                    .setFontSize(10).setFontColor(COLOR_GRIS)
                    .setTextAlignment(TextAlignment.CENTER));

            documento.add(lineaDivisoria());

            documento.add(new Paragraph("BOLETA DE VENTA N " + nroBoleta)
                    .setFontSize(12).setBold()
                    .setFontColor(COLOR_OSCURO)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(8));

            documento.add(new Paragraph("Fecha de emision: "
                    + LocalDateTime.now().format(FMT))
                    .setFontSize(9).setFontColor(COLOR_GRIS)
                    .setTextAlignment(TextAlignment.CENTER));

            documento.add(lineaDivisoria());

            // ── Datos del huesped ──────────────────────────────────────
            Table tabla = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(10);

            agregarFila(tabla, "Cliente:",     huesped.getNombreCompleto());
            agregarFila(tabla, "DNI:",         huesped.getDni());
            agregarFila(tabla, "Procedencia:", huesped.getProcedencia() != null
                    ? huesped.getProcedencia() : "-");
            agregarFila(tabla, "Habitacion:",  reserva.getNumeroHabitacion()
                    + " - " + reserva.getTipoHabitacion());
            agregarFila(tabla, "Entrada:",     reserva.getFechaEntradaStr());
            agregarFila(tabla, "Vence:",       reserva.getFechaVencimientoStr());
            agregarFila(tabla, "Concepto:",    "Alojamiento");
            documento.add(tabla);

            documento.add(lineaDivisoria());

            // ── Metodo de pago ─────────────────────────────────────────
            String metodo = reserva.getMetodoPago() != null
                    ? reserva.getMetodoPago() : "EFECTIVO";

            DeviceRgb colorMetodo = switch (metodo) {
                case "YAPE" -> COLOR_YAPE;
                case "VISA", "BCP" -> COLOR_AZUL;
                default -> COLOR_OSCURO;
            };

            String iconoMetodo = switch (metodo) {
                case "YAPE"    -> "Yape";
                case "VISA"    -> "Tarjeta Visa";
                case "BCP"     -> "Tarjeta BCP";
                default        -> "Efectivo";
            };

            Table tablaPago = new Table(UnitValue.createPercentArray(new float[]{40, 60}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(4);

            tablaPago.addCell(new Cell()
                    .add(new Paragraph("Metodo de pago:").setFontSize(10).setBold())
                    .setBorder(Border.NO_BORDER).setPaddingBottom(4));
            tablaPago.addCell(new Cell()
                    .add(new Paragraph(iconoMetodo).setFontSize(11).setBold()
                            .setFontColor(colorMetodo))
                    .setBorder(Border.NO_BORDER).setPaddingBottom(4));

            documento.add(tablaPago);
            documento.add(lineaDivisoria());

            // ── Total ──────────────────────────────────────────────────
            Table tablaTotal = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(6);

            tablaTotal.addCell(new Cell()
                    .add(new Paragraph("TOTAL A PAGAR:").setBold().setFontSize(13))
                    .setBorder(Border.NO_BORDER)
                    .setFontColor(COLOR_OSCURO));

            tablaTotal.addCell(new Cell()
                    .add(new Paragraph("S/ " + reserva.getPrecioCobrado().toPlainString())
                            .setBold().setFontSize(16)
                            .setTextAlignment(TextAlignment.RIGHT))
                    .setBorder(Border.NO_BORDER)
                    .setFontColor(COLOR_VERDE));

            documento.add(tablaTotal);
            documento.add(lineaDivisoria());

            // ── Pie de pagina ──────────────────────────────────────────
            documento.add(new Paragraph(
                    "Gracias por su preferencia.\nEste documento es su comprobante de pago.")
                    .setFontSize(9).setFontColor(COLOR_GRIS)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(8));

            documento.close();
            abrirPdf(rutaCompleta);

        } catch (Exception e) {
            System.err.println("Error generando boleta PDF: " + e.getMessage());
            e.printStackTrace();
        }

        return rutaCompleta;
    }

    private static Table lineaDivisoria() {
        Table linea = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(6).setMarginBottom(6);
        linea.addCell(new Cell()
                .setBorderTop(Border.NO_BORDER)
                .setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                .setPadding(0).setHeight(1));
        return linea;
    }

    private static void agregarFila(Table tabla, String etiqueta, String valor) {
        tabla.addCell(new Cell()
                .add(new Paragraph(etiqueta).setFontSize(10).setBold())
                .setBorder(Border.NO_BORDER).setPaddingBottom(4));
        tabla.addCell(new Cell()
                .add(new Paragraph(valor).setFontSize(10))
                .setBorder(Border.NO_BORDER).setPaddingBottom(4));
    }

    private static void abrirPdf(String ruta) {
        try {
            Desktop.getDesktop().open(new File(ruta));
        } catch (Exception e) {
            System.out.println("PDF guardado en: " + ruta);
        }
    }
}