package com.hospedaje.modelo;

/**
 * Modelo que representa un usuario del sistema.
 */
public class Usuario {

    private int    id;
    private String nombre;
    private String usuario;
    private String contrasena;
    private String rol;       // ADMIN | OPERADOR
    private boolean activo;

    public Usuario() {}

    public Usuario(int id, String nombre, String usuario, String rol, boolean activo) {
        this.id        = id;
        this.nombre    = nombre;
        this.usuario   = usuario;
        this.rol       = rol;
        this.activo    = activo;
    }

    // Getters y Setters
    public int getId()                  { return id; }
    public void setId(int id)           { this.id = id; }

    public String getNombre()           { return nombre; }
    public void setNombre(String n)     { this.nombre = n; }

    public String getUsuario()          { return usuario; }
    public void setUsuario(String u)    { this.usuario = u; }

    public String getContrasena()       { return contrasena; }
    public void setContrasena(String c) { this.contrasena = c; }

    public String getRol()              { return rol; }
    public void setRol(String r)        { this.rol = r; }

    public boolean isActivo()           { return activo; }
    public void setActivo(boolean a)    { this.activo = a; }

    public boolean esAdmin()            { return "ADMIN".equals(rol); }

    @Override
    public String toString() {
        return nombre + " (" + rol + ")";
    }
}
