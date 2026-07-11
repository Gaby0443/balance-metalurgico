package com.balance.modelo;


public class Flujo {

    private final String codigo;
    private final String descripcion;
    private double tonelaje;

    public Flujo(String codigo, String descripcion, double tonelaje) {
        this.codigo = codigo;
        this.descripcion = descripcion;
        this.tonelaje = tonelaje;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getTonelaje() {
        return tonelaje;
    }

    public void setTonelaje(double tonelaje) {
        this.tonelaje = tonelaje;
    }
}