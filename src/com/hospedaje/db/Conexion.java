package com.hospedaje.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static Connection instancia = null;


    private static final String URL      = "jdbc:sqlserver://localhost:1433;databaseName=HospedajeUnion;encrypt=false;trustServerCertificate=true";
    private static final String USUARIO  = "sa";
    private static final String PASSWORD = "12345";

    private Conexion() {}

    public static synchronized Connection getConexion() {
        try {
            if (instancia == null || instancia.isClosed()) {
                instancia = DriverManager.getConnection(URL, USUARIO, PASSWORD);
                System.out.println(" Conexión establecida con SQL Server.");
            }
        } catch (SQLException e) {
            System.err.println(" Error al conectar: " + e.getMessage());
            throw new RuntimeException("No se pudo conectar a la base de datos.", e);
        }
        return instancia;
    }

    public static synchronized void cerrar() {
        try {
            if (instancia != null && !instancia.isClosed()) {
                instancia.close();
                System.out.println("Conexión cerrada.");
            }
        } catch (SQLException e) {
            System.err.println("Error al cerrar conexión: " + e.getMessage());
        }
    }
}