package com.hospedaje.vista;

import com.hospedaje.db.Conexion;
import com.hospedaje.estructura.ListaLavanderia;
import com.hospedaje.estructura.NodoLavanderia;
import com.hospedaje.modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PanelLavanderia extends JPanel {

    private final Usuario         usuario;
    private final ListaLavanderia lista = new ListaLavanderia();

    private DefaultTableModel modeloTabla;
    private JTable            tabla;
    private JLabel            lblTotal, lblPendientes, lblEstructura;

    private static final double PRECIO_KILO = 5.0;
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PanelLavanderia(Usuario usuario) {
        this.usuario = usuario;
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 245, 248));
        construirUI();
        cargarDesdeBD();
    }

    private void construirUI() {
        // ── Encabezado ─────────────────────────────────────────────────
        JPanel enc = new JPanel(new BorderLayout());
        enc.setBackground(new Color(245, 245, 248));
        enc.setBorder(new EmptyBorder(20, 20, 10, 20));
        JLabel titulo = new JLabel("Lavanderia");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        enc.add(titulo, BorderLayout.WEST);
        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarDesdeBD());
        enc.add(btnRefrescar, BorderLayout.EAST);
        add(enc, BorderLayout.NORTH);

        // ── Tabla ──────────────────────────────────────────────────────
        String[] cols = {"ID","Hab.","Huesped","Kilos","Costo","Tipo",
                "Estado","Fecha registro","Entrega estimada","Observaciones"};
        modeloTabla = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modeloTabla);
        tabla.setRowHeight(28);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setGridColor(new Color(220, 220, 220));

        int[] anchos = {40, 60, 140, 55, 65, 130, 100, 120, 120, 180};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    String estado = (String) t.getValueAt(row, 6);
                    setBackground(switch (estado != null ? estado : "") {
                        case "PENDIENTE"  -> new Color(255, 248, 225);
                        case "EN_PROCESO" -> new Color(227, 242, 253);
                        case "LISTO"      -> new Color(232, 245, 233);
                        case "ENTREGADO"  -> new Color(245, 245, 245);
                        default           -> Color.WHITE;
                    });
                    // Resaltar en rojo si la entrega estimada ya paso
                    if (col == 8 && val != null && !val.toString().equals("-")) {
                        try {
                            LocalDateTime est = LocalDateTime.parse(
                                    val.toString(), FMT);
                            if (est.isBefore(LocalDateTime.now())
                                    && !"ENTREGADO".equals(estado)) {
                                setForeground(new Color(198, 40, 40));
                                setFont(getFont().deriveFont(Font.BOLD));
                                return this;
                            }
                        } catch (Exception ignored) {}
                    }
                    setForeground(Color.DARK_GRAY);
                }
                return this;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        // ── Estadisticas ───────────────────────────────────────────────
        JPanel stats = new JPanel(new GridLayout(1, 3, 10, 0));
        stats.setOpaque(false);
        stats.setBorder(new EmptyBorder(0, 20, 10, 20));
        lblTotal      = crearStatCard("Total ingresos lavanderia","S/ 0.00",new Color(46,125,50));
        lblPendientes = crearStatCard("Pedidos pendientes",       "0",      new Color(198,40,40));
        lblEstructura = crearStatCard("Lista enlazada",           "null",   new Color(21,101,192));
        stats.add(lblTotal.getParent());
        stats.add(lblPendientes.getParent());
        stats.add(lblEstructura.getParent());

        // ── Botones ────────────────────────────────────────────────────
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        botones.setOpaque(false);
        botones.setBorder(new EmptyBorder(0, 20, 16, 20));

        JButton btnNuevo     = crearBtn("+ Nuevo pedido", new Color(26, 26, 46));
        JButton btnEnProceso = crearBtn("En proceso",     new Color(21, 101, 192));
        JButton btnListo     = crearBtn("Listo",          new Color(46, 125, 50));
        JButton btnEntregado = crearBtn("Entregado",      new Color(100, 100, 100));
        JButton btnEliminar  = crearBtn("Eliminar",       new Color(198, 40, 40));
        JButton btnVerTodos  = crearBtn("Ver todos",      new Color(80, 80, 80));
        JButton btnEditar    = crearBtn("Editar",         new Color(230, 81, 0));

        btnNuevo.addActionListener(e     -> abrirFormulario());
        btnEnProceso.addActionListener(e -> cambiarEstado("EN_PROCESO"));
        btnListo.addActionListener(e     -> cambiarEstado("LISTO"));
        btnEntregado.addActionListener(e -> cambiarEstado("ENTREGADO"));
        btnEliminar.addActionListener(e  -> eliminar());
        btnVerTodos.addActionListener(e  -> cargarTodos());
        btnEditar.addActionListener(e    -> editarPedido());

        botones.add(btnNuevo);
        botones.add(btnEnProceso);
        botones.add(btnListo);
        botones.add(btnEntregado);
        botones.add(btnEliminar);
        botones.add(btnVerTodos);
        botones.add(btnEditar);

        JPanel panelCentro = new JPanel(new BorderLayout(0, 0));
        panelCentro.setOpaque(false);
        panelCentro.setBorder(new EmptyBorder(0, 20, 0, 20));
        panelCentro.add(stats,   BorderLayout.NORTH);
        panelCentro.add(scroll,  BorderLayout.CENTER);
        panelCentro.add(botones, BorderLayout.SOUTH);
        add(panelCentro, BorderLayout.CENTER);
    }

    // ── Carga solo activos (sin ENTREGADO) ─────────────────────────────
    private void cargarDesdeBD() {
        cargarConFiltro("WHERE l.estado <> 'ENTREGADO'");
    }

    private void cargarTodos() {
        cargarConFiltro("");
    }

    private void cargarConFiltro(String filtro) {
        for (NodoLavanderia n : lista.obtenerTodos()) lista.eliminar(n.getId());
        modeloTabla.setRowCount(0);

        String sql = "SELECT l.id, h.numero AS hab, l.nombre_huesped, "
                + "l.kilos, l.costo_total, l.tipo, l.estado, "
                + "l.observaciones, l.reserva_id, "
                + "CONVERT(VARCHAR,l.fecha_registro,103)+' '+CONVERT(VARCHAR,l.fecha_registro,108) AS fecha_reg, "
                + "CASE WHEN l.fecha_entrega_estimada IS NULL THEN '-' "
                + "     ELSE CONVERT(VARCHAR,l.fecha_entrega_estimada,103)+' '"
                + "          +CONVERT(VARCHAR,l.fecha_entrega_estimada,108) "
                + "     END AS fecha_est "
                + "FROM lavanderia l "
                + "JOIN habitaciones h ON h.id = l.habitacion_id "
                + filtro + " "
                + "ORDER BY l.fecha_registro DESC";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                NodoLavanderia nodo = new NodoLavanderia(
                        rs.getInt("reserva_id"),
                        rs.getString("nombre_huesped"),
                        rs.getString("hab"),
                        rs.getDouble("kilos"),
                        rs.getString("tipo"),
                        rs.getString("observaciones")
                );
                nodo.setId(rs.getInt("id"));
                nodo.setEstado(rs.getString("estado"));
                nodo.setFechaRegistro(rs.getString("fecha_reg"));
                nodo.setFechaEntregaEstimada(rs.getString("fecha_est"));
                lista.agregar(nodo);

                String obs = rs.getString("observaciones");
                modeloTabla.addRow(new Object[]{
                        nodo.getId(),
                        nodo.getNumeroHabitacion(),
                        nodo.getNombreHuesped(),
                        nodo.getKilos() + " kg",
                        "S/ " + String.format("%.2f", nodo.getCosto()),
                        nodo.getTipoLabel(),
                        nodo.getEstado(),
                        rs.getString("fecha_reg"),
                        rs.getString("fecha_est"),
                        obs != null && !obs.isEmpty() ? obs : "-"
                });
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            JOptionPane.showMessageDialog(this,
                    "Error al cargar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        actualizarEstadisticas();
    }

    private void actualizarEstadisticas() {
        lblTotal.setText(String.format("S/ %.2f", lista.totalIngresos()));
        lblPendientes.setText(String.valueOf(lista.obtenerPendientes().size()));
        String est = lista.visualizarEstructura();
        lblEstructura.setText(est.length() > 45 ? est.substring(0, 45) + "..." : est);
    }

    private void abrirFormulario() {
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Nuevo pedido de lavanderia", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(480, 430);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 24, 10, 24));

        JTextField txtHabitacion = new JTextField();
        JTextField txtHuesped    = new JTextField();
        JTextField txtKilos      = new JTextField();

        JLabel lblCosto   = new JLabel("S/ 0.00");
        lblCosto.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblCosto.setForeground(new Color(46, 125, 50));

        JLabel lblEntrega = new JLabel("-");
        lblEntrega.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEntrega.setForeground(new Color(21, 101, 192));

        String[] tiposLabel = {"Ropa del huesped","Sabanas y toallas","Ropa + sabanas/toallas"};
        String[] tipos      = {"ROPA_HUESPED","SABANAS_TOALLAS","MIXTO"};
        JComboBox<String> cboTipo = new JComboBox<>(tiposLabel);
        JTextField txtObs = new JTextField();

        txtKilos.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void update() {
                try {
                    double k = Double.parseDouble(
                            txtKilos.getText().trim().replace(",", "."));
                    lblCosto.setText(String.format("S/ %.2f", k * PRECIO_KILO));
                    LocalDateTime est = NodoLavanderia.calcularFechaEntrega(
                            LocalDateTime.now(), k);
                    lblEntrega.setText(est.format(FMT));
                } catch (Exception ex) {
                    lblCosto.setText("S/ 0.00");
                    lblEntrega.setText("-");
                }
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        panel.add(new JLabel("N de habitacion:"));     panel.add(txtHabitacion);
        panel.add(new JLabel("Nombre del huesped:"));  panel.add(txtHuesped);
        panel.add(new JLabel("Kilos de ropa:"));       panel.add(txtKilos);
        panel.add(new JLabel("Costo (S/5 x kilo):"));  panel.add(lblCosto);
        panel.add(new JLabel("Entrega estimada:"));    panel.add(lblEntrega);
        panel.add(new JLabel("Tipo de ropa:"));        panel.add(cboTipo);
        panel.add(new JLabel("Observaciones:"));       panel.add(txtObs);

        JButton btnGuardar  = crearBtn("Registrar pedido", new Color(26, 26, 46));
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dlg.dispose());

        btnGuardar.addActionListener(e -> {
            String hab      = txtHabitacion.getText().trim();
            String nombre   = txtHuesped.getText().trim();
            String kilosStr = txtKilos.getText().trim().replace(",", ".");
            if (hab.isEmpty() || nombre.isEmpty() || kilosStr.isEmpty()) {
                JOptionPane.showMessageDialog(dlg,
                        "Completa todos los campos obligatorios.",
                        "Incompleto", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                double kilos = Double.parseDouble(kilosStr);
                if (kilos <= 0) throw new NumberFormatException();
                double costo  = kilos * PRECIO_KILO;
                String tipo   = tipos[cboTipo.getSelectedIndex()];
                String obs    = txtObs.getText().trim();
                LocalDateTime entregaEst = NodoLavanderia
                        .calcularFechaEntrega(LocalDateTime.now(), kilos);

                guardarEnBD(hab, nombre, kilos, costo, tipo, obs, entregaEst);
                cargarDesdeBD();
                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                        "<html>Pedido registrado.<br>"
                                + "Costo: <b>S/" + String.format("%.2f", costo) + "</b><br>"
                                + "Entrega estimada: <b>" + entregaEst.format(FMT) + "</b></html>",
                        "Pedido registrado", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg,
                        "Ingresa un numero valido de kilos.",
                        "Dato invalido", JOptionPane.WARNING_MESSAGE);
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bottom.add(btnCancelar);
        bottom.add(btnGuardar);
        dlg.setLayout(new BorderLayout());
        dlg.add(panel, BorderLayout.CENTER);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void guardarEnBD(String numHab, String nombre, double kilos,
                             double costo, String tipo, String obs, LocalDateTime entregaEst) {
        String sql = "INSERT INTO lavanderia "
                + "(reserva_id, habitacion_id, nombre_huesped, kilos, "
                + "costo_total, tipo, estado, observaciones, usuario_id, "
                + "fecha_entrega_estimada) "
                + "SELECT ISNULL((SELECT TOP 1 r.id FROM reservas r "
                + "  JOIN habitaciones h2 ON h2.id = r.habitacion_id "
                + "  WHERE h2.numero = ? AND r.estado = 'ACTIVA' "
                + "  ORDER BY r.fecha_entrada DESC), 0), "
                + "h.id, ?, ?, ?, ?, 'PENDIENTE', ?, ?, ? "
                + "FROM habitaciones h WHERE h.numero = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numHab);
            ps.setString(2, nombre);
            ps.setDouble(3, kilos);
            ps.setDouble(4, costo);
            ps.setString(5, tipo);
            ps.setString(6, obs);
            ps.setInt(7, usuario.getId());
            ps.setTimestamp(8, Timestamp.valueOf(entregaEst));
            ps.setString(9, numHab);
            int rows = ps.executeUpdate();
            if (rows == 0) {
                JOptionPane.showMessageDialog(this,
                        "No se encontro la habitacion " + numHab + ".",
                        "Habitacion no encontrada", JOptionPane.WARNING_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editarPedido() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { avisar("Selecciona un pedido de la tabla."); return; }

        int    id          = (int)    modeloTabla.getValueAt(fila, 0);
        String hab         = (String) modeloTabla.getValueAt(fila, 1);
        String huesped     = (String) modeloTabla.getValueAt(fila, 2);
        String kilosStr    = modeloTabla.getValueAt(fila, 3).toString().replace(" kg","");
        String fechaEstStr = modeloTabla.getValueAt(fila, 8).toString();
        String obsStr      = modeloTabla.getValueAt(fila, 9).toString();

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                "Editar pedido — Hab." + hab + " | " + huesped,
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(460, 320);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.setBorder(new EmptyBorder(20, 24, 10, 24));

        // Info no editable
        JLabel lblHab     = new JLabel("Hab. " + hab + " — " + huesped);
        lblHab.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHab.setForeground(new Color(26, 26, 46));

        // Campos editables
        JTextField txtKilos      = new JTextField(kilosStr);
        JLabel     lblCosto      = new JLabel("S/ " + String.format("%.2f",
                Double.parseDouble(kilosStr) * PRECIO_KILO));
        lblCosto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblCosto.setForeground(new Color(46, 125, 50));

        JTextField txtFechaEst   = new JTextField(fechaEstStr.equals("-") ? "" : fechaEstStr);
        txtFechaEst.setToolTipText("Formato: dd/MM/yyyy HH:mm  Ej: 09/06/2026 18:00");

        JTextField txtObs        = new JTextField(obsStr.equals("-") ? "" : obsStr);

        // Recalcular costo al cambiar kilos
        txtKilos.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void update() {
                try {
                    double k = Double.parseDouble(txtKilos.getText().trim().replace(",","."));
                    lblCosto.setText(String.format("S/ %.2f", k * PRECIO_KILO));
                    LocalDateTime est = NodoLavanderia.calcularFechaEntrega(LocalDateTime.now(), k);
                    if (txtFechaEst.getText().trim().isEmpty())
                        txtFechaEst.setText(est.format(FMT));
                } catch (Exception ignored) {}
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { update(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { update(); }
        });

        panel.add(new JLabel("Huesped:"));              panel.add(lblHab);
        panel.add(new JLabel("Kilos de ropa:"));        panel.add(txtKilos);
        panel.add(new JLabel("Nuevo costo:"));          panel.add(lblCosto);
        panel.add(new JLabel("Entrega estimada:"));     panel.add(txtFechaEst);
        panel.add(new JLabel("  (dd/MM/yyyy HH:mm)"));
        panel.add(new JLabel("Ej: 09/06/2026 18:00"));
        panel.add(new JLabel("Observaciones:"));        panel.add(txtObs);

        JButton btnGuardar  = crearBtn("Guardar cambios", new Color(230, 81, 0));
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dlg.dispose());

        btnGuardar.addActionListener(e -> {
            try {
                double kilos = Double.parseDouble(
                        txtKilos.getText().trim().replace(",","."));
                if (kilos <= 0) throw new NumberFormatException();
                double costo  = kilos * PRECIO_KILO;
                String obs    = txtObs.getText().trim();
                String fechaTxt = txtFechaEst.getText().trim();

                // Parsear fecha estimada
                LocalDateTime nuevaFecha = null;
                if (!fechaTxt.isEmpty()) {
                    try {
                        nuevaFecha = LocalDateTime.parse(fechaTxt, FMT);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dlg,
                                "Formato de fecha invalido.\nUsa: dd/MM/yyyy HH:mm\nEj: 09/06/2026 18:00",
                                "Fecha invalida", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }

                // Actualizar en BD
                String sql = "UPDATE lavanderia SET kilos=?, costo_total=?, "
                        + "observaciones=?, fecha_entrega_estimada=? WHERE id=?";
                try (Connection con = Conexion.getConexion();
                     PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setDouble(1, kilos);
                    ps.setDouble(2, costo);
                    ps.setString(3, obs);
                    if (nuevaFecha != null)
                        ps.setTimestamp(4, Timestamp.valueOf(nuevaFecha));
                    else
                        ps.setNull(4, java.sql.Types.TIMESTAMP);
                    ps.setInt(5, id);
                    ps.executeUpdate();
                }

                cargarDesdeBD();
                dlg.dispose();
                JOptionPane.showMessageDialog(this,
                        "<html>Pedido actualizado.<br>"
                                + "Kilos: <b>" + kilos + "</b> — Costo: <b>S/"
                                + String.format("%.2f", costo) + "</b><br>"
                                + (nuevaFecha != null
                                ? "Entrega estimada: <b>" + nuevaFecha.format(FMT) + "</b>"
                                : "") + "</html>",
                        "Actualizado", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg,
                        "Ingresa un numero valido de kilos.",
                        "Dato invalido", JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg,
                        "Error al guardar: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        bottom.add(btnCancelar);
        bottom.add(btnGuardar);
        dlg.setLayout(new BorderLayout());
        dlg.add(panel, BorderLayout.CENTER);
        dlg.add(bottom, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void cambiarEstado(String nuevoEstado) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { avisar("Selecciona un pedido de la tabla."); return; }
        int id = (int) modeloTabla.getValueAt(fila, 0);
        lista.actualizarEstado(id, nuevoEstado);

        String sql = "UPDATE lavanderia SET estado = ?"
                + ("ENTREGADO".equals(nuevoEstado) ? ", fecha_entrega = GETDATE()" : "")
                + " WHERE id = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, id);
            ps.executeUpdate();
            cargarDesdeBD();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { avisar("Selecciona un pedido."); return; }
        int id     = (int)    modeloTabla.getValueAt(fila, 0);
        String nom = (String) modeloTabla.getValueAt(fila, 2);
        int conf = JOptionPane.showConfirmDialog(this,
                "Eliminar el pedido de " + nom + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            lista.eliminar(id);
            String sql = "DELETE FROM lavanderia WHERE id = ?";
            try (Connection con = Conexion.getConexion();
                 PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, id);
                ps.executeUpdate();
                cargarDesdeBD();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this,
                        "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JLabel crearStatCard(String titulo, String valor, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220,220,220),1,true),
                new EmptyBorder(10,14,10,14)));
        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTit.setForeground(Color.GRAY);
        card.add(lblTit);
        card.add(Box.createVerticalStrut(4));
        JLabel lblVal = new JLabel(valor);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblVal.setForeground(color);
        card.add(lblVal);
        return lblVal;
    }

    private JButton crearBtn(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return btn;
    }

    private void avisar(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Atencion", JOptionPane.WARNING_MESSAGE);
    }
}