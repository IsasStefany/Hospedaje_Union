package com.hospedaje.vista;

import com.hospedaje.dao.HabitacionDAO;
import com.hospedaje.dao.ReservaDAO;
import com.hospedaje.modelo.Habitacion;
import com.hospedaje.modelo.Usuario;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Ventana principal del sistema.
 * Contiene el menú lateral y el panel de contenido central.
 */
public class MainFrame extends JFrame {

    private static final Color COLOR_SIDEBAR = new Color(26, 26, 46);
    private static final Color COLOR_ACTIVO = new Color(45, 45, 78);
    private static final Color COLOR_ACENTO = new Color(79, 142, 247);

    private final Usuario usuarioActual;
    private final HabitacionDAO habitacionDAO = new HabitacionDAO();
    private final ReservaDAO reservaDAO = new ReservaDAO();

    private JPanel panelContenido;
    private JLabel lblLibres, lblOcupados, lblIngresos, lblPorVencer;

    public MainFrame(Usuario usuario) {
        this.usuarioActual = usuario;
        setTitle("Hospedaje Union — " + usuario.getNombre());
        setSize(1000, 650);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 500));
        construirUI();
        cargarPanelPrincipal();
    }

    private void construirUI() {
        setLayout(new BorderLayout());

        // ── Sidebar ───────────────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(COLOR_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(170, 0));
        sidebar.setBorder(new EmptyBorder(0, 0, 0, 0));

        // Logo
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 14));
        logoPanel.setBackground(COLOR_SIDEBAR);
        JLabel logo = new JLabel("<html><b>Hospedaje</b><br>Union</html>");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logoPanel.add(logo);
        sidebar.add(logoPanel);

        // JLabel para mostrar el logo circular debajo del texto
        JLabel lblSidebarLogo = new JLabel();
        lblSidebarLogo.setHorizontalAlignment(SwingConstants.CENTER);
        lblSidebarLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSidebarLogo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        try {
            java.net.URL resource = getClass().getResource("/com/hospedaje/recursos/logo.png");
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                lblSidebarLogo.setIcon(new ImageIcon(img));
            } else {
                lblSidebarLogo.setText("[Logo]");
                lblSidebarLogo.setForeground(Color.GRAY);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar el logo en sidebar: " + e.getMessage());
        }
        sidebar.add(lblSidebarLogo);
        sidebar.add(Box.createVerticalStrut(10));

        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(60, 60, 80));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        sidebar.add(sep);
        sidebar.add(Box.createVerticalStrut(8));

        // Ítems del menú
        sidebar.add(crearMenuBtn("Panel principal", () -> cargarPanelPrincipal()));
        sidebar.add(crearMenuBtn("Nuevo registro", () -> abrirNuevoRegistro()));
        sidebar.add(crearMenuBtn("Huéspedes activos", () -> cargarReservasActivas()));
        sidebar.add(crearMenuBtn("Habitaciones", () -> cargarHabitaciones()));
        sidebar.add(crearMenuBtn("Caja del día", () -> cargarCajaDia()));
        sidebar.add(crearMenuBtn("Reportes", () -> cargarReportes()));

        sidebar.add(Box.createVerticalGlue());

        // Usuario en la parte baja del sidebar
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 10));
        userPanel.setBackground(new Color(20, 20, 36));
        JLabel lblUser = new JLabel("<html><small style='color:#aaa'>" + usuarioActual.getRol()
                + "</small><br>" + usuarioActual.getNombre() + "</html>");
        lblUser.setForeground(Color.WHITE);
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        userPanel.add(lblUser);
        sidebar.add(userPanel);

        add(sidebar, BorderLayout.WEST);

        // ── Panel de contenido ────────────────────────────────────────────
        panelContenido = new JPanel(new BorderLayout());
        panelContenido.setBackground(new Color(245, 245, 248));
        add(panelContenido, BorderLayout.CENTER);
    }

    private JButton crearMenuBtn(String texto, Runnable accion) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setForeground(new Color(180, 180, 200));
        btn.setBackground(COLOR_SIDEBAR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(10, 18, 10, 10));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(COLOR_ACTIVO);
                btn.setForeground(Color.WHITE);
            }

            public void mouseExited(MouseEvent e) {
                btn.setBackground(COLOR_SIDEBAR);
                btn.setForeground(new Color(180, 180, 200));
            }
        });
        btn.addActionListener(e -> accion.run());
        return btn;
    }

    // ── PANEL PRINCIPAL ───────────────────────────────────────────────────

    private void cargarPanelPrincipal() {
        panelContenido.removeAll();

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setBackground(new Color(245, 245, 248));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Título
        JLabel titulo = new JLabel("Ocupación hoy");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        panel.add(titulo, BorderLayout.NORTH);

        // Tarjetas de estadísticas
        JPanel stats = new JPanel(new GridLayout(1, 4, 12, 0));
        stats.setOpaque(false);
        lblLibres = crearStatCard("Disponibles", "—", new Color(46, 125, 50));
        lblOcupados = crearStatCard("Ocupados", "—", new Color(198, 40, 40));
        lblIngresos = crearStatCard("Ingresos hoy", "—", new Color(21, 101, 192));
        lblPorVencer = crearStatCard("Por vencer", "—", new Color(230, 81, 0));
        stats.add(lblLibres.getParent());
        stats.add(lblOcupados.getParent());
        stats.add(lblIngresos.getParent());
        stats.add(lblPorVencer.getParent());

        JPanel centro = new JPanel(new BorderLayout(0, 12));
        centro.setOpaque(false);
        centro.add(stats, BorderLayout.NORTH);

        // Mapa de habitaciones
        List<Habitacion> habitaciones = habitacionDAO.listarTodas();
        JPanel mapa = construirMapaHabitaciones(habitaciones);
        JScrollPane scroll = new JScrollPane(mapa);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        centro.add(scroll, BorderLayout.CENTER);

        panel.add(centro, BorderLayout.CENTER);
        panelContenido.add(panel);

        // Cargar estadísticas en segundo plano
        SwingWorker<double[], Void> worker = new SwingWorker<>() {
            protected double[] doInBackground() {
                return new double[]{
                        habitacionDAO.contarPorEstado("DISPONIBLE"),
                        habitacionDAO.contarPorEstado("OCUPADO"),
                        reservaDAO.totalIngresosHoy(),
                        0 // reservas por vencer (ampliar en siguiente versión)
                };
            }

            protected void done() {
                try {
                    double[] d = get();
                    lblLibres.setText(String.valueOf((int) d[0]));
                    lblOcupados.setText(String.valueOf((int) d[1]));
                    lblIngresos.setText("S/ " + NumberFormat.getInstance(new Locale("es", "PE")).format(d[2]));
                    lblPorVencer.setText(String.valueOf((int) d[3]));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();

        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private JLabel crearStatCard(String titulo, String valor, Color colorValor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(14, 16, 14, 16)));

        JLabel lblTit = new JLabel(titulo);
        lblTit.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTit.setForeground(Color.GRAY);
        card.add(lblTit);
        card.add(Box.createVerticalStrut(6));

        JLabel lblVal = new JLabel(valor);
        lblVal.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblVal.setForeground(colorValor);
        card.add(lblVal);

        return lblVal;
    }

    private JPanel construirMapaHabitaciones(List<Habitacion> habitaciones) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220), 1, true),
                new EmptyBorder(14, 14, 14, 14)));

        JLabel lblTitulo = new JLabel("Mapa de habitaciones — 2do y 3er piso");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setBorder(new EmptyBorder(0, 0, 10, 0));
        wrapper.add(lblTitulo, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(0, 6, 6, 6));
        grid.setBackground(Color.WHITE);

        for (Habitacion h : habitaciones) {
            JButton btn = new JButton("<html><center><b>" + h.getNumero() + "</b><br>"
                    + "<small>" + h.getTipoLabel().split(" ")[0] + "</small></center></html>");
            btn.setBackground(h.getColorEstado());
            btn.setBorderPainted(true);
            btn.setFocusPainted(false);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btn.setToolTipText(h.toString() + " — " + h.getEstado());
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            btn.setPreferredSize(new Dimension(100, 55));
            btn.addActionListener(e -> mostrarDetalleHabitacion(h));
            grid.add(btn);
        }

        wrapper.add(grid, BorderLayout.CENTER);

        // Leyenda
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 4));
        leyenda.setBackground(Color.WHITE);
        leyenda.add(crearLeyenda("Disponible", new Color(232, 245, 233)));
        leyenda.add(crearLeyenda("Ocupado", new Color(255, 235, 238)));
        leyenda.add(crearLeyenda("Mantenimiento", new Color(255, 248, 225)));
        wrapper.add(leyenda, BorderLayout.SOUTH);

        return wrapper;
    }

    private JLabel crearLeyenda(String texto, Color color) {
        JLabel lbl = new JLabel("  " + texto);
        lbl.setOpaque(true);
        lbl.setBackground(color);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        lbl.setPreferredSize(new Dimension(110, 22));
        return lbl;
    }

    private void mostrarDetalleHabitacion(Habitacion h) {
        JDialog dialog = new JDialog(this, "Detalle de Habitación " + h.getNumero(), true);
        dialog.setSize(520, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);
        dialog.setLayout(new BorderLayout());

        // Panel principal con fondo blanco y bordes elegantes
        JPanel mainPanel = new JPanel(new BorderLayout(15, 0));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // 1. Panel Izquierdo con los datos
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBackground(Color.WHITE);

        JLabel lblTitle = new JLabel("Detalle de Habitación");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(26, 26, 46));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoPanel.add(lblTitle);
        infoPanel.add(Box.createVerticalStrut(12));

        // Detalles
        infoPanel.add(crearDetalleLabel("Número: ", h.getNumero()));
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(crearDetalleLabel("Piso: ", String.valueOf(h.getPiso())));
        infoPanel.add(Box.createVerticalStrut(6));
        infoPanel.add(crearDetalleLabel("Tipo: ", h.getTipoLabel()));
        infoPanel.add(Box.createVerticalStrut(6));

        String precioStr = "S/ " + h.getPrecioNoche();
        if (h.getPrecioCama1() != null) {
            precioStr += " / S/ " + h.getPrecioCama1() + " (1 cama)";
        }
        infoPanel.add(crearDetalleLabel("Precio: ", precioStr));
        infoPanel.add(Box.createVerticalStrut(6));

        // Estado con color
        JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        estadoPanel.setBackground(Color.WHITE);
        estadoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblEstKey = new JLabel("Estado: ");
        lblEstKey.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEstKey.setForeground(Color.GRAY);
        JLabel lblEstVal = new JLabel(" " + h.getEstado() + " ");
        lblEstVal.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblEstVal.setOpaque(true);
        lblEstVal.setBackground(h.getColorEstado());
        lblEstVal.setForeground(Color.DARK_GRAY);
        lblEstVal.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1, true));
        estadoPanel.add(lblEstKey);
        estadoPanel.add(lblEstVal);
        infoPanel.add(estadoPanel);

        mainPanel.add(infoPanel, BorderLayout.CENTER);

        // 2. Panel Derecho con la imagen
        JLabel lblImagen = new JLabel();
        lblImagen.setPreferredSize(new Dimension(240, 180));
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);
        lblImagen.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Cargar imagen de la habitación
        try {
            String tipo = h.getTipo();
            java.net.URL resource = getClass().getResource("/com/hospedaje/recursos/habitaciones/" + tipo.toLowerCase() + ".jpg");
            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                Image img = icon.getImage().getScaledInstance(240, 180, Image.SCALE_SMOOTH);
                lblImagen.setIcon(new ImageIcon(img));
            } else {
                // Fallback a simple.jpg si es simple_2p o similar
                java.net.URL fallbackResource = null;
                if (tipo.toLowerCase().startsWith("simple")) {
                    fallbackResource = getClass().getResource("/com/hospedaje/recursos/habitaciones/simple.jpg");
                }
                if (fallbackResource != null) {
                    ImageIcon icon = new ImageIcon(fallbackResource);
                    Image img = icon.getImage().getScaledInstance(240, 180, Image.SCALE_SMOOTH);
                    lblImagen.setIcon(new ImageIcon(img));
                } else {
                    mostrarImagenNoDisponible(lblImagen);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error al cargar imagen de habitación: " + ex.getMessage());
            mostrarImagenNoDisponible(lblImagen);
        }

        mainPanel.add(lblImagen, BorderLayout.EAST);

        // 3. Botón de cierre en la parte inferior
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCerrar.setBackground(new Color(26, 26, 46));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorderPainted(false);
        btnCerrar.setPreferredSize(new Dimension(100, 32));
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dialog.dispose());
        bottomPanel.add(btnCerrar);

        dialog.add(mainPanel, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JPanel crearDetalleLabel(String key, String value) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        p.setBackground(Color.WHITE);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel lblKey = new JLabel(key);
        lblKey.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblKey.setForeground(Color.GRAY);
        JLabel lblVal = new JLabel(value);
        lblVal.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblVal.setForeground(Color.DARK_GRAY);
        p.add(lblKey);
        p.add(lblVal);
        return p;
    }

    private void mostrarImagenNoDisponible(JLabel label) {
        label.setIcon(null);
        label.setText("Imagen no disponible");
        label.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        label.setForeground(Color.GRAY);
        label.setOpaque(true);
        label.setBackground(new Color(240, 240, 240));
        label.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220), 1));
    }

    // ── OTROS PANELES ─────────────────────────────────────────────────────

    private void abrirNuevoRegistro() {
        new RegistroFrame(this, usuarioActual).setVisible(true);
    }

    private void cargarReservasActivas() {
        panelContenido.removeAll();
        panelContenido.add(new PanelReservas(usuarioActual, this));
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void cargarHabitaciones() {
        panelContenido.removeAll();
        panelContenido.add(new PanelHabitaciones(habitacionDAO));
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void cargarCajaDia() {
        panelContenido.removeAll();
        panelContenido.add(new PanelCaja(usuarioActual));
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void cargarReportes() {
        panelContenido.removeAll();
        panelContenido.add(new PanelReportes());
        panelContenido.revalidate();
        panelContenido.repaint();
    }

    /** Refresca el panel principal (útil tras registrar un huésped). */
    public void refrescar() {
        cargarPanelPrincipal();
    }
}
