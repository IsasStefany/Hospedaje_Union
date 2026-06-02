package com.hospedaje.dao;

import com.hospedaje.db.Conexion;
import com.hospedaje.modelo.Habitacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HabitacionDAO {

    public List<Habitacion> listarTodas() {
        List<Habitacion> lista = new ArrayList<>();
        String sql = "SELECT id, numero, piso, tipo, descripcion, precio_noche, "
                + "precio_cama1, estado, tiene_banio_propio "
                + "FROM habitaciones ORDER BY piso, numero";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    public List<Habitacion> listarDisponibles() {
        List<Habitacion> lista = new ArrayList<>();
        String sql = "SELECT id, numero, piso, tipo, descripcion, precio_noche, "
                + "precio_cama1, estado, tiene_banio_propio "
                + "FROM habitaciones WHERE estado = 'DISPONIBLE' ORDER BY piso, numero";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) { System.err.println("Error: " + e.getMessage()); }
        return lista;
    }

    public boolean insertar(Habitacion h) {
        String sql = "INSERT INTO habitaciones (numero, piso, tipo, descripcion, "
                + "precio_noche, precio_cama1, estado, tiene_banio_propio) "
                + "VALUES (?, ?, ?, ?, ?, ?, 'DISPONIBLE', ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, h.getNumero());
            ps.setInt(2, h.getPiso());
            ps.setString(3, h.getTipo());
            ps.setString(4, h.getDescripcion());
            ps.setBigDecimal(5, h.getPrecioNoche());
            ps.setBigDecimal(6, h.getPrecioCama1());
            ps.setBoolean(7, h.isTieneBanioPropio());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error insertando: " + e.getMessage()); return false; }
    }

    public boolean actualizar(Habitacion h) {
        String sql = "UPDATE habitaciones SET numero=?, piso=?, tipo=?, descripcion=?, "
                + "precio_noche=?, precio_cama1=?, tiene_banio_propio=? "
                + "WHERE id=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, h.getNumero());
            ps.setInt(2, h.getPiso());
            ps.setString(3, h.getTipo());
            ps.setString(4, h.getDescripcion());
            ps.setBigDecimal(5, h.getPrecioNoche());
            ps.setBigDecimal(6, h.getPrecioCama1());
            ps.setBoolean(7, h.isTieneBanioPropio());
            ps.setInt(8, h.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error actualizando: " + e.getMessage()); return false; }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM habitaciones WHERE id=? AND estado='DISPONIBLE'";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error eliminando: " + e.getMessage()); return false; }
    }

    public boolean actualizarEstado(int habitacionId, String nuevoEstado) {
        String sql = "UPDATE habitaciones SET estado=? WHERE id=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, habitacionId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("Error estado: " + e.getMessage()); return false; }
    }

    public int contarPorEstado(String estado) {
        String sql = "SELECT COUNT(*) FROM habitaciones WHERE estado=?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
        } catch (SQLException e) { System.err.println("Error contando: " + e.getMessage()); }
        return 0;
    }

    public double precioPromedio() {
        String sql = "SELECT AVG(precio_noche) FROM habitaciones";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 30.0;
    }

    public int totalHabitaciones() {
        String sql = "SELECT COUNT(*) FROM habitaciones";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { System.err.println(e.getMessage()); }
        return 22;
    }

    private Habitacion mapear(ResultSet rs) throws SQLException {
        Habitacion h = new Habitacion();
        h.setId(rs.getInt("id"));
        h.setNumero(rs.getString("numero"));
        h.setPiso(rs.getInt("piso"));
        h.setTipo(rs.getString("tipo"));
        h.setDescripcion(rs.getString("descripcion"));
        h.setPrecioNoche(rs.getBigDecimal("precio_noche"));
        h.setPrecioCama1(rs.getBigDecimal("precio_cama1"));
        // h.setPrecioRebaja(rs.getBigDecimal("precio_rebaja")); // Removido por compatibilidad
        // h.setDiasMinRebaja(rs.getInt("dias_minimo_rebaja")); // Removido por compatibilidad
        h.setEstado(rs.getString("estado"));
        h.setTieneBanioPropio(rs.getBoolean("tiene_banio_propio"));
        // h.setEsCuartoEspecial(rs.getBoolean("es_cuarto_especial")); // Removido por compatibilidad
        return h;
    }
}
