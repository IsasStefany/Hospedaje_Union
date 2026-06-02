package com.hospedaje.vista;

import com.hospedaje.dao.UsuarioDAO;
import com.hospedaje.modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Pantalla de inicio de sesión del sistema.
 */
public class LoginFrame extends JFrame {

    private JTextField     txtUsuario;
    private JPasswordField txtContrasena;
    private JButton        btnIngresar;
    private JLabel         lblError;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    public LoginFrame() {
        setTitle("Hospedaje Union — Iniciar sesión");
        setSize(400, 480);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        construirUI();
    }

    private void construirUI() {
        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(new Color(26, 26, 46));
        setContentPane(fondo);

        JPanel caja = new JPanel();
        caja.setLayout(new BoxLayout(caja, BoxLayout.Y_AXIS));
        caja.setBackground(Color.WHITE);
        caja.setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));
        caja.setPreferredSize(new Dimension(300, 400));

        // Logo
        JLabel lblLogo = new JLabel("Logo Placeholder");
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblLogo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        caja.add(lblLogo);
        caja.add(Box.createVerticalStrut(10));

        // Cargar logo dinámicamente
        try {
            java.net.URL resource = getClass().getResource("/com/hospedaje/recursos/logo.png");
            if (resource != null) {
                ImageIcon originalIcon = new ImageIcon(resource);
                Image originalImg = originalIcon.getImage();
                Image scaledImg = originalImg.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                lblLogo.setIcon(new ImageIcon(scaledImg));
                lblLogo.setText(""); // Limpiar texto placeholder
            } else {
                lblLogo.setText("[Logo no encontrado]");
            }
        } catch (Exception e) {
            System.err.println("Error cargando logo.png: " + e.getMessage());
            lblLogo.setText("[Error de carga]");
        }
        lblLogo.setMaximumSize(new Dimension(Integer.MAX_VALUE, lblLogo.getPreferredSize().height));

        // Título
        JLabel titulo = new JLabel("Hospedaje Union");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titulo.setForeground(new Color(26, 26, 46));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        titulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        titulo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        titulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, titulo.getPreferredSize().height));
        caja.add(titulo);
        caja.add(Box.createVerticalStrut(4));

        JLabel subtitulo = new JLabel("Sistema de gestión");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitulo.setForeground(Color.GRAY);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitulo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        subtitulo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        subtitulo.setMaximumSize(new Dimension(Integer.MAX_VALUE, subtitulo.getPreferredSize().height));
        caja.add(subtitulo);
        caja.add(Box.createVerticalStrut(20));

        // Campo usuario
        caja.add(crearLabel("Usuario"));
        caja.add(Box.createVerticalStrut(4));
        txtUsuario = new JTextField();
        txtUsuario.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtUsuario.setAlignmentX(Component.CENTER_ALIGNMENT);
        caja.add(txtUsuario);
        caja.add(Box.createVerticalStrut(10));

        // Campo contraseña
        caja.add(crearLabel("Contraseña"));
        caja.add(Box.createVerticalStrut(4));
        txtContrasena = new JPasswordField();
        txtContrasena.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtContrasena.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtContrasena.setAlignmentX(Component.CENTER_ALIGNMENT);
        caja.add(txtContrasena);
        caja.add(Box.createVerticalStrut(16));

        // Botón ingresar
        btnIngresar = new JButton("Ingresar al sistema");
        btnIngresar.setBackground(new Color(26, 26, 46));
        btnIngresar.setForeground(Color.WHITE);
        btnIngresar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnIngresar.setFocusPainted(false);
        btnIngresar.setBorderPainted(false);
        btnIngresar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btnIngresar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnIngresar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnIngresar.addActionListener(e -> intentarLogin());
        caja.add(btnIngresar);
        caja.add(Box.createVerticalStrut(8));

        // Label de error
        lblError = new JLabel(" ");
        lblError.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblError.setForeground(new Color(198, 40, 40));
        lblError.setAlignmentX(Component.CENTER_ALIGNMENT);
        caja.add(lblError);

        fondo.add(caja);

        // Enter en contraseña también hace login
        txtContrasena.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) intentarLogin();
            }
        });
    }

    private JLabel crearLabel(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(Color.GRAY);
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        return lbl;
    }

    private void intentarLogin() {
        String user = txtUsuario.getText().trim();
        String pass = new String(txtContrasena.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            lblError.setText("Completa usuario y contraseña.");
            return;
        }

        btnIngresar.setEnabled(false);
        btnIngresar.setText("Verificando...");

        SwingWorker<Usuario, Void> worker = new SwingWorker<>() {
            @Override
            protected Usuario doInBackground() {
                return usuarioDAO.login(user, pass);
            }

            @Override
            protected void done() {
                try {
                    Usuario u = get();
                    if (u != null) {
                        dispose();
                        new MainFrame(u).setVisible(true);
                    } else {
                        lblError.setText("Usuario o contraseña incorrectos.");
                        txtContrasena.setText("");
                        btnIngresar.setEnabled(true);
                        btnIngresar.setText("Ingresar al sistema");
                    }
                } catch (Exception ex) {
                    lblError.setText("Error de conexión con la base de datos.");
                    btnIngresar.setEnabled(true);
                    btnIngresar.setText("Ingresar al sistema");
                }
            }
        };
        worker.execute();
    }
}
