package com.hospedaje.modelo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo que representa la entidad Boleta.
 */
public class Boleta {
    private int id;
    private String numeroBoleta;
    private int reservaId;
    private BigDecimal montoTotal;
    private int usuarioId;
    private LocalDateTime fechaEmision;

    public Boleta() {}

    public Boleta(String numeroBoleta, int reservaId, BigDecimal montoTotal, int usuarioId) {
        this.numeroBoleta = numeroBoleta;
        this.reservaId = reservaId;
        this.montoTotal = montoTotal;
        this.usuarioId = usuarioId;
        this.fechaEmision = LocalDateTime.now();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNumeroBoleta() {
        return numeroBoleta;
    }

    public void setNumeroBoleta(String numeroBoleta) {
        this.numeroBoleta = numeroBoleta;
    }

    public int getReservaId() {
        return reservaId;
    }

    public void setReservaId(int reservaId) {
        this.reservaId = reservaId;
    }

    public BigDecimal getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(BigDecimal montoTotal) {
        this.montoTotal = montoTotal;
    }

    public int getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(int usuarioId) {
        this.usuarioId = usuarioId;
    }

    public LocalDateTime getFechaEmision() {
        return fechaEmision;
    }

    public void setFechaEmision(LocalDateTime fechaEmision) {
        this.fechaEmision = fechaEmision;
    }
}
