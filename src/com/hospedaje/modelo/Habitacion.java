package com.hospedaje.modelo;

import java.math.BigDecimal;

public class Habitacion {

    private int        id;
    private String     numero;
    private int        piso;
    private String     tipo;
    private String     descripcion;
    private BigDecimal precioNoche;
    private BigDecimal precioCama1;
    private BigDecimal precioRebaja;
    private int        diasMinRebaja;
    private String     estado;
    private boolean    tieneBanioPropio;
    private boolean    esCuartoEspecial;

    public Habitacion() {}

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }
    public String getNumero()                       { return numero; }
    public void setNumero(String n)                 { this.numero = n; }
    public int getPiso()                            { return piso; }
    public void setPiso(int p)                      { this.piso = p; }
    public String getTipo()                         { return tipo; }
    public void setTipo(String t)                   { this.tipo = t; }
    public String getDescripcion()                  { return descripcion; }
    public void setDescripcion(String d)            { this.descripcion = d; }
    public BigDecimal getPrecioNoche()              { return precioNoche; }
    public void setPrecioNoche(BigDecimal p)        { this.precioNoche = p; }
    public BigDecimal getPrecioCama1()              { return precioCama1; }
    public void setPrecioCama1(BigDecimal p)        { this.precioCama1 = p; }
    public BigDecimal getPrecioRebaja()             { return precioRebaja; }
    public void setPrecioRebaja(BigDecimal p)       { this.precioRebaja = p; }
    public int getDiasMinRebaja()                   { return diasMinRebaja; }
    public void setDiasMinRebaja(int d)             { this.diasMinRebaja = d; }
    public String getEstado()                       { return estado; }
    public void setEstado(String e)                 { this.estado = e; }
    public boolean isTieneBanioPropio()             { return tieneBanioPropio; }
    public void setTieneBanioPropio(boolean b)      { this.tieneBanioPropio = b; }
    public boolean isEsCuartoEspecial()             { return esCuartoEspecial; }
    public void setEsCuartoEspecial(boolean e)      { this.esCuartoEspecial = e; }
    public boolean isDisponible()                   { return "DISPONIBLE".equals(estado); }
    public boolean isOcupado()                      { return "OCUPADO".equals(estado); }

    public String getTipoLabel() {
        return switch (tipo) {
            case "SIMPLE"      -> "Simple (baño compartido)";
            case "SIMPLE_2P"   -> "Simple 2 plazas (baño compartido)";
            case "MATRIMONIAL" -> "Matrimonial (baño propio)";
            case "DOBLE"       -> "Doble (baño propio)";
            case "INDIVIDUAL"  -> "Individual (baño propio)";
            default            -> tipo;
        };
    }

    public java.awt.Color getColorEstado() {
        return switch (estado) {
            case "DISPONIBLE"    -> new java.awt.Color(232, 245, 233);
            case "OCUPADO"       -> new java.awt.Color(255, 235, 238);
            case "MANTENIMIENTO" -> new java.awt.Color(255, 248, 225);
            default              -> java.awt.Color.WHITE;
        };
    }

    @Override
    public String toString() {
        return "Hab. " + numero + " — " + getTipoLabel() + " — S/ " + precioNoche
                + (esCuartoEspecial ? " (especial)" : "");
    }
}
