package com.balance.util;

public final class MatrizUtils {

    private static final double EPSILON = 1.0e-15;

    private MatrizUtils() {
    }

    public static double[] resolverSistema2x2(
            double a11,
            double a12,
            double a21,
            double a22,
            double b1,
            double b2) {

        validarNumero(a11, "a11");
        validarNumero(a12, "a12");
        validarNumero(a21, "a21");
        validarNumero(a22, "a22");
        validarNumero(b1, "b1");
        validarNumero(b2, "b2");

        double determinante = a11 * a22 - a12 * a21;

        double escala = Math.max(
                Math.max(Math.abs(a11), Math.abs(a12)),
                Math.max(Math.abs(a21), Math.abs(a22))
        );

        double tolerancia = Math.max(EPSILON, escala * escala * 1.0e-14);

        if (Math.abs(determinante) <= tolerancia) {
            throw new IllegalStateException(
                    "La matriz 2x2 es singular o está mal condicionada. "
                            + "Determinante: " + determinante
            );
        }

        double x1 = (b1 * a22 - a12 * b2) / determinante;
        double x2 = (a11 * b2 - b1 * a21) / determinante;

        if (!Double.isFinite(x1) || !Double.isFinite(x2)) {
            throw new IllegalStateException(
                    "El sistema produjo resultados no válidos"
            );
        }

        return new double[]{x1, x2};
    }

    private static void validarNumero(double valor, String nombre) {
        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException(
                    nombre + " no es un número válido"
            );
        }
    }
}