package com.hospedaje.vista;

import com.hospedaje.dao.ReservaDAO;
import com.hospedaje.modelo.Reserva;
import com.hospedaje.modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableRowSorter;
import javax.swing.RowFilter;
import java.awt.*;
import java.util.List;

/**
 * Panel que muestra las reservas activas del día
 * y permite finalizar el alojamiento de un huésped.
 */
public class PanelReservas extends JPanel {

    private final ReservaDAO reservaDAO = new ReservaDAO();
    private final Usuario    usuario;
    private final MainFrame  mainFrame;

    private JTable          tabla;
    private DefaultTableModel modelo;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField      txtBuscar;
    private JButton         btnFinalizar;
    private JLabel          lblHuespedesCount;
    private JLabel          lblTotal;

    private List<Reserva> reservas;

    public PanelReservas(Usuario usuario, MainFrame mainFrame) {
        this.usuario   = usuario;
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(0, 0));
        setBackground(new Color(245, 245, 248));
        construirUI();
        cargarDatos();

        // Renderizador para alinear al centro
        javax.swing.table.DefaultTableCellRenderer centerRenderer = new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? java.awt.Color.WHITE : new java.awt.Color(248, 248, 250));
                }
                return this;
            }
        };

        // Aplicar centro a las columnas: Hab (0), DNI (2), Entrada (5), Vence (6), Estado (7)
        tabla.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tabla.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);
        tabla.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tabla.getColumnModel().getColumn(6).setCellRenderer(centerRenderer);
        tabla.getColumnModel().getColumn(7).setCellRenderer(centerRenderer);

        // Renderizador para la columna Precio (4) para poner 'S/ ' y alinear a la derecha
        tabla.getColumnModel().getColumn(4).setCellRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public java.awt.Component getTableCellRendererComponent(javax.swing.JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
                if (value != null) {
                    if (value instanceof Number) {
                        setText(String.format("S/ %.2f", ((Number) value).doubleValue()));
                    } else {
                        String str = value.toString().replace("S/", "").trim();
                        try { setText(String.format("S/ %.2f", Double.parseDouble(str))); } catch(Exception e) { setText(value.toString()); }
                    }
                }
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? java.awt.Color.WHITE : new java.awt.Color(248, 248, 250));
                }
                return this;
            }
        });
    }

    private void construirUI() {
        // ── Encabezado / Filtros ──────────────────────────────────────────
        JPanel encabezado = new JPanel(new BorderLayout(15, 0));
        encabezado.setBackground(new Color(245, 245, 248));
        encabezado.setBorder(new EmptyBorder(15, 20, 10, 20));

        JPanel panelIzquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panelIzquierda.setOpaque(false);

        JLabel lblTitulo = new JLabel("Huéspedes activos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panelIzquierda.add(lblTitulo);

        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(2, 20));
        panelIzquierda.add(sep);

        JLabel lblBuscar = new JLabel("Buscar Huésped:");
        lblBuscar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panelIzquierda.add(lblBuscar);

        txtBuscar = new JTextField(20);
        txtBuscar.putClientProperty("JTextField.placeholderText", "Ingrese DNI o Apellido...");
        txtBuscar.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filtrar(); }

            private void filtrar() {
                String texto = txtBuscar.getText().trim();
                if (texto.isEmpty()) {
                    sorter.setRowFilter(null);
                } else {
                    sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(texto), 1, 2));
                }
                actualizarMetricas();
            }
        });
        panelIzquierda.add(txtBuscar);

        encabezado.add(panelIzquierda, BorderLayout.WEST);

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarDatos());
        encabezado.add(btnRefrescar, BorderLayout.EAST);

        add(encabezado, BorderLayout.NORTH);

        // ── Tabla ─────────────────────────────────────────────────────────
        String[] columnas = {"Hab.", "Huésped", "DNI", "Tipo", "Precio", "Entrada", "Vence", "Estado"};
        modelo = new DefaultTableModel(columnas, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setGridColor(new Color(220, 220, 220));
        tabla.setShowGrid(true);

        sorter = new TableRowSorter<>(modelo);
        tabla.setRowSorter(sorter);

        // Anchos de columnas
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(160);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(6).setPreferredWidth(110);
        tabla.getColumnModel().getColumn(7).setPreferredWidth(70);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 215), 1, true));

        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBorder(new EmptyBorder(0, 20, 0, 20));
        panelTabla.setOpaque(false);
        panelTabla.add(scroll);
        add(panelTabla, BorderLayout.CENTER);

        // ── Barra inferior ────────────────────────────────────────────────
        JPanel barraInferior = new JPanel(new BorderLayout());
        barraInferior.setBackground(new Color(245, 245, 248));
        barraInferior.setBorder(new EmptyBorder(10, 20, 20, 20));

        // Izquierda: Métricas
        JPanel panelMetricas = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        panelMetricas.setOpaque(false);

        lblHuespedesCount = new JLabel("Huéspedes en casa: 0");
        lblHuespedesCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHuespedesCount.setForeground(new Color(21, 129, 192)); // Azul

        lblTotal = new JLabel("Total ingresos hoy: S/ 0.00");
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTotal.setForeground(new Color(46, 125, 50)); // Verde

        panelMetricas.add(lblHuespedesCount);
        panelMetricas.add(lblTotal);
        barraInferior.add(panelMetricas, BorderLayout.WEST);

        // Derecha: Botones de Acción
        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelAcciones.setOpaque(false);

        JButton btnDetalles = new JButton("Ver Detalles");
        btnDetalles.setBackground(new Color(100, 110, 120));
        btnDetalles.setForeground(Color.WHITE);
        btnDetalles.setFocusPainted(false);
        btnDetalles.setBorderPainted(false);
        btnDetalles.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDetalles.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnDetalles.putClientProperty("JButton.arc", 8);
        btnDetalles.addActionListener(e -> verDetalles());
        panelAcciones.add(btnDetalles);

        btnFinalizar = new JButton("Finalizar alojamiento");
        btnFinalizar.setBackground(new Color(198, 40, 40, 255));
        btnFinalizar.setForeground(Color.WHITE);
        btnFinalizar.setFocusPainted(false);
        btnFinalizar.setBorderPainted(false);
        btnFinalizar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnFinalizar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnFinalizar.putClientProperty("JButton.arc", 8);
        btnFinalizar.addActionListener(e -> finalizarReserva());
        panelAcciones.add(btnFinalizar);

        barraInferior.add(panelAcciones, BorderLayout.EAST);

        add(barraInferior, BorderLayout.SOUTH);
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        reservas = reservaDAO.listarActivas();

        for (Reserva r : reservas) {
            modelo.addRow(new Object[]{
                    r.getNumeroHabitacion(),
                    r.getNombreHuesped(),
                    r.getDniHuesped(),
                    r.getTipoHabitacion(),
                    "S/ " + r.getPrecioCobrado().toPlainString(),
                    r.getFechaEntradaStr(),
                    r.getFechaVencimientoStr(),
                    r.getEstado()
            });
        }
        actualizarMetricas();
    }

    private void actualizarMetricas() {
        int count = tabla.getRowCount();
        double total = 0;
        for (int i = 0; i < count; i++) {
            int modelRow = tabla.convertRowIndexToModel(i);
            Reserva r = reservas.get(modelRow);
            total += r.getPrecioCobrado().doubleValue();
        }

        if (lblHuespedesCount != null) {
            lblHuespedesCount.setText("Huéspedes en casa: " + count);
        }
        if (lblTotal != null) {
            lblTotal.setText(String.format("Total ingresos hoy: S/ %.2f", total));
        }
    }

    private void verDetalles() {
        int viewRow = tabla.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un huésped de la tabla primero.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tabla.convertRowIndexToModel(viewRow);
        Reserva r = reservas.get(modelRow);

        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Detalles de la Reserva", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(450, 360);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 6, 6, 6);

        java.util.function.BiConsumer<String, String> addRow = (label, val) -> {
            gbc.gridx = 0;
            JLabel lblLabel = new JLabel(label);
            lblLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblLabel.setForeground(Color.GRAY);
            panel.add(lblLabel, gbc);

            gbc.gridx = 1;
            JLabel lblVal = new JLabel(val);
            lblVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblVal.setForeground(Color.DARK_GRAY);
            panel.add(lblVal, gbc);

            gbc.gridy++;
        };

        gbc.gridy = 0;
        addRow.accept("Habitación:", r.getNumeroHabitacion() + " (" + r.getTipoHabitacion() + ")");
        addRow.accept("Huésped:", r.getNombreHuesped());
        addRow.accept("DNI:", r.getDniHuesped());
        addRow.accept("Precio Cobrado:", "S/ " + String.format("%.2f", r.getPrecioCobrado().doubleValue()));
        addRow.accept("Fecha Entrada:", r.getFechaEntradaStr());
        addRow.accept("Fecha Vencimiento:", r.getFechaVencimientoStr());
        addRow.accept("Estado:", r.getEstado());

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(15, 0, 0, 0);

        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dlg.dispose());
        panel.add(btnCerrar, gbc);

        dlg.add(panel);
        dlg.setVisible(true);
    }

    private void finalizarReserva() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Selecciona un huésped de la tabla primero.",
                    "Sin selección", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = tabla.convertRowIndexToModel(fila);
        Reserva r = reservas.get(modelRow);
        int confirm = JOptionPane.showConfirmDialog(this,
                "<html>¿Finalizar alojamiento de <b>" + r.getNombreHuesped() + "</b>?<br>"
                        + "Habitación " + r.getNumeroHabitacion() + " quedará disponible.</html>",
                "Confirmar salida", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = reservaDAO.finalizarReserva(r.getId(), r.getHabitacionId());
            if (ok) {
                JOptionPane.showMessageDialog(this,
                        "Alojamiento finalizado. Habitación liberada.",
                        "Listo", JOptionPane.INFORMATION_MESSAGE);
                cargarDatos();
                mainFrame.refrescar();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Error al finalizar. Intenta nuevamente.",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}