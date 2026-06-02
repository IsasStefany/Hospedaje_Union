package com.hospedaje.vista;

import com.hospedaje.dao.ReservaDAO;
import com.hospedaje.db.Conexion;
import com.hospedaje.modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Panel de cierre de caja diario.
 * Muestra ingresos del día, permite registrar gastos
 * y calcular el saldo neto.
 */
public class PanelCaja extends JPanel {

    private final Usuario    usuario;
    private final ReservaDAO reservaDAO = new ReservaDAO();

    private JLabel           lblFecha, lblIngresos, lblGastos, lblSaldo;
    private DefaultTableModel modeloGastos, modeloCaja;
    private JTable           tablaCaja;
    private JTextField       txtConcepto;
    private JTextField       txtMonto;
    private JButton          btnAgregarGasto;

    public PanelCaja(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 245, 248));
        construirUI();
        cargarResumen();
    }

    private void construirUI() {
        // ── Encabezado ────────────────────────────────────────────────────
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(245, 245, 248));
        encabezado.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel titulo = new JLabel("Caja del día");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        encabezado.add(titulo, BorderLayout.WEST);

        lblFecha = new JLabel(LocalDate.now()
                .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")));
        lblFecha.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFecha.setForeground(Color.GRAY);
        encabezado.add(lblFecha, BorderLayout.EAST);
        add(encabezado, BorderLayout.NORTH);

        // ── Contenido central ─────────────────────────────────────────────
        JPanel centro = new JPanel(new BorderLayout(0, 16));
        centro.setOpaque(false);
        centro.setBorder(new EmptyBorder(0, 20, 20, 20));

        // Panel superior — Contiene Resumen y Gastos
        JPanel panelSup = new JPanel(new GridLayout(1, 2, 16, 0));
        panelSup.setOpaque(false);

        // Panel izquierdo — Resumen
        JPanel panelResumen = new JPanel();
        panelResumen.setLayout(new BoxLayout(panelResumen, BoxLayout.Y_AXIS));
        panelResumen.setBackground(Color.WHITE);
        panelResumen.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        panelResumen.add(crearFilaResumen("Fecha:", LocalDate.now().toString(), Color.DARK_GRAY));
        panelResumen.add(Box.createVerticalStrut(10));

        lblIngresos = new JLabel("S/ 0.00");
        lblIngresos.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblIngresos.setForeground(new Color(46, 125, 50));
        panelResumen.add(crearCardResumen("Total ingresos del día", lblIngresos));
        panelResumen.add(Box.createVerticalStrut(10));

        lblGastos = new JLabel("S/ 0.00");
        lblGastos.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblGastos.setForeground(new Color(198, 40, 40));
        panelResumen.add(crearCardResumen("Total gastos del día", lblGastos));
        panelResumen.add(Box.createVerticalStrut(10));

        lblSaldo = new JLabel("S/ 0.00");
        lblSaldo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblSaldo.setForeground(new Color(21, 101, 192));
        panelResumen.add(crearCardResumen("Saldo neto del día", lblSaldo));
        panelResumen.add(Box.createVerticalStrut(16));

        JButton btnRefrescar = new JButton("Actualizar resumen");
        btnRefrescar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnRefrescar.addActionListener(e -> cargarResumen());
        panelResumen.add(btnRefrescar);

        panelSup.add(panelResumen);

        // Panel derecho — Gastos
        JPanel panelGastos = new JPanel(new BorderLayout(0, 10));
        panelGastos.setBackground(Color.WHITE);
        panelGastos.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(16, 16, 16, 16)));

        JLabel tituloGastos = new JLabel("Registrar gasto del día");
        tituloGastos.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelGastos.add(tituloGastos, BorderLayout.NORTH);

        // Tabla de gastos registrados
        String[] cols = {"Concepto", "Monto"};
        modeloGastos = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaGastos = new JTable(modeloGastos);
        tablaGastos.setRowHeight(26);
        tablaGastos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaGastos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tablaGastos.getColumnModel().getColumn(0).setPreferredWidth(220);
        tablaGastos.getColumnModel().getColumn(1).setPreferredWidth(80);

        JScrollPane scrollGastos = new JScrollPane(tablaGastos);
        panelGastos.add(scrollGastos, BorderLayout.CENTER);

        // Formulario agregar gasto
        JPanel formGasto = new JPanel(new GridLayout(3, 1, 4, 4));
        formGasto.setOpaque(false);

        JPanel filaConcepto = new JPanel(new BorderLayout(6, 0));
        filaConcepto.setOpaque(false);
        filaConcepto.add(new JLabel("Concepto:"), BorderLayout.WEST);
        txtConcepto = new JTextField();
        filaConcepto.add(txtConcepto, BorderLayout.CENTER);

        JPanel filaMonto = new JPanel(new BorderLayout(6, 0));
        filaMonto.setOpaque(false);
        filaMonto.add(new JLabel("Monto S/:"), BorderLayout.WEST);
        txtMonto = new JTextField();
        filaMonto.add(txtMonto, BorderLayout.CENTER);

        btnAgregarGasto = new JButton("Agregar gasto");
        btnAgregarGasto.setBackground(new Color(26, 26, 46));
        btnAgregarGasto.setForeground(Color.WHITE);
        btnAgregarGasto.setFocusPainted(false);
        btnAgregarGasto.setBorderPainted(false);
        btnAgregarGasto.addActionListener(e -> agregarGasto());

        formGasto.add(filaConcepto);
        formGasto.add(filaMonto);
        formGasto.add(btnAgregarGasto);
        panelGastos.add(formGasto, BorderLayout.SOUTH);

        panelSup.add(panelGastos);
        centro.add(panelSup, BorderLayout.CENTER);

        // Panel inferior — Historial de cierres de caja
        JPanel panelHistorico = new JPanel(new BorderLayout(0, 8));
        panelHistorico.setBackground(Color.WHITE);
        panelHistorico.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(16, 16, 16, 16)));
        panelHistorico.setPreferredSize(new Dimension(0, 220));

        JLabel tituloHist = new JLabel("Historial de cierres de caja");
        tituloHist.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panelHistorico.add(tituloHist, BorderLayout.NORTH);

        String[] colsCaja = {"Fecha", "Total ingresos", "Total gastos", "Saldo neto", "Cerrado", "Hora cierre"};
        modeloCaja = new DefaultTableModel(colsCaja, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaCaja = new JTable(modeloCaja);
        tablaCaja.setRowHeight(26);
        tablaCaja.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaCaja.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Renderizadores de celdas
        javax.swing.table.DefaultTableCellRenderer renderCentrado = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 250));
                    setForeground(Color.DARK_GRAY);
                }
                return this;
            }
        };

        javax.swing.table.DefaultTableCellRenderer renderNumero = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (val instanceof Number) {
                    setText(String.format("S/ %.2f", ((Number) val).doubleValue()));
                }
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 250));
                    setForeground(Color.DARK_GRAY);
                }
                return this;
            }
        };

        javax.swing.table.DefaultTableCellRenderer renderSaldo = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                setHorizontalAlignment(SwingConstants.RIGHT);
                if (val instanceof Number) {
                    double numVal = ((Number) val).doubleValue();
                    setText(String.format("S/ %.2f", numVal));
                    if (!sel) {
                        if (numVal > 0) {
                            setForeground(new Color(46, 125, 50));
                        } else if (numVal < 0) {
                            setForeground(new Color(198, 40, 40));
                        } else {
                            setForeground(Color.DARK_GRAY);
                        }
                    }
                }
                if (!sel) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 250));
                }
                return this;
            }
        };

        // Asignar renderizadores
        tablaCaja.getColumnModel().getColumn(0).setCellRenderer(renderCentrado);
        tablaCaja.getColumnModel().getColumn(1).setCellRenderer(renderNumero);
        tablaCaja.getColumnModel().getColumn(2).setCellRenderer(renderNumero);
        tablaCaja.getColumnModel().getColumn(3).setCellRenderer(renderSaldo);
        tablaCaja.getColumnModel().getColumn(4).setCellRenderer(renderCentrado);
        tablaCaja.getColumnModel().getColumn(5).setCellRenderer(renderCentrado);

        JScrollPane scrollCaja = new JScrollPane(tablaCaja);
        panelHistorico.add(scrollCaja, BorderLayout.CENTER);
        centro.add(panelHistorico, BorderLayout.SOUTH);

        add(centro, BorderLayout.CENTER);

        cargarGastos();
    }

    private JPanel crearCardResumen(String titulo, JLabel valorLabel) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(248, 248, 250));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(10, 12, 10, 12)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(Color.GRAY);
        card.add(lbl);
        card.add(Box.createVerticalStrut(4));
        card.add(valorLabel);
        return card;
    }

    private JPanel crearFilaResumen(String etiqueta, String valor, Color color) {
        JPanel fila = new JPanel(new BorderLayout());
        fila.setOpaque(false);
        JLabel lbl = new JLabel(etiqueta + " " + valor);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(color);
        fila.add(lbl);
        return fila;
    }

    private void cargarResumen() {
        double ingresos = reservaDAO.totalIngresosHoy();
        double gastos   = totalGastosHoy();
        double saldo    = ingresos - gastos;

        lblIngresos.setText(String.format("S/ %.2f", ingresos));
        lblGastos.setText(String.format("S/ %.2f", gastos));
        lblSaldo.setText(String.format("S/ %.2f", saldo));
        lblSaldo.setForeground(saldo >= 0
                ? new Color(21, 101, 192)
                : new Color(198, 40, 40));

        cargarHistorialCaja();
    }

    private void cargarGastos() {
        modeloGastos.setRowCount(0);
        String sql = "SELECT concepto, monto FROM gastos "
                + "WHERE fecha = CAST(GETDATE() AS DATE) ORDER BY fecha_registro DESC";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modeloGastos.addRow(new Object[]{
                        rs.getString("concepto"),
                        "S/ " + rs.getBigDecimal("monto").toPlainString()
                });
            }
        } catch (SQLException e) {
            System.err.println("Error cargando gastos: " + e.getMessage());
        }
    }

    private double totalGastosHoy() {
        String sql = "SELECT ISNULL(SUM(monto), 0) FROM gastos "
                + "WHERE fecha = CAST(GETDATE() AS DATE)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error total gastos: " + e.getMessage());
        }
        return 0;
    }

    private void agregarGasto() {
        String concepto = txtConcepto.getText().trim();
        String montoStr = txtMonto.getText().trim();

        if (concepto.isEmpty() || montoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Completa el concepto y el monto.",
                    "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
            if (monto <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "El monto debe ser un número mayor a 0.",
                    "Monto inválido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String sql = "INSERT INTO gastos (fecha, concepto, monto, usuario_id) "
                + "VALUES (CAST(GETDATE() AS DATE), ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, concepto);
            ps.setDouble(2, monto);
            ps.setInt(3, usuario.getId());
            ps.executeUpdate();

            txtConcepto.setText("");
            txtMonto.setText("");
            cargarGastos();
            cargarResumen();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar el gasto: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarHistorialCaja() {
        if (modeloCaja == null) return;
        modeloCaja.setRowCount(0);
        String sql = "SELECT fecha, total_ingresos, total_gastos, saldo_neto, cerrado, hora_cierre FROM caja_diaria ORDER BY fecha DESC";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                java.sql.Date fecha = rs.getDate("fecha");
                double ingresos = rs.getDouble("total_ingresos");
                double gastos = rs.getDouble("total_gastos");
                double saldo = rs.getDouble("saldo_neto");
                boolean cerrado = rs.getBoolean("cerrado");
                java.sql.Time hora = rs.getTime("hora_cierre");

                modeloCaja.addRow(new Object[]{
                    fecha != null ? fecha.toString() : "-",
                    ingresos,
                    gastos,
                    saldo,
                    cerrado ? "Cerrado" : "Abierto",
                    hora != null ? hora.toString() : "-"
                });
            }
        } catch (SQLException e) {
            System.err.println("Error cargando historial de caja: " + e.getMessage());
        }
    }
}
