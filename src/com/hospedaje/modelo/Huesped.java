package com.hospedaje.modelo;

/**
 * Modelo que representa un huésped registrado.
 */
public class Huesped {

    private int    id;
    private String nombres;
    private String apellidos;
    private String dni;
    private String procedencia;
    private String motivoVisita;
    private String telefono;

    public Huesped() {}

    public Huesped(String nombres, String apellidos, String dni,
                   String procedencia, String motivoVisita, String telefono) {
        this.nombres       = nombres;
        this.apellidos     = apellidos;
        this.dni           = dni;
        this.procedencia   = procedencia;
        this.motivoVisita  = motivoVisita;
        this.telefono      = telefono;
    }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public String getNombres()                  { return nombres; }
    public void setNombres(String n)            { this.nombres = n; }

    public String getApellidos()                { return apellidos; }
    public void setApellidos(String a)          { this.apellidos = a; }

    public String getNombreCompleto()           { return nombres + " " + apellidos; }

    public String getDni()                      { return dni; }
    public void setDni(String d)                { this.dni = d; }

    public String getProcedencia()              { return procedencia; }
    public void setProcedencia(String p)        { this.procedencia = p; }

    public String getMotivoVisita()             { return motivoVisita; }
    public void setMotivoVisita(String m)       { this.motivoVisita = m; }

    public String getTelefono()                 { return telefono; }
    public void setTelefono(String t)           { this.telefono = t; }

    @Override
    public String toString() { return getNombreCompleto() + " — DNI: " + dni; }
}
