package com.hospedaje.db;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Conexion {

    private static Connection instancia = null;

    private Conexion() {
    }

    public static synchronized Connection getConexion() {
        try {
            if (instancia == null || instancia.isClosed()) {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream("database.properties")) {
                    props.load(fis);
                } catch (IOException e) {
                    throw new RuntimeException("No se pudo cargar el archivo de configuración 'database.properties'", e);
                }

                String url = props.getProperty("db.url");
                String user = props.getProperty("db.user");
                String pass = props.getProperty("db.password");

                instancia = DriverManager.getConnection(url, user, pass);
                System.out.println(" Conexion establecida con SQL Server.");
            }
        } catch (SQLException e) {
            System.err.println("Error al conectar con SQL Server: " + e.getMessage());
            throw new RuntimeException("No se pudo conectar a la base de datos.", e);
        }
        return instancia;
    }

    public static synchronized void cerrar() {
        try {
            if (instancia != null && !instancia.isClosed()) {
                instancia.close();
                System.out.println("Conexion cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexion: " + e.getMessage());
        }
    }
}
