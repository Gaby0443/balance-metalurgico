package com.balance.modelo;

public class FactorPonderacion {

    private final String malla;

    private final double wi1;
    private final double wi2;
    private final double wi3;
    private final double wi4;
    private final double wi5;
    private final double wi6;
    private final double wi7;

    public FactorPonderacion(
            String malla,
            double wi1,
            double wi2,
            double wi3,
            double wi4,
            double wi5,
            double wi6,
            double wi7) {

        this.malla = malla;
        this.wi1 = validar(wi1, "Wi1");
        this.wi2 = validar(wi2, "Wi2");
        this.wi3 = validar(wi3, "Wi3");
        this.wi4 = validar(wi4, "Wi4");
        this.wi5 = validar(wi5, "Wi5");
        this.wi6 = validar(wi6, "Wi6");
        this.wi7 = validar(wi7, "Wi7");
    }

    private double validar(double valor, String nombre) {
        if (!Double.isFinite(valor) || valor <= 0) {
            throw new IllegalArgumentException(
                    nombre + " debe ser mayor que cero"
            );
        }

        return valor;
    }

    public String getMalla() {
        return malla;
    }

    public double getWi1() {
        return wi1;
    }

    public double getWi2() {
        return wi2;
    }

    public double getWi3() {
        return wi3;
    }

    public double getWi4() {
        return wi4;
    }

    public double getWi5() {
        return wi5;
    }

    public double getWi6() {
        return wi6;
    }

    public double getWi7() {
        return wi7;
    }
}