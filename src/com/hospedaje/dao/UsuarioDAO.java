package com.hospedaje.dao;

import com.hospedaje.db.Conexion;
import com.hospedaje.modelo.Usuario;

import java.sql.*;

/**
 * DAO para operaciones de usuarios en la base de datos.
 */
public class UsuarioDAO {

    /**
     * Verifica las credenciales del usuario.
     * Retorna el Usuario si es válido, null si no existe o está inactivo.
     */
    public Usuario login(String usuario, String contrasena) {
        String sql = "SELECT id, nombre, usuario, rol, activo "
                + "FROM usuarios "
                + "WHERE usuario = ? AND contrasena = ? AND activo = 1";

        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario);
            ps.setString(2, contrasena);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setId(rs.getInt("id"));
                    u.setNombre(rs.getString("nombre"));
                    u.setUsuario(rs.getString("usuario"));
                    u.setRol(rs.getString("rol"));
                    u.setActivo(rs.getBoolean("activo"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en login: " + e.getMessage());
        }
        return null;
    }
}