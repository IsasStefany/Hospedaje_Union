package com.hospedaje.estructura;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * NODO de la Lista Enlazada Simple para pedidos de lavanderia.
 * Cada nodo representa un pedido de lavanderia de un huesped.
 *
 * Regla de entrega:
 * - Fecha estimada = hora de registro + (kilos x 1 hora)
 * - Si pasa las 20:00 (8pm) -> al dia siguiente a las 08:00
 */
public class NodoLavanderia {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int HORA_LIMITE = 20; // 8pm
    private static final int HORA_APERTURA = 8; // 8am

    // ── Datos del pedido ──────────────────────────────────────────────
    private int    id;
    private int    reservaId;
    private String nombreHuesped;
    private String numeroHabitacion;
    private double kilos;
    private double costo;
    private String tipo;
    private String estado;
    private String observaciones;
    private String fechaRegistro;
    private String fechaEntregaEstimada;

    // ── Puntero al siguiente nodo ─────────────────────────────────────
    public NodoLavanderia siguiente;

    public NodoLavanderia(int reservaId, String nombreHuesped,
                          String numeroHabitacion, double kilos,
                          String tipo, String observaciones) {
        this.reservaId        = reservaId;
        this.nombreHuesped    = nombreHuesped;
        this.numeroHabitacion = numeroHabitacion;
        this.kilos            = kilos;
        this.costo            = kilos * 5.0;
        this.tipo             = tipo;
        this.estado           = "PENDIENTE";
        this.observaciones    = observaciones;

        LocalDateTime ahora = LocalDateTime.now();
        this.fechaRegistro  = ahora.format(FMT);
        this.fechaEntregaEstimada = calcularFechaEntrega(ahora, kilos).format(FMT);
        this.siguiente = null;
    }

    /**
     * Calcula la fecha estimada de entrega.
     * Regla: hora_registro + (kilos x 1 hora)
     * Si el resultado pasa las 20:00 -> dia siguiente a las 08:00
     */
    public static LocalDateTime calcularFechaEntrega(LocalDateTime desde, double kilos) {
        int horas = (int) Math.ceil(kilos); // redondea hacia arriba
        LocalDateTime estimada = desde.plusHours(horas);

        if (estimada.getHour() >= HORA_LIMITE) {
            // Pasa las 8pm -> dia siguiente a las 8am
            estimada = estimada.toLocalDate()
                    .plusDays(1)
                    .atTime(HORA_APERTURA, 0);
        }
        return estimada;
    }

    // ── Getters y Setters ─────────────────────────────────────────────
    public int    getId()                          { return id; }
    public void   setId(int id)                    { this.id = id; }
    public int    getReservaId()                   { return reservaId; }
    public String getNombreHuesped()               { return nombreHuesped; }
    public String getNumeroHabitacion()            { return numeroHabitacion; }
    public double getKilos()                       { return kilos; }
    public double getCosto()                       { return costo; }
    public String getTipo()                        { return tipo; }
    public String getEstado()                      { return estado; }
    public void   setEstado(String e)              { this.estado = e; }
    public String getObservaciones()               { return observaciones; }
    public String getFechaRegistro()               { return fechaRegistro; }
    public void   setFechaRegistro(String f)       { this.fechaRegistro = f; }
    public String getFechaEntregaEstimada()        { return fechaEntregaEstimada; }
    public void   setFechaEntregaEstimada(String f){ this.fechaEntregaEstimada = f; }

    public String getTipoLabel() {
        return switch (tipo) {
            case "ROPA_HUESPED"    -> "Ropa del huesped";
            case "SABANAS_TOALLAS" -> "Sabanas y toallas";
            case "MIXTO"           -> "Ropa + sabanas/toallas";
            default                -> tipo;
        };
    }

    @Override
    public String toString() {
        return "Hab." + numeroHabitacion + " - " + nombreHuesped
                + " | " + kilos + "kg | S/" + String.format("%.2f", costo)
                + " | Entrega estimada: " + fechaEntregaEstimada;
    }
}