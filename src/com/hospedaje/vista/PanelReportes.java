package com.hospedaje.vista;

import com.hospedaje.dao.HabitacionDAO;
import com.hospedaje.dao.ReservaDAO;
import com.hospedaje.db.Conexion;
import com.hospedaje.util.CalculadorPrecio;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class PanelReportes extends JPanel {

    private final HabitacionDAO habitacionDAO = new HabitacionDAO();
    private final ReservaDAO    reservaDAO    = new ReservaDAO();

    private DefaultTableModel modeloMensual;
    private JTable            tablaMensual;
    private JLabel            lblTotalAnual, lblPromedioMensual, lblMejorMes;
    private JSpinner          spnAnio;
    private GraficoBarrasPanel graficoBarras;

    private int[]    datReservas   = new int[12];
    private double[] datIngReales  = new double[12];
    private double[] datProyeccion = new double[12];

    private static final String[] MESES = {
            "Enero","Febrero","Marzo","Abril","Mayo","Junio",
            "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"
    };

    private static final DeviceRgb AZUL_OSC  = new DeviceRgb(26, 26, 46);
    private static final DeviceRgb AZUL_CLARO= new DeviceRgb(21, 101, 192);
    private static final DeviceRgb VERDE     = new DeviceRgb(46, 125, 50);
    private static final DeviceRgb ROJO      = new DeviceRgb(198, 40, 40);
    private static final DeviceRgb GRIS      = new DeviceRgb(120, 120, 120);
    private static final DeviceRgb VERDE_CLR = new DeviceRgb(232, 245, 233);
    private static final DeviceRgb AZUL_HDR  = new DeviceRgb(232, 240, 254);

    public PanelReportes() {
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 245, 248));
        construirUI();
        cargarReporte(java.time.Year.now().getValue());
    }

    private void construirUI() {
        // ── Encabezado ─────────────────────────────────────────────────
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(245, 245, 248));
        encabezado.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel titulo = new JLabel("Reporte de ingresos");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        encabezado.add(titulo, BorderLayout.WEST);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controles.setOpaque(false);
        controles.add(new JLabel("Año:"));
        spnAnio = new JSpinner(new SpinnerNumberModel(
                java.time.Year.now().getValue(), 2024, 2099, 1));
        spnAnio.setPreferredSize(new Dimension(80, 28));
        controles.add(spnAnio);

        JButton btnCargar = crearBtn("Actualizar tabla", new Color(26, 26, 46));
        btnCargar.addActionListener(e -> cargarReporte((int) spnAnio.getValue()));
        controles.add(btnCargar);

        JButton btnPDF = crearBtn("Generar reporte PDF", new Color(198, 40, 40));
        btnPDF.addActionListener(e -> generarPDF((int) spnAnio.getValue()));
        controles.add(btnPDF);

        encabezado.add(controles, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        // ── Tabla ──────────────────────────────────────────────────────
        JPanel contenido = new JPanel(new BorderLayout(16, 0));
        contenido.setOpaque(false);
        contenido.setBorder(new EmptyBorder(0, 20, 20, 20));

        String[] cols = {"Mes","Reservas","Ingresos reales","Proyeccion estimada","Diferencia"};
        modeloMensual = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaMensual = new JTable(modeloMensual);
        tablaMensual.setRowHeight(28);
        tablaMensual.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaMensual.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaMensual.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaMensual.setGridColor(new Color(220, 220, 220));
        tablaMensual.getColumnModel().getColumn(0).setPreferredWidth(110);
        tablaMensual.getColumnModel().getColumn(1).setPreferredWidth(70);
        tablaMensual.getColumnModel().getColumn(2).setPreferredWidth(130);
        tablaMensual.getColumnModel().getColumn(3).setPreferredWidth(150);
        tablaMensual.getColumnModel().getColumn(4).setPreferredWidth(100);

        tablaMensual.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    setBackground(datIngReales[row] > 0
                            ? new Color(232, 245, 233)
                            : (row % 2 == 0 ? Color.WHITE : new Color(248, 248, 250)));
                    setForeground(Color.DARK_GRAY);
                }
                return this;
            }
        });

        tablaMensual.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int fila = tablaMensual.getSelectedRow();
                    if (fila >= 0) abrirFormulario(fila);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(tablaMensual);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));

        JPanel panelIzq = new JPanel(new BorderLayout(0, 8));
        panelIzq.setOpaque(false);
        panelIzq.add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        btnPanel.setOpaque(false);

        JButton btnAgregar = crearBtn("+ Agregar / Editar ingreso", new Color(26, 26, 46));
        btnAgregar.addActionListener(e -> {
            int fila = tablaMensual.getSelectedRow();
            if (fila < 0) { avisar("Selecciona un mes de la tabla primero."); return; }
            abrirFormulario(fila);
        });

        JButton btnEliminar = crearBtn("Eliminar ingreso", new Color(198, 40, 40));
        btnEliminar.addActionListener(e -> eliminarIngreso());

        JLabel hint = new JLabel("  Doble clic en un mes para editar rapido");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);

        btnPanel.add(btnAgregar);
        btnPanel.add(btnEliminar);
        btnPanel.add(hint);
        panelIzq.add(btnPanel, BorderLayout.SOUTH);
        contenido.add(panelIzq, BorderLayout.CENTER);

        // Panel resumen
        JPanel panelResumen = new JPanel();
        panelResumen.setLayout(new BoxLayout(panelResumen, BoxLayout.Y_AXIS));
        panelResumen.setBackground(Color.WHITE);
        panelResumen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        panelResumen.setPreferredSize(new Dimension(300, 0));

        JLabel tituloRes = new JLabel("Resumen anual");
        tituloRes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tituloRes.setForeground(new Color(26, 26, 46));
        panelResumen.add(tituloRes);
        panelResumen.add(Box.createVerticalStrut(12));

        lblTotalAnual = new JLabel("S/ 0,00");
        lblTotalAnual.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTotalAnual.setForeground(new Color(46, 125, 50));
        panelResumen.add(crearCardMini("Total anual proyectado", lblTotalAnual));
        panelResumen.add(Box.createVerticalStrut(10));

        lblPromedioMensual = new JLabel("S/ 0,00");
        lblPromedioMensual.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblPromedioMensual.setForeground(new Color(21, 101, 192));
        panelResumen.add(crearCardMini("Promedio mensual", lblPromedioMensual));
        panelResumen.add(Box.createVerticalStrut(10));

        lblMejorMes = new JLabel("-");
        lblMejorMes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMejorMes.setForeground(new Color(230, 81, 0));
        panelResumen.add(crearCardMini("Mejor mes", lblMejorMes));
        panelResumen.add(Box.createVerticalStrut(16));

        JLabel lblTituloGrafico = new JLabel("Evolución de Ingresos Mensuales");
        lblTituloGrafico.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTituloGrafico.setForeground(new Color(26, 26, 46));
        lblTituloGrafico.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelResumen.add(lblTituloGrafico);
        panelResumen.add(Box.createVerticalStrut(6));

        graficoBarras = new GraficoBarrasPanel();
        graficoBarras.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelResumen.add(graficoBarras);
        panelResumen.add(Box.createVerticalStrut(16));

        JLabel nota = new JLabel("<html><small style='color:gray'>"
                + "Barras verdes = datos reales. Celestes = proyecciones.<br>"
                + "Usa el botón rojo para exportar el PDF detallado.</small></html>");
        nota.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelResumen.add(nota);
        contenido.add(panelResumen, BorderLayout.EAST);
        add(contenido, BorderLayout.CENTER);
    }

    // ── Cargar datos ────────────────────────────────────────────────────
    private void cargarReporte(int anio) {
        modeloMensual.setRowCount(0);
        for (int i = 0; i < 12; i++) { datReservas[i] = 0; datIngReales[i] = 0; datProyeccion[i] = 0; }

        HashMap<Integer, Double>  ingReales  = new HashMap<>();
        HashMap<Integer, Integer> resxMes    = new HashMap<>();

        String sql1 = "SELECT MONTH(fecha_creacion) AS mes, COUNT(*) AS tot_res, "
                + "SUM(precio_cobrado) AS tot_ing FROM reservas "
                + "WHERE YEAR(fecha_creacion)=? AND estado IN ('ACTIVA','FINALIZADA','EXTENDIDA') "
                + "GROUP BY MONTH(fecha_creacion)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql1)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ingReales.put(rs.getInt("mes"), rs.getDouble("tot_ing"));
                    resxMes.put(rs.getInt("mes"), rs.getInt("tot_res"));
                }
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }

        HashMap<Integer, Double>  ingMan  = new HashMap<>();
        HashMap<Integer, Integer> resMan  = new HashMap<>();
        String sql2 = "SELECT mes, total_reservas, total_ingresos FROM proyeccion_ingresos WHERE anio=?";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql2)) {
            ps.setInt(1, anio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ingMan.put(rs.getInt("mes"), rs.getDouble("total_ingresos"));
                    resMan.put(rs.getInt("mes"), rs.getInt("total_reservas"));
                }
            }
        } catch (SQLException e) { System.err.println(e.getMessage()); }

        LinkedHashMap<String, Double> proy = CalculadorPrecio.proyeccionMensual(
                ingReales, habitacionDAO.totalHabitaciones(), habitacionDAO.precioPromedio());
        double[] valProy = proy.values().stream().mapToDouble(Double::doubleValue).toArray();

        double totalAnual = 0; double mejorVal = 0; String mejorNom = "-";
        for (int i = 0; i < 12; i++) {
            int    nm  = i + 1;
            double real = ingReales.getOrDefault(nm, ingMan.getOrDefault(nm, 0.0));
            double pr   = valProy[i];
            int    res  = resxMes.getOrDefault(nm, resMan.getOrDefault(nm, 0));
            datReservas[i] = res; datIngReales[i] = real; datProyeccion[i] = pr;
            modeloMensual.addRow(new Object[]{
                    MESES[i], res,
                    String.format("S/ %.2f", real),
                    String.format("S/ %.2f", pr),
                    real > 0 ? String.format("S/ %.2f", real - pr) : "-"
            });
            double vm = real > 0 ? real : pr;
            totalAnual += vm;
            if (vm > mejorVal) { mejorVal = vm; mejorNom = MESES[i]; }
        }
        lblTotalAnual.setText(String.format("S/ %.2f", totalAnual));
        lblPromedioMensual.setText(String.format("S/ %.2f", totalAnual / 12));
        lblMejorMes.setText(mejorNom);
        if (graficoBarras != null) {
            graficoBarras.repaint();
        }
    }

    // ── Generar PDF detallado ───────────────────────────────────────────
    private void generarPDF(int anio) {
        String carpeta = System.getProperty("user.dir") + File.separator + "reportes";
        new File(carpeta).mkdirs();
        String archivo = carpeta + File.separator + "reporte_" + anio + "_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".pdf";

        try {
            PdfWriter   writer = new PdfWriter(archivo);
            PdfDocument pdf    = new PdfDocument(writer);
            Document    doc    = new Document(pdf);
            doc.setMargins(36, 40, 36, 40);

            // ── Encabezado PDF ──────────────────────────────────────
            doc.add(new Paragraph("HOSPEDAJE UNION")
                    .setFontSize(20).setBold().setFontColor(AZUL_OSC)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("Tocache, San Martin - Peru")
                    .setFontSize(10).setFontColor(GRIS)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(lineaPDF());

            doc.add(new Paragraph("REPORTE DE INGRESOS - AÑO " + anio)
                    .setFontSize(14).setBold().setFontColor(AZUL_OSC)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(6));
            doc.add(new Paragraph("Generado el: "
                    + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                    .setFontSize(9).setFontColor(GRIS)
                    .setTextAlignment(TextAlignment.CENTER));

            doc.add(lineaPDF());

            // ── Tabla mensual ───────────────────────────────────────
            doc.add(new Paragraph("Detalle mensual")
                    .setFontSize(12).setBold().setFontColor(AZUL_OSC).setMarginTop(10));

            Table tabla = new Table(UnitValue.createPercentArray(new float[]{22,12,20,22,14,10}))
                    .setWidth(UnitValue.createPercentValue(100)).setMarginTop(6);

            // Cabecera
            String[] hdrs = {"Mes","Reservas","Ingresos reales","Proyeccion estimada","Diferencia","Estado"};
            for (String h : hdrs) {
                tabla.addHeaderCell(new Cell()
                        .add(new Paragraph(h).setBold().setFontSize(10)
                                .setFontColor(AZUL_OSC))
                        .setBackgroundColor(AZUL_HDR)
                        .setPadding(6));
            }

            double totalReal = 0, totalProy = 0;
            int    totalRes  = 0;

            for (int i = 0; i < 12; i++) {
                double real = datIngReales[i];
                double pr   = datProyeccion[i];
                int    res  = datReservas[i];
                double dif  = real - pr;
                totalReal += real; totalProy += pr; totalRes += res;

                boolean tieneReal = real > 0;
                DeviceRgb bgFila  = tieneReal ? VERDE_CLR : new DeviceRgb(255,255,255);

                tabla.addCell(celda(MESES[i], 10, bgFila, false));
                tabla.addCell(celda(String.valueOf(res), 10, bgFila, false));
                tabla.addCell(celda(String.format("S/ %.2f", real), 10, bgFila, tieneReal));
                tabla.addCell(celda(String.format("S/ %.2f", pr), 10, bgFila, false));
                tabla.addCell(celda(tieneReal ? String.format("S/ %.2f", dif) : "-",
                        10, bgFila, false));

                String estado = tieneReal ? "Real" : "Estimado";
                DeviceRgb clrEst = tieneReal ? VERDE : GRIS;
                tabla.addCell(new Cell()
                        .add(new Paragraph(estado).setFontSize(9).setFontColor(clrEst).setBold())
                        .setBackgroundColor(bgFila).setPadding(4).setBorder(Border.NO_BORDER)
                        .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)));
            }

            // Fila totales
            tabla.addCell(new Cell().add(new Paragraph("TOTAL").setBold().setFontSize(10)
                    .setFontColor(AZUL_OSC)).setBackgroundColor(AZUL_HDR).setPadding(6));
            tabla.addCell(new Cell().add(new Paragraph(String.valueOf(totalRes)).setBold()
                    .setFontSize(10)).setBackgroundColor(AZUL_HDR).setPadding(6));
            tabla.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", totalReal))
                            .setBold().setFontSize(10).setFontColor(VERDE))
                    .setBackgroundColor(AZUL_HDR).setPadding(6));
            tabla.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", totalProy))
                    .setBold().setFontSize(10)).setBackgroundColor(AZUL_HDR).setPadding(6));
            tabla.addCell(new Cell().add(new Paragraph(String.format("S/ %.2f", totalReal - totalProy))
                    .setBold().setFontSize(10)).setBackgroundColor(AZUL_HDR).setPadding(6));
            tabla.addCell(new Cell().add(new Paragraph("-").setFontSize(10))
                    .setBackgroundColor(AZUL_HDR).setPadding(6));

            doc.add(tabla);
            doc.add(lineaPDF());

            // ── Resumen ejecutivo ───────────────────────────────────
            doc.add(new Paragraph("Resumen ejecutivo")
                    .setFontSize(12).setBold().setFontColor(AZUL_OSC).setMarginTop(10));

            Table resumen = new Table(UnitValue.createPercentArray(new float[]{50, 50}))
                    .setWidth(UnitValue.createPercentValue(100)).setMarginTop(6);

            double promMensual = totalReal > 0 ? totalReal / 12 : totalProy / 12;
            String mejorMes = lblMejorMes.getText();

            resumen.addCell(celdaRes("Total ingresos reales del año:", String.format("S/ %.2f", totalReal), VERDE));
            resumen.addCell(celdaRes("Total proyeccion estimada:", String.format("S/ %.2f", totalProy), AZUL_CLARO));
            resumen.addCell(celdaRes("Promedio mensual:", String.format("S/ %.2f", promMensual), AZUL_CLARO));
            resumen.addCell(celdaRes("Total reservas registradas:", String.valueOf(totalRes), AZUL_OSC));
            resumen.addCell(celdaRes("Mejor mes del año:", mejorMes, VERDE));
            resumen.addCell(celdaRes("Diferencia real vs estimado:",
                    String.format("S/ %.2f", totalReal - totalProy),
                    totalReal >= totalProy ? VERDE : ROJO));

            doc.add(resumen);
            doc.add(lineaPDF());

            // ── Pie de página ───────────────────────────────────────
            doc.add(new Paragraph("Este reporte fue generado automaticamente por el Sistema de Gestion - Hospedaje Union.")
                    .setFontSize(8).setFontColor(GRIS)
                    .setTextAlignment(TextAlignment.CENTER).setMarginTop(8));

            doc.close();

            // Abrir PDF automáticamente
            try {
                java.awt.Desktop.getDesktop().open(new File(archivo));
            } catch (Exception e) {
                System.out.println("PDF guardado en: " + archivo);
            }

            JOptionPane.showMessageDialog(this,
                    "Reporte PDF generado exitosamente.\nGuardado en:\n" + archivo,
                    "Reporte generado", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error al generar el PDF: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private Table lineaPDF() {
        Table t = new Table(UnitValue.createPercentArray(new float[]{1}))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(6).setMarginBottom(6);
        t.addCell(new Cell()
                .setBorderTop(Border.NO_BORDER).setBorderLeft(Border.NO_BORDER)
                .setBorderRight(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                .setPadding(0).setHeight(1));
        return t;
    }

    private Cell celda(String texto, float size, DeviceRgb bg, boolean bold) {
        Paragraph p = new Paragraph(texto).setFontSize(size);
        if (bold) p.setBold().setFontColor(VERDE);
        return new Cell().add(p).setBackgroundColor(bg).setPadding(5)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f));
    }

    private Cell celdaRes(String etiqueta, String valor, DeviceRgb colorVal) {
        Cell c = new Cell().setBorder(Border.NO_BORDER)
                .setBorderBottom(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f))
                .setPadding(8);
        c.add(new Paragraph(etiqueta).setFontSize(10).setFontColor(GRIS));
        c.add(new Paragraph(valor).setFontSize(13).setBold().setFontColor(colorVal));
        return c;
    }

    // ── Formulario edición ──────────────────────────────────────────────
    private void abrirFormulario(int fila) {
        int anio = (int) spnAnio.getValue();
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Editar ingreso - " + MESES[fila] + " " + anio,
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(420, 260);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(0, 2, 12, 10));
        panel.setBorder(new EmptyBorder(20, 24, 10, 24));

        JTextField txtReservas   = new JTextField(String.valueOf(datReservas[fila]));
        JTextField txtIngresos   = new JTextField(String.format("%.2f", datIngReales[fila]));
        JTextField txtProyeccion = new JTextField(String.format("%.2f", datProyeccion[fila]));

        JLabel lblMesVal = new JLabel(MESES[fila] + " " + anio);
        lblMesVal.setFont(new Font("Segoe UI", Font.BOLD, 13));

        panel.add(new JLabel("Mes:"));            panel.add(lblMesVal);
        panel.add(new JLabel("N de reservas:"));  panel.add(txtReservas);
        panel.add(new JLabel("Ingresos reales S/:")); panel.add(txtIngresos);
        panel.add(new JLabel("Proyeccion estimada S/:")); panel.add(txtProyeccion);

        JButton btnGuardar = crearBtn("Guardar", new Color(26, 26, 46));
        btnGuardar.addActionListener(e -> {
            try {
                int    res  = Integer.parseInt(txtReservas.getText().trim());
                double ing  = Double.parseDouble(txtIngresos.getText().trim().replace(",", "."));
                double pr   = Double.parseDouble(txtProyeccion.getText().trim().replace(",", "."));

                guardarIngreso(anio, fila + 1, res, ing, pr);
                datReservas[fila] = res; datIngReales[fila] = ing; datProyeccion[fila] = pr;

                modeloMensual.setValueAt(res, fila, 1);
                modeloMensual.setValueAt(String.format("S/ %.2f", ing), fila, 2);
                modeloMensual.setValueAt(String.format("S/ %.2f", pr), fila, 3);
                modeloMensual.setValueAt(ing > 0 ? String.format("S/ %.2f", ing - pr) : "-", fila, 4);
                actualizarResumen();
                dlg.dispose();
                JOptionPane.showMessageDialog(PanelReportes.this,
                        MESES[fila] + " actualizado correctamente.", "Guardado",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg,
                        "Verifica que los valores sean numeros validos.\nUsa punto (.) como decimal.",
                        "Datos invalidos", JOptionPane.WARNING_MESSAGE);
            }
        });

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dlg.dispose());
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bottom.add(btnCancelar); bottom.add(btnGuardar);
        dlg.setLayout(new BorderLayout());
        dlg.add(panel, BorderLayout.CENTER);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void guardarIngreso(int anio, int mes, int reservas, double ingresos, double proyeccion) {
        String sql = "IF EXISTS (SELECT 1 FROM proyeccion_ingresos WHERE anio=? AND mes=?) "
                + "  UPDATE proyeccion_ingresos SET total_reservas=?, total_ingresos=?, "
                + "    saldo_neto=?, fecha_calculo=GETDATE() WHERE anio=? AND mes=? "
                + "ELSE "
                + "  INSERT INTO proyeccion_ingresos (anio,mes,total_reservas,total_ingresos,saldo_neto) "
                + "  VALUES (?,?,?,?,?)";
        try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1,anio); ps.setInt(2,mes);
            ps.setInt(3,reservas); ps.setDouble(4,ingresos); ps.setDouble(5,ingresos-proyeccion);
            ps.setInt(6,anio); ps.setInt(7,mes);
            ps.setInt(8,anio); ps.setInt(9,mes);
            ps.setInt(10,reservas); ps.setDouble(11,ingresos); ps.setDouble(12,ingresos-proyeccion);
            ps.executeUpdate();
        } catch (SQLException e) { System.err.println("Error guardando: " + e.getMessage()); }
    }

    private void eliminarIngreso() {
        int fila = tablaMensual.getSelectedRow();
        if (fila < 0) { avisar("Selecciona un mes de la tabla."); return; }
        if (datIngReales[fila] == 0) { avisar("Este mes no tiene ingresos registrados para eliminar."); return; }
        int anio = (int) spnAnio.getValue();
        int conf = JOptionPane.showConfirmDialog(this,
                "Eliminar los ingresos de " + MESES[fila] + " " + anio + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM proyeccion_ingresos WHERE anio=? AND mes=?";
            try (Connection con = Conexion.getConexion(); PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, anio); ps.setInt(2, fila + 1);
                ps.executeUpdate();
                cargarReporte(anio);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void actualizarResumen() {
        double total = 0; double mejor = 0; String nomMejor = "-";
        for (int i = 0; i < 12; i++) {
            double v = datIngReales[i] > 0 ? datIngReales[i] : datProyeccion[i];
            total += v;
            if (v > mejor) { mejor = v; nomMejor = MESES[i]; }
        }
        lblTotalAnual.setText(String.format("S/ %.2f", total));
        lblPromedioMensual.setText(String.format("S/ %.2f", total / 12));
        lblMejorMes.setText(nomMejor);
        if (graficoBarras != null) {
            graficoBarras.repaint();
        }
    }

    private JButton crearBtn(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return btn;
    }

    private JPanel crearCardMini(String titulo, JLabel valorLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(248, 248, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220),1,true),
                new EmptyBorder(8,10,8,10)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(Color.GRAY);
        card.add(lbl); card.add(Box.createVerticalStrut(3)); card.add(valorLabel);
        return card;
    }

    private void avisar(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atencion", JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Panel personalizado para renderizar un gráfico de barras estilizado.
     */
    private class GraficoBarrasPanel extends JPanel {
        public GraficoBarrasPanel() {
            setPreferredSize(new Dimension(260, 180));
            setBackground(Color.WHITE);
            setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;

            // Renderizado suave y de alta calidad
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int padLeft = 32;
            int padRight = 10;
            int padTop = 15;
            int padBottom = 22;

            int graphW = w - padLeft - padRight;
            int graphH = h - padTop - padBottom;

            // Encontrar el valor máximo para escalar
            double maxVal = 0.0;
            for (int i = 0; i < 12; i++) {
                double v = datIngReales[i] > 0 ? datIngReales[i] : datProyeccion[i];
                if (v > maxVal) {
                    maxVal = v;
                }
            }
            if (maxVal <= 0) {
                maxVal = 1000.0; // valor base si no hay datos
            }

            // Dibujar líneas horizontales de cuadrícula (escala de 4 tramos)
            g2.setColor(new Color(240, 240, 243));
            g2.setStroke(new BasicStroke(1f));
            for (int i = 0; i <= 4; i++) {
                int y = padTop + graphH - (i * graphH / 4);
                g2.drawLine(padLeft, y, w - padRight, y);

                // Etiquetas Y
                double scaleVal = maxVal * i / 4.0;
                g2.setColor(new Color(120, 120, 130));
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                String scaleStr;
                if (scaleVal >= 1000.0) {
                    scaleStr = String.format("%.1fk", scaleVal / 1000.0);
                } else {
                    scaleStr = String.format("%.0f", scaleVal);
                }
                g2.drawString(scaleStr, 2, y + 3);
                g2.setColor(new Color(240, 240, 243));
            }

            // Dibujar las barras para los 12 meses
            int numMeses = 12;
            double barWidth = (double) graphW / numMeses;
            int spacing = 3;

            String[] etiquetasMes = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

            for (int i = 0; i < numMeses; i++) {
                double val = datIngReales[i] > 0 ? datIngReales[i] : datProyeccion[i];
                boolean esReal = datIngReales[i] > 0;

                int barH = (int) ((val / maxVal) * graphH);
                int x = padLeft + (int) (i * barWidth) + spacing / 2;
                int y = padTop + graphH - barH;
                int bw = (int) barWidth - spacing;

                if (bw < 2) bw = 2;
                if (barH < 1) barH = 1;

                // Color de la barra: verde para real, celeste para proyección
                if (esReal) {
                    g2.setColor(new Color(46, 125, 50));
                } else {
                    g2.setColor(new Color(144, 202, 249));
                }

                // Dibujar barra redondeada
                g2.fillRoundRect(x, y, bw, barH, 3, 3);

                // Dibujar etiqueta X (Meses)
                g2.setColor(Color.GRAY);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 8));
                FontMetrics fm = g2.getFontMetrics();
                int labelW = fm.stringWidth(etiquetasMes[i]);
                int labelX = x + (bw - labelW) / 2;
                g2.drawString(etiquetasMes[i], labelX, h - 5);
            }

            // Dibujar eje X
            g2.setColor(Color.LIGHT_GRAY);
            g2.drawLine(padLeft, padTop + graphH, w - padRight, padTop + graphH);
        }
    }
}