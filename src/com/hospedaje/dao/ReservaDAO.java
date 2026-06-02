package com.hospedaje.dao;

import com.hospedaje.db.Conexion;
import com.hospedaje.modelo.Huesped;
import com.hospedaje.modelo.Reserva;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para registrar huéspedes y sus reservas.
 */
public class ReservaDAO {

    /**
     * Registra un huésped nuevo y su reserva en una sola transacción.
     * Retorna el ID de la reserva creada, o -1 si hubo error.
     */
    public int registrarHuespedYReserva(Huesped huesped, Reserva reserva) {
        Connection con = null;
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false);

            // 1. Insertar huésped
            int huespedId = insertarHuesped(con, huesped);
            if (huespedId == -1) throw new SQLException("No se pudo insertar el huésped.");

            // 2. Insertar reserva
            reserva.setHuespedId(huespedId);
            int reservaId = insertarReserva(con, reserva);
            if (reservaId == -1) throw new SQLException("No se pudo insertar la reserva.");

            // 3. Marcar habitación como OCUPADO
            actualizarEstadoHabitacion(con, reserva.getHabitacionId(), "OCUPADO");

            // 4. Actualizar caja del día
            actualizarCajaDiaria(con, reserva.getPrecioCobrado());

            con.commit();
            return reservaId;

        } catch (SQLException e) {
            System.err.println("Error en transacción: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return -1;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /** Retorna las reservas activas del día de hoy. */
    public List<Reserva> listarActivas() {
        List<Reserva> lista = new ArrayList<>();
        String sql = "SELECT r.id, r.habitacion_id, r.precio_cobrado, r.uso_camas, "
                + "r.fecha_entrada, r.fecha_vencimiento, r.estado, "
                + "hu.nombres + ' ' + hu.apellidos AS nombre_huesped, "
                + "hu.dni, h.numero AS numero_habitacion, h.tipo "
                + "FROM reservas r "
                + "JOIN huespedes hu ON hu.id = r.huesped_id "
                + "JOIN habitaciones h ON h.id = r.habitacion_id "
                + "WHERE r.estado = 'ACTIVA' "
                + "ORDER BY r.fecha_vencimiento";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Reserva re = new Reserva();
                re.setId(rs.getInt("id"));
                re.setHabitacionId(rs.getInt("habitacion_id"));
                re.setPrecioCobrado(rs.getBigDecimal("precio_cobrado"));
                re.setUsoCamas(rs.getInt("uso_camas"));
                re.setFechaEntrada(rs.getTimestamp("fecha_entrada").toLocalDateTime());
                re.setFechaVencimiento(rs.getTimestamp("fecha_vencimiento").toLocalDateTime());
                re.setEstado(rs.getString("estado"));
                re.setNombreHuesped(rs.getString("nombre_huesped"));
                re.setDniHuesped(rs.getString("dni"));
                re.setNumeroHabitacion(rs.getString("numero_habitacion"));
                re.setTipoHabitacion(rs.getString("tipo"));
                lista.add(re);
            }
        } catch (SQLException e) {
            System.err.println("Error listando reservas: " + e.getMessage());
        }
        return lista;
    }

    /** Finaliza una reserva y libera la habitación. */
    public boolean finalizarReserva(int reservaId, int habitacionId) {
        Connection con = null;
        try {
            con = Conexion.getConexion();
            con.setAutoCommit(false);

            String sql1 = "UPDATE reservas SET estado = 'FINALIZADA', "
                    + "fecha_salida_real = GETDATE() WHERE id = ?";
            try (PreparedStatement ps = con.prepareStatement(sql1)) {
                ps.setInt(1, reservaId);
                ps.executeUpdate();
            }
            actualizarEstadoHabitacion(con, habitacionId, "DISPONIBLE");
            con.commit();
            return true;

        } catch (SQLException e) {
            System.err.println("Error finalizando reserva: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    /** Total de ingresos del día actual. */
    public double totalIngresosHoy() {
        String sql = "SELECT ISNULL(SUM(precio_cobrado), 0) "
                + "FROM reservas "
                + "WHERE CAST(fecha_creacion AS DATE) = CAST(GETDATE() AS DATE)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            System.err.println("Error total ingresos: " + e.getMessage());
        }
        return 0;
    }

    // ── Métodos privados de apoyo ──────────────────────────────────────────

    private int insertarHuesped(Connection con, Huesped h) throws SQLException {
        String sql = "INSERT INTO huespedes (nombres, apellidos, dni, procedencia, motivo_visita, telefono) "
                + "VALUES (?, ?, ?, ?, ?, ?); SELECT SCOPE_IDENTITY();";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, h.getNombres());
            ps.setString(2, h.getApellidos());
            ps.setString(3, h.getDni());
            ps.setString(4, h.getProcedencia());
            ps.setString(5, h.getMotivoVisita());
            ps.setString(6, h.getTelefono());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private int insertarReserva(Connection con, Reserva r) throws SQLException {
        String sql = "INSERT INTO reservas (huesped_id, habitacion_id, usuario_id, "
                + "fecha_entrada, fecha_vencimiento, precio_cobrado, uso_camas, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVA'); SELECT SCOPE_IDENTITY();";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, r.getHuespedId());
            ps.setInt(2, r.getHabitacionId());
            ps.setInt(3, r.getUsuarioId());
            ps.setTimestamp(4, Timestamp.valueOf(r.getFechaEntrada()));
            ps.setTimestamp(5, Timestamp.valueOf(r.getFechaVencimiento()));
            ps.setBigDecimal(6, r.getPrecioCobrado());
            ps.setInt(7, r.getUsoCamas());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    private void actualizarEstadoHabitacion(Connection con, int habId, String estado)
            throws SQLException {
        String sql = "UPDATE habitaciones SET estado = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            ps.setInt(2, habId);
            ps.executeUpdate();
        }
    }

    private void actualizarCajaDiaria(Connection con, java.math.BigDecimal monto)
            throws SQLException {
        String sql = "IF EXISTS (SELECT 1 FROM caja_diaria WHERE fecha = CAST(GETDATE() AS DATE)) "
                + "  UPDATE caja_diaria SET total_ingresos = total_ingresos + ?, "
                + "    saldo_neto = saldo_neto + ? WHERE fecha = CAST(GETDATE() AS DATE) "
                + "ELSE "
                + "  INSERT INTO caja_diaria (fecha, total_ingresos, saldo_neto) "
                + "  VALUES (CAST(GETDATE() AS DATE), ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setBigDecimal(1, monto);
            ps.setBigDecimal(2, monto);
            ps.setBigDecimal(3, monto);
            ps.setBigDecimal(4, monto);
            ps.executeUpdate();
        }
    }
}
