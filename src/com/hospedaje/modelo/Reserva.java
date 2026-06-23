package com.hospedaje.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reserva {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int           id;
    private int           huespedId;
    private int           habitacionId;
    private int           usuarioId;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaVencimiento;
    private LocalDateTime fechaSalidaReal;
    private BigDecimal    precioCobrado;
    private int           usoCamas;
    private String        estado;
    private String        metodoPago;   // EFECTIVO | YAPE | VISA | BCP
    private String        observaciones;

    // Para mostrar en pantalla (JOIN)
    private String nombreHuesped;
    private String dniHuesped;
    private String numeroHabitacion;
    private String tipoHabitacion;

    public Reserva() {}

    public static LocalDateTime calcularVencimiento(LocalDateTime entrada) {
        return entrada.toLocalDate().plusDays(1).atTime(12, 0);
    }

    public String getFechaEntradaStr()     { return fechaEntrada     != null ? fechaEntrada.format(FMT)     : ""; }
    public String getFechaVencimientoStr() { return fechaVencimiento != null ? fechaVencimiento.format(FMT) : ""; }

    public int getId()                              { return id; }
    public void setId(int id)                       { this.id = id; }
    public int getHuespedId()                       { return huespedId; }
    public void setHuespedId(int h)                 { this.huespedId = h; }
    public int getHabitacionId()                    { return habitacionId; }
    public void setHabitacionId(int h)              { this.habitacionId = h; }
    public int getUsuarioId()                       { return usuarioId; }
    public void setUsuarioId(int u)                 { this.usuarioId = u; }
    public LocalDateTime getFechaEntrada()          { return fechaEntrada; }
    public void setFechaEntrada(LocalDateTime f)    { this.fechaEntrada = f; }
    public LocalDateTime getFechaVencimiento()      { return fechaVencimiento; }
    public void setFechaVencimiento(LocalDateTime f){ this.fechaVencimiento = f; }
    public LocalDateTime getFechaSalidaReal()       { return fechaSalidaReal; }
    public void setFechaSalidaReal(LocalDateTime f) { this.fechaSalidaReal = f; }
    public BigDecimal getPrecioCobrado()            { return precioCobrado; }
    public void setPrecioCobrado(BigDecimal p)      { this.precioCobrado = p; }
    public int getUsoCamas()                        { return usoCamas; }
    public void setUsoCamas(int u)                  { this.usoCamas = u; }
    public String getEstado()                       { return estado; }
    public void setEstado(String e)                 { this.estado = e; }
    public String getMetodoPago()                   { return metodoPago; }
    public void setMetodoPago(String m)             { this.metodoPago = m; }
    public String getObservaciones()                { return observaciones; }
    public void setObservaciones(String o)          { this.observaciones = o; }
    public String getNombreHuesped()                { return nombreHuesped; }
    public void setNombreHuesped(String n)          { this.nombreHuesped = n; }
    public String getDniHuesped()                   { return dniHuesped; }
    public void setDniHuesped(String d)             { this.dniHuesped = d; }
    public String getNumeroHabitacion()             { return numeroHabitacion; }
    public void setNumeroHabitacion(String n)       { this.numeroHabitacion = n; }
    public String getTipoHabitacion()               { return tipoHabitacion; }
    public void setTipoHabitacion(String t)         { this.tipoHabitacion = t; }
}