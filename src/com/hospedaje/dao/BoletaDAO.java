package com.hospedaje.dao;

import com.hospedaje.db.Conexion;
import com.hospedaje.modelo.Boleta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BoletaDAO {

    /**
     * DAO para gestionar las boletas de venta.
     */

        /**
         * Obtiene el siguiente número de boleta de la base de datos.
         * Retorna una cadena con ceros a la izquierda (ej. "00000001").
         */
        public String obtenerSiguienteNumeroBoleta() {
            String sql = "SELECT ISNULL(MAX(CAST(numero_boleta AS INT)), 0) + 1 FROM boletas";
            try (Connection con = Conexion.getConexion();
                 PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int siguiente = rs.getInt(1);
                    return String.format("%08d", siguiente);
                }
            } catch (SQLException e) {
                System.err.println("Error al obtener siguiente número de boleta: " + e.getMessage());
            }
            // Retorna "00000001" en caso de tabla vacía o error de conexión/tabla inexistente
            return "00000001";
        }

        /**
         * Registra una boleta de venta en la base de datos.
         * Lanza SQLException si ocurre un error para que la interfaz pueda capturarlo y mostrarlo.
         */
        public void registrarBoleta(Boleta boleta) throws SQLException {
            String sql = "INSERT INTO boletas (numero_boleta, reserva_id, monto_total, usuario_id, fecha_emision) VALUES (?, ?, ?, ?, ?)";
            try (Connection con = Conexion.getConexion()) {
                boolean originalAutoCommit = con.getAutoCommit();
                con.setAutoCommit(true);
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setString(1, boleta.getNumeroBoleta());
                    ps.setInt(2, boleta.getReservaId());
                    ps.setBigDecimal(3, boleta.getMontoTotal());
                    ps.setInt(4, boleta.getUsuarioId());
                    ps.setTimestamp(5, java.sql.Timestamp.valueOf(boleta.getFechaEmision()));
                    ps.executeUpdate();
                } finally {
                    con.setAutoCommit(originalAutoCommit);
                }
            }
        }
    }

