package com.hospedaje.vista;

import com.hospedaje.dao.HabitacionDAO;
import com.hospedaje.modelo.Habitacion;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class PanelHabitaciones extends JPanel {

    private final HabitacionDAO habitacionDAO;
    private JTable            tabla;
    private DefaultTableModel modelo;
    private List<Habitacion>  habitaciones;

    // Campos del formulario lateral
    private JTextField        txtNumero;
    private JSpinner          spnPiso;
    private JComboBox<String> cboTipo;
    private JTextArea         txtDesc;
    private JTextField        txtPrecio;
    private JTextField        txtCama1;
    private JTextField        txtRebaja;
    private JSpinner          spnDias;
    private JCheckBox         chkBanio;
    private JCheckBox         chkEsp;

    public PanelHabitaciones(HabitacionDAO habitacionDAO) {
        this.habitacionDAO = habitacionDAO;
        setLayout(new BorderLayout());
        setBackground(new Color(245, 245, 248));
        construirUI();
        cargarDatos();

        // Aplicar propiedades por código puro al final del constructor
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                                                           boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);

                // Ajustar alineaciones por columna
                switch (col) {
                    case 0: // N°
                    case 1: // Piso
                    case 6: // Dias min.
                    case 7: // Banio
                    case 8: // Estado
                    case 9: // Especial
                        setHorizontalAlignment(SwingConstants.CENTER);
                        break;
                    case 4: // Precio
                        setHorizontalAlignment(SwingConstants.RIGHT);
                        if (val != null) {
                            if (val instanceof Number) {
                                setText(String.format("S/ %.2f", ((Number) val).doubleValue()));
                            } else {
                                String str = val.toString().replace("S/", "").replace("S", "").replace("/", "").trim();
                                try { setText(String.format("S/ %.2f", Double.parseDouble(str))); } catch(Exception e) { setText(val.toString()); }
                            }
                        }
                        break;
                    default:
                        setHorizontalAlignment(SwingConstants.LEFT);
                        break;
                }

                if (!sel && habitaciones != null && row < habitaciones.size()) {
                    Habitacion h = habitaciones.get(row);
                    if (h.isEsCuartoEspecial()) setBackground(new Color(255, 253, 231));
                    else switch (h.getEstado()) {
                        case "DISPONIBLE"    -> setBackground(new Color(232, 245, 233));
                        case "OCUPADO"       -> setBackground(new Color(255, 235, 238));
                        case "MANTENIMIENTO" -> setBackground(new Color(255, 248, 225));
                        default              -> setBackground(Color.WHITE);
                    }
                    setForeground(Color.DARK_GRAY);
                }
                return this;
            }
        });
    }

    private void construirUI() {
        // ── Encabezado ────────────────────────────────────────────────────
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(245, 245, 248));
        encabezado.setBorder(new EmptyBorder(20, 20, 10, 20));

        JLabel titulo = new JLabel("Habitaciones");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        encabezado.add(titulo, BorderLayout.WEST);

        JButton btnRefrescar = new JButton("Refrescar");
        btnRefrescar.addActionListener(e -> cargarDatos());
        encabezado.add(btnRefrescar, BorderLayout.EAST);

        add(encabezado, BorderLayout.NORTH);

        // ── Panel Central Dividido (West: Formulario, Center: Tabla) ──────
        JPanel panelContenido = new JPanel(new BorderLayout(15, 0));
        panelContenido.setOpaque(false);
        panelContenido.setBorder(new EmptyBorder(0, 20, 0, 20));

        // 1. Panel Lateral Izquierdo (Formulario de Datos)
        JPanel panelLateral = new JPanel(new BorderLayout());
        panelLateral.setPreferredSize(new Dimension(290, 0));
        panelLateral.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(210, 210, 215), 1, true),
                "Gestión de Habitaciones",
                javax.swing.border.TitledBorder.LEADING,
                javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 13),
                new Color(26, 26, 46)
        ));

        JPanel panelCampos = new JPanel(new GridBagLayout());
        panelCampos.setBackground(Color.WHITE);

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(4, 8, 4, 8);
        g.weightx = 1.0;

        g.gridx = 0; g.gridy = 0; g.gridwidth = 2;
        panelCampos.add(new JLabel("Número de Habitación *"), g);
        g.gridy = 1;
        txtNumero = new JTextField();
        panelCampos.add(txtNumero, g);

        g.gridy = 2;
        panelCampos.add(new JLabel("Piso"), g);
        g.gridy = 3;
        spnPiso = new JSpinner(new SpinnerNumberModel(2, 1, 10, 1));
        panelCampos.add(spnPiso, g);

        g.gridy = 4;
        panelCampos.add(new JLabel("Tipo"), g);
        g.gridy = 5;
        String[] tipos = {"SIMPLE", "SIMPLE_2P", "MATRIMONIAL", "DOBLE", "INDIVIDUAL"};
        cboTipo = new JComboBox<>(tipos);
        panelCampos.add(cboTipo, g);

        g.gridy = 6;
        panelCampos.add(new JLabel("Descripción"), g);
        g.gridy = 7;
        txtDesc = new JTextArea(3, 20);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);
        JScrollPane scrollDesc = new JScrollPane(txtDesc);
        panelCampos.add(scrollDesc, g);

        g.gridy = 8;
        panelCampos.add(new JLabel("Precio por Noche *"), g);
        g.gridy = 9;
        txtPrecio = new JTextField();
        panelCampos.add(txtPrecio, g);

        g.gridy = 10;
        panelCampos.add(new JLabel("Precio Cama Adicional"), g);
        g.gridy = 11;
        txtCama1 = new JTextField();
        panelCampos.add(txtCama1, g);

        g.gridy = 12;
        panelCampos.add(new JLabel("Rebaja S/ por noche"), g);
        g.gridy = 13;
        txtRebaja = new JTextField("5");
        panelCampos.add(txtRebaja, g);

        g.gridy = 14;
        panelCampos.add(new JLabel("Días Mínimos para Rebaja"), g);
        g.gridy = 15;
        spnDias = new JSpinner(new SpinnerNumberModel(2, 1, 30, 1));
        panelCampos.add(spnDias, g);

        g.gridy = 16;
        chkBanio = new JCheckBox("Tiene Baño Propio");
        panelCampos.add(chkBanio, g);

        g.gridy = 17;
        chkEsp = new JCheckBox("Es Cuarto Especial");
        panelCampos.add(chkEsp, g);

        JPanel panelBotonesForm = new JPanel(new GridLayout(2, 2, 6, 6));
        panelBotonesForm.setBorder(new EmptyBorder(8, 8, 8, 8));
        panelBotonesForm.setOpaque(false);

        JButton btnAgregarForm = crearBtn("+ Agregar", new Color(46, 125, 50));
        btnAgregarForm.addActionListener(e -> agregarHabitacion());

        JButton btnEditarForm = crearBtn("Actualizar", new Color(26, 26, 46));
        btnEditarForm.addActionListener(e -> actualizarHabitacion());

        JButton btnEliminarForm = crearBtn("Eliminar", new Color(198, 40, 40));
        btnEliminarForm.addActionListener(e -> eliminarHabitacion());

        JButton btnLimpiarForm = new JButton("Limpiar");
        btnLimpiarForm.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLimpiarForm.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLimpiarForm.putClientProperty("JButton.arc", 8);
        btnLimpiarForm.addActionListener(e -> limpiarFormulario());

        panelBotonesForm.add(btnAgregarForm);
        panelBotonesForm.add(btnEditarForm);
        panelBotonesForm.add(btnEliminarForm);
        panelBotonesForm.add(btnLimpiarForm);

        JScrollPane scrollCampos = new JScrollPane(panelCampos);
        scrollCampos.setBorder(BorderFactory.createEmptyBorder());
        panelLateral.add(scrollCampos, BorderLayout.CENTER);
        panelLateral.add(panelBotonesForm, BorderLayout.SOUTH);

        panelContenido.add(panelLateral, BorderLayout.WEST);

        // 2. Panel Central (Tabla del Catálogo)
        JPanel panelTablaContenedor = new JPanel(new BorderLayout(0, 10));
        panelTablaContenedor.setOpaque(false);

        String[] cols = {"N°","Piso","Tipo","Descripcion","Precio","Rebaja","Dias min.","Banio","Estado","Especial"};
        modelo = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(modelo);
        tabla.setRowHeight(28);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setGridColor(new Color(220, 220, 220));

        // Listener de selección para rellenar campos
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                cargarSeleccion();
            }
        });

        int[] anchos = {45,45,110,220,65,65,65,55,100,65};
        for (int i = 0; i < anchos.length; i++)
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(210, 210, 215), 1, true));
        panelTablaContenedor.add(scrollTabla, BorderLayout.CENTER);

        // 3. Barra de Herramientas de Estado Rápido (South del panel de tabla)
        JPanel barraHerramientas = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        barraHerramientas.setOpaque(false);

        JButton btnMant = crearBtn("Mantenimiento", new Color(255, 167, 38)); // Naranja
        btnMant.addActionListener(e -> cambiarEstado("MANTENIMIENTO"));

        JButton btnDisp = crearBtn("Marcar disponible", new Color(46, 125, 50)); // Verde
        btnDisp.addActionListener(e -> cambiarEstado("DISPONIBLE"));

        barraHerramientas.add(btnMant);
        barraHerramientas.add(btnDisp);
        panelTablaContenedor.add(barraHerramientas, BorderLayout.SOUTH);

        panelContenido.add(panelTablaContenedor, BorderLayout.CENTER);

        add(panelContenido, BorderLayout.CENTER);
    }

    private JButton crearBtn(String texto, Color bg) {
        JButton btn = new JButton(texto);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.arc", 8);
        return btn;
    }

    private void cargarDatos() {
        modelo.setRowCount(0);
        habitaciones = habitacionDAO.listarTodas();
        for (Habitacion h : habitaciones) {
            modelo.addRow(new Object[]{
                    h.getNumero(), h.getPiso(), h.getTipoLabel(), h.getDescripcion(),
                    "S/ " + h.getPrecioNoche(),
                    h.getPrecioRebaja() != null ? "S/ " + h.getPrecioRebaja() : "-",
                    h.getDiasMinRebaja() + "+ dias",
                    h.isTieneBanioPropio() ? "Si" : "No",
                    h.getEstado(),
                    h.isEsCuartoEspecial() ? "Si" : "No"
            });
        }
    }

    private void cargarSeleccion() {
        int selected = tabla.getSelectedRow();
        if (habitaciones != null && selected >= 0 && selected < habitaciones.size()) {
            Habitacion h = habitaciones.get(selected);
            txtNumero.setText(h.getNumero());
            spnPiso.setValue(h.getPiso());
            cboTipo.setSelectedItem(h.getTipo());
            txtDesc.setText(h.getDescripcion());
            txtPrecio.setText(h.getPrecioNoche() != null ? h.getPrecioNoche().toPlainString() : "");
            txtCama1.setText(h.getPrecioCama1() != null ? h.getPrecioCama1().toPlainString() : "");
            txtRebaja.setText(h.getPrecioRebaja() != null ? h.getPrecioRebaja().toPlainString() : "");
            spnDias.setValue(h.getDiasMinRebaja());
            chkBanio.setSelected(h.isTieneBanioPropio());
            chkEsp.setSelected(h.isEsCuartoEspecial());
        }
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        txtNumero.setText("");
        spnPiso.setValue(2);
        cboTipo.setSelectedIndex(0);
        txtDesc.setText("");
        txtPrecio.setText("");
        txtCama1.setText("");
        txtRebaja.setText("5");
        spnDias.setValue(2);
        chkBanio.setSelected(false);
        chkEsp.setSelected(false);
    }

    private void agregarHabitacion() {
        try {
            Habitacion h = new Habitacion();
            h.setNumero(txtNumero.getText().trim());
            if (h.getNumero().isEmpty()) { avisar("El número de habitación es obligatorio."); return; }
            h.setPiso((int) spnPiso.getValue());
            h.setTipo((String) cboTipo.getSelectedItem());
            h.setDescripcion(txtDesc.getText().trim());
            h.setPrecioNoche(new BigDecimal(txtPrecio.getText().trim()));

            String pc = txtCama1.getText().trim();
            h.setPrecioCama1(pc.isEmpty() ? null : new BigDecimal(pc));
            String reb = txtRebaja.getText().trim();
            h.setPrecioRebaja(reb.isEmpty() ? null : new BigDecimal(reb));
            h.setDiasMinRebaja((int) spnDias.getValue());
            h.setTieneBanioPropio(chkBanio.isSelected());
            h.setEsCuartoEspecial(chkEsp.isSelected());

            boolean ok = habitacionDAO.insertar(h);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Habitación agregada.");
                cargarDatos();
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "Error al guardar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Verifica que los precios sean números válidos.", "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void actualizarHabitacion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { avisar("Selecciona una habitación de la tabla para editar."); return; }
        Habitacion h = habitaciones.get(fila);
        try {
            h.setNumero(txtNumero.getText().trim());
            if (h.getNumero().isEmpty()) { avisar("El número de habitación es obligatorio."); return; }
            h.setPiso((int) spnPiso.getValue());
            h.setTipo((String) cboTipo.getSelectedItem());
            h.setDescripcion(txtDesc.getText().trim());
            h.setPrecioNoche(new BigDecimal(txtPrecio.getText().trim()));

            String pc = txtCama1.getText().trim();
            h.setPrecioCama1(pc.isEmpty() ? null : new BigDecimal(pc));
            String reb = txtRebaja.getText().trim();
            h.setPrecioRebaja(reb.isEmpty() ? null : new BigDecimal(reb));
            h.setDiasMinRebaja((int) spnDias.getValue());
            h.setTieneBanioPropio(chkBanio.isSelected());
            h.setEsCuartoEspecial(chkEsp.isSelected());

            boolean ok = habitacionDAO.actualizar(h);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Habitación actualizada.");
                cargarDatos();
                limpiarFormulario();
            } else {
                JOptionPane.showMessageDialog(this, "Error al actualizar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Verifica que los precios sean números válidos.", "Datos inválidos", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void eliminarHabitacion() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { avisar("Selecciona una habitación de la tabla primero."); return; }
        Habitacion h = habitaciones.get(fila);
        if (h.isOcupado()) { JOptionPane.showMessageDialog(this, "No puedes eliminar una habitación ocupada.", "Ocupada", JOptionPane.WARNING_MESSAGE); return; }
        int conf = JOptionPane.showConfirmDialog(this,
                "¿Eliminar habitación " + h.getNumero() + "?",
                "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (conf == JOptionPane.YES_OPTION) {
            if (habitacionDAO.eliminar(h.getId())) {
                cargarDatos();
                limpiarFormulario();
                JOptionPane.showMessageDialog(this, "Habitación eliminada.");
            } else {
                JOptionPane.showMessageDialog(this, "Error al eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cambiarEstado(String nuevoEstado) {
        int fila = tabla.getSelectedRow();
        if (fila < 0) { avisar("Selecciona una habitación."); return; }
        Habitacion h = habitaciones.get(fila);
        if ("OCUPADO".equals(h.getEstado())) { JOptionPane.showMessageDialog(this, "No puedes cambiar el estado de una habitación ocupada.", "Ocupada", JOptionPane.WARNING_MESSAGE); return; }
        if (habitacionDAO.actualizarEstado(h.getId(), nuevoEstado)) {
            cargarDatos();
            limpiarFormulario();
        }
    }

    private void avisar(String msg) { JOptionPane.showMessageDialog(this, msg, "Atención", JOptionPane.WARNING_MESSAGE); }
}