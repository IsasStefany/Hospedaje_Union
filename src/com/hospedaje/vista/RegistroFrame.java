package com.hospedaje.vista;

import com.hospedaje.dao.HabitacionDAO;
import com.hospedaje.dao.ReservaDAO;
import com.hospedaje.modelo.*;
import com.hospedaje.util.BoletaGenerator;
import com.hospedaje.util.CalculadorPrecio;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RegistroFrame extends JDialog {

    private final Usuario       usuarioActual;
    private final MainFrame     parent;
    private final HabitacionDAO habitacionDAO = new HabitacionDAO();
    private final ReservaDAO    reservaDAO    = new ReservaDAO();

    private JTextField   txtNombres, txtApellidos, txtDni, txtProcedencia, txtTelefono, txtBoleta;
    private JComboBox<String>     cboMotivo;
    private JComboBox<Habitacion> cboHabitacion;
    private JComboBox<String>     cboMetodoPago;
    private JCheckBox    chkUnaCama;
    private JSpinner     spnDias;
    private JLabel       lblPrecio, lblVencimiento, lblDesglose, lblMetodoPagoInfo;
    private JButton      btnRegistrar;

    private List<Habitacion> habitacionesDisponibles;

    public RegistroFrame(MainFrame parent, Usuario usuario) {
        super(parent, "Nuevo registro de huesped", true);
        this.parent        = parent;
        this.usuarioActual = usuario;
        setSize(700, 700);
        setLocationRelativeTo(parent);
        setResizable(false);
        construirUI();
        cargarHabitaciones();
    }

    private void construirUI() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(20, 24, 20, 24));
        panel.setBackground(Color.WHITE);

        // ── Datos del huesped ──────────────────────────────────────────
        panel.add(crearTitulo("Datos del huesped"));
        panel.add(Box.createVerticalStrut(10));

        JPanel grid1 = new JPanel(new GridLayout(0, 2, 12, 8));
        grid1.setOpaque(false);
        txtNombres     = campo(grid1, "Nombres *");
        txtApellidos   = campo(grid1, "Apellidos *");
        txtDni         = campo(grid1, "DNI *");
        txtProcedencia = campo(grid1, "Procedencia");
        txtTelefono    = campo(grid1, "Telefono");

        cboMotivo = new JComboBox<>(new String[]{"Trabajo","Turismo","Salud","Comercio","Tramites","Otro"});
        JPanel mp = new JPanel(new BorderLayout(0,3)); mp.setOpaque(false);
        mp.add(new JLabel("Motivo de visita"), BorderLayout.NORTH);
        mp.add(cboMotivo, BorderLayout.CENTER);
        grid1.add(mp);
        panel.add(grid1);
        panel.add(Box.createVerticalStrut(16));

        // ── Habitacion y precio ────────────────────────────────────────
        panel.add(crearTitulo("Habitacion y precio"));
        panel.add(Box.createVerticalStrut(10));

        JPanel grid2 = new JPanel(new GridLayout(0, 2, 12, 8));
        grid2.setOpaque(false);

        JPanel habPanel = new JPanel(new BorderLayout(0,3));
        habPanel.setOpaque(false);
        habPanel.add(new JLabel("Habitacion disponible *"), BorderLayout.NORTH);
        cboHabitacion = new JComboBox<>();
        cboHabitacion.addActionListener(e -> actualizarPrecio());
        habPanel.add(cboHabitacion, BorderLayout.CENTER);
        grid2.add(habPanel);

        JPanel diasPanel = new JPanel(new BorderLayout(0,3));
        diasPanel.setOpaque(false);
        diasPanel.add(new JLabel("Dias de alojamiento"), BorderLayout.NORTH);
        spnDias = new JSpinner(new SpinnerNumberModel(1, 1, 365, 1));
        spnDias.addChangeListener(e -> actualizarPrecio());
        diasPanel.add(spnDias, BorderLayout.CENTER);
        grid2.add(diasPanel);

        JPanel precioPanel = new JPanel(new BorderLayout(0,3));
        precioPanel.setOpaque(false);
        precioPanel.add(new JLabel("Total a cobrar"), BorderLayout.NORTH);
        lblPrecio = new JLabel("S/ -");
        lblPrecio.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblPrecio.setForeground(new Color(46, 125, 50));
        precioPanel.add(lblPrecio, BorderLayout.CENTER);
        grid2.add(precioPanel);

        JPanel camaPanel = new JPanel(new BorderLayout(0,3));
        camaPanel.setOpaque(false);
        camaPanel.add(new JLabel(" "), BorderLayout.NORTH);
        chkUnaCama = new JCheckBox("Solo usa 1 cama (cuartos dobles)");
        chkUnaCama.setOpaque(false);
        chkUnaCama.setVisible(false);
        chkUnaCama.addActionListener(e -> actualizarPrecio());
        camaPanel.add(chkUnaCama, BorderLayout.CENTER);
        grid2.add(camaPanel);

        txtBoleta = campo(grid2, "N de boleta *");
        panel.add(grid2);
        panel.add(Box.createVerticalStrut(10));

        // ── Metodo de pago ─────────────────────────────────────────────
        panel.add(crearTitulo("Metodo de pago"));
        panel.add(Box.createVerticalStrut(8));

        JPanel gridPago = new JPanel(new GridLayout(1, 2, 12, 0));
        gridPago.setOpaque(false);

        JPanel pagoPanel = new JPanel(new BorderLayout(0,3));
        pagoPanel.setOpaque(false);
        pagoPanel.add(new JLabel("Forma de pago *"), BorderLayout.NORTH);
        cboMetodoPago = new JComboBox<>(new String[]{
                "EFECTIVO", "YAPE", "VISA", "BCP"
        });
        cboMetodoPago.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cboMetodoPago.addActionListener(e -> actualizarInfoPago());
        pagoPanel.add(cboMetodoPago, BorderLayout.CENTER);
        gridPago.add(pagoPanel);

        JPanel infoPanel = new JPanel(new BorderLayout(0,3));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("Info del metodo"), BorderLayout.NORTH);
        lblMetodoPagoInfo = new JLabel("Pago en efectivo al momento del registro");
        lblMetodoPagoInfo.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblMetodoPagoInfo.setForeground(new Color(80, 80, 80));
        infoPanel.add(lblMetodoPagoInfo, BorderLayout.CENTER);
        gridPago.add(infoPanel);

        panel.add(gridPago);
        panel.add(Box.createVerticalStrut(10));

        // ── Desglose precio ────────────────────────────────────────────
        lblDesglose = new JLabel(" ");
        lblDesglose.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesglose.setForeground(new Color(21, 101, 192));
        lblDesglose.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(144, 202, 249), 1, true),
                new EmptyBorder(5, 8, 5, 8)));
        lblDesglose.setOpaque(true);
        lblDesglose.setBackground(new Color(227, 242, 253));
        panel.add(lblDesglose);
        panel.add(Box.createVerticalStrut(8));

        // ── Vencimiento ────────────────────────────────────────────────
        lblVencimiento = new JLabel("Vence: -");
        lblVencimiento.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblVencimiento.setForeground(new Color(198, 40, 40));
        lblVencimiento.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 183, 77), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        lblVencimiento.setOpaque(true);
        lblVencimiento.setBackground(new Color(255, 248, 225));
        panel.add(lblVencimiento);
        actualizarVencimiento();
        panel.add(Box.createVerticalStrut(16));

        // ── Botones ────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);
        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> dispose());
        btnRegistrar = new JButton("Registrar y emitir boleta");
        btnRegistrar.setBackground(new Color(26, 26, 46));
        btnRegistrar.setForeground(Color.WHITE);
        btnRegistrar.setFocusPainted(false);
        btnRegistrar.setBorderPainted(false);
        btnRegistrar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegistrar.addActionListener(e -> registrar());
        btnPanel.add(btnCancelar);
        btnPanel.add(btnRegistrar);
        panel.add(btnPanel);

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        setContentPane(scroll);
    }

    private JTextField campo(JPanel panel, String etiqueta) {
        JPanel p = new JPanel(new BorderLayout(0,3)); p.setOpaque(false);
        p.add(new JLabel(etiqueta), BorderLayout.NORTH);
        JTextField tf = new JTextField(); p.add(tf, BorderLayout.CENTER);
        panel.add(p); return tf;
    }

    private JLabel crearTitulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(new Color(26, 26, 46));
        return lbl;
    }

    private void cargarHabitaciones() {
        habitacionesDisponibles = habitacionDAO.listarDisponibles();
        cboHabitacion.removeAllItems();
        for (Habitacion h : habitacionesDisponibles) cboHabitacion.addItem(h);
        actualizarPrecio();
    }

    private void actualizarPrecio() {
        Habitacion h = (Habitacion) cboHabitacion.getSelectedItem();
        if (h == null) { lblPrecio.setText("S/ -"); lblDesglose.setText(" "); return; }
        boolean esDoble = "DOBLE".equals(h.getTipo());
        chkUnaCama.setVisible(esDoble);
        int dias    = (int) spnDias.getValue();
        int usoCamas = (esDoble && chkUnaCama.isSelected()) ? 1 : 2;
        BigDecimal total   = CalculadorPrecio.calcularTotal(h, dias, usoCamas);
        String     desglose = CalculadorPrecio.getDesglose(h, dias, usoCamas);
        lblPrecio.setText("S/ " + total.toPlainString());
        lblDesglose.setText("<html>" + desglose + "</html>");
        actualizarVencimiento();
    }

    private void actualizarVencimiento() {
        int dias = (int) spnDias.getValue();
        LocalDateTime vence = LocalDateTime.now().toLocalDate().plusDays(dias).atTime(12, 0);
        lblVencimiento.setText("Vence: " + vence.format(
                DateTimeFormatter.ofPattern("EEEE dd/MM/yyyy 'a las' HH:mm")));
    }

    private void actualizarInfoPago() {
        String metodo = (String) cboMetodoPago.getSelectedItem();
        lblMetodoPagoInfo.setText(switch (metodo) {
            case "EFECTIVO" -> "Pago en efectivo al momento del registro";
            case "YAPE"     -> "Transferir al numero Yape del hospedaje";
            case "VISA"     -> "Pago con tarjeta Visa — sin recargo adicional";
            case "BCP"      -> "Deposito o transferencia BCP — sin recargo";
            default         -> "";
        });
    }

    private void registrar() {
        if (txtNombres.getText().trim().isEmpty()
                || txtApellidos.getText().trim().isEmpty()
                || txtDni.getText().trim().isEmpty()
                || txtBoleta.getText().trim().isEmpty()
                || cboHabitacion.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this,
                    "Los campos marcados con * son obligatorios.",
                    "Campos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Habitacion hab      = (Habitacion) cboHabitacion.getSelectedItem();
        int        dias     = (int) spnDias.getValue();
        boolean    esDoble  = "DOBLE".equals(hab.getTipo());
        int        usoCamas = (esDoble && chkUnaCama.isSelected()) ? 1 : 2;
        BigDecimal total    = CalculadorPrecio.calcularTotal(hab, dias, usoCamas);
        String     metodo   = (String) cboMetodoPago.getSelectedItem();

        Huesped huesped = new Huesped(
                txtNombres.getText().trim(), txtApellidos.getText().trim(),
                txtDni.getText().trim(), txtProcedencia.getText().trim(),
                (String) cboMotivo.getSelectedItem(), txtTelefono.getText().trim());

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime vence = ahora.toLocalDate().plusDays(dias).atTime(12, 0);

        Reserva reserva = new Reserva();
        reserva.setHabitacionId(hab.getId());
        reserva.setUsuarioId(usuarioActual.getId());
        reserva.setFechaEntrada(ahora);
        reserva.setFechaVencimiento(vence);
        reserva.setPrecioCobrado(total);
        reserva.setUsoCamas(usoCamas);
        reserva.setMetodoPago(metodo);

        btnRegistrar.setEnabled(false);
        btnRegistrar.setText("Guardando...");

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            protected Integer doInBackground() {
                return reservaDAO.registrarHuespedYReserva(huesped, reserva);
            }
            protected void done() {
                try {
                    int reservaId = get();
                    if (reservaId > 0) {
                        reserva.setId(reservaId);
                        reserva.setNombreHuesped(huesped.getNombreCompleto());
                        reserva.setDniHuesped(huesped.getDni());
                        reserva.setNumeroHabitacion(hab.getNumero());
                        reserva.setTipoHabitacion(hab.getTipoLabel());
                        String nroBoleta = txtBoleta.getText().trim();
                        String rutaPdf = BoletaGenerator.generar(reserva, huesped, nroBoleta);
                        JOptionPane.showMessageDialog(RegistroFrame.this,
                                "<html>Huesped registrado.<br>"
                                        + "Metodo de pago: <b>" + metodo + "</b><br>"
                                        + "Boleta N " + nroBoleta + " guardada.</html>",
                                "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
                        parent.refrescar();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(RegistroFrame.this,
                                "Error al guardar. Intenta nuevamente.",
                                "Error", JOptionPane.ERROR_MESSAGE);
                        btnRegistrar.setEnabled(true);
                        btnRegistrar.setText("Registrar y emitir boleta");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    btnRegistrar.setEnabled(true);
                    btnRegistrar.setText("Registrar y emitir boleta");
                }
            }
        };
        worker.execute();
    }
}