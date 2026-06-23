package com.hospedaje;

import com.formdev.flatlaf.FlatLightLaf;
import com.hospedaje.vista.LoginFrame;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Aplicar tema moderno FlatLaf
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("defaultFont", new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));
        } catch (Exception e) {
            System.err.println("No se pudo cargar FlatLaf, usando look and feel por defecto.");
        }

        // Lanzar en el hilo de eventos de Swing
        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame();
            login.setVisible(true);
        });
    }
}