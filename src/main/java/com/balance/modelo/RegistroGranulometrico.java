package com.balance.modelo;

public class RegistroGranulometrico {

    private final String malla;
    private final double abertura;

    private final double f1; //Alimentacion fresca
    private final double f2; // Alimentaci'on al molino
    private final double f3; // Des Molino
    private final double f4; // Rebalse Clasi
    private final double f5; // Desc Clasif
    private final double f6; // Rebal Clasif 2
    private final double f7; // Desclasif 2

    public RegistroGranulometrico(
            String malla,
            double abertura,
            double f1,
            double f2,
            double f3,
            double f4,
            double f5,
            double f6,
            double f7) {

        this.malla = malla;
        this.abertura = abertura;
        this.f1 = validarPorcentaje(f1, "F1");
        this.f2 = validarPorcentaje(f2, "F2");
        this.f3 = validarPorcentaje(f3, "F3");
        this.f4 = validarPorcentaje(f4, "F4");
        this.f5 = validarPorcentaje(f5, "F5");
        this.f6 = validarPorcentaje(f6, "F6");
        this.f7 = validarPorcentaje(f7, "F7");
    }

    private static double validarPorcentaje(double valor, String nombre) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException(
                    nombre + " debe ser un número válido"
            );
        }

        if (valor < 0 || valor > 100) {
            throw new IllegalArgumentException(
                    nombre + " debe estar entre 0 y 100"
            );
        }

        return valor;
    }

    public String getMalla() {
        return malla;
    }

    public double getAbertura() {
        return abertura;
    }

    public double getF1() {
        return f1;
    }

    public double getF2() {
        return f2;
    }

    public double getF3() {
        return f3;
    }

    public double getF4() {
        return f4;
    }

    public double getF5() {
        return f5;
    }

    public double getF6() {
        return f6;
    }

    public double getF7() {
        return f7;
    }
}
