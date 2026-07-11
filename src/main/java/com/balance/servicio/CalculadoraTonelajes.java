package com.balance.servicio;

import com.balance.modelo.RegistroGranulometrico;
import com.balance.modelo.ResultadoTonelajes;

import java.util.List;

public class CalculadoraTonelajes {

    private static final double EPSILON = 1.0e-12;

    public ResultadoTonelajes calcular(
            List<RegistroGranulometrico> datos,
            double ms3) {

        if (datos == null || datos.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe ingresar al menos una fila granulométrica"
            );
        }

        if (!Double.isFinite(ms3) || ms3 <= 0) {
            throw new IllegalArgumentException(
                    "MS3 debe ser mayor que cero"
            );
        }

        double sumaX5Cuadrado = 0;
        double sumaY5Cuadrado = 0;
        double sumaY7Cuadrado = 0;
        double sumaY5Y7 = 0;

        double sumaX3X5 = 0;
        double sumaY3Y5 = 0;
        double sumaY3Y7 = 0;

        for (RegistroGranulometrico fila : datos) {

            double x3 = fila.getF3() - fila.getF4();
            double x5 = fila.getF4() - fila.getF5();

            double y3 = fila.getF4() - fila.getF6();
            double y5 = fila.getF6() - fila.getF4();
            double y7 = fila.getF6() - fila.getF7();

            sumaX5Cuadrado += x5 * x5;
            sumaY5Cuadrado += y5 * y5;
            sumaY7Cuadrado += y7 * y7;

            sumaY5Y7 += y5 * y7;

            sumaX3X5 += x3 * x5;
            sumaY3Y5 += y3 * y5;
            sumaY3Y7 += y3 * y7;
        }

        double c11 = sumaX5Cuadrado + sumaY5Cuadrado;
        double c12 = sumaY5Y7;
        double c21 = sumaY5Y7;
        double c22 = sumaY7Cuadrado;

        double b1 = -ms3 * (sumaX3X5 + sumaY3Y5);
        double b2 = -ms3 * sumaY3Y7;

        double determinante = c11 * c22 - c12 * c21;

        if (Math.abs(determinante) < EPSILON) {
            throw new IllegalStateException(
                    "La matriz de tonelajes es singular o está mal condicionada"
            );
        }

        // Solución directa para una matriz 2 x 2.
        double ms5 = (b1 * c22 - c12 * b2) / determinante;
        double ms7 = (c11 * b2 - b1 * c21) / determinante;

        double ms4 = ms3 - ms5;
        double ms6 = ms4 - ms7;
        double ms8 = ms5 + ms7;

        validarTonelajes(ms3, ms4, ms5, ms6, ms7, ms8);

        return new ResultadoTonelajes(
                ms3,
                ms4,
                ms5,
                ms6,
                ms7,
                ms8
        );
    }

    private void validarTonelajes(double... valores) {
        for (double valor : valores) {
            if (!Double.isFinite(valor)) {
                throw new IllegalStateException(
                        "Se obtuvo un tonelaje no válido"
                );
            }

            if (valor < -1.0e-8) {
                throw new IllegalStateException(
                        "El cálculo produjo un tonelaje negativo: " + valor
                );
            }
        }
    }
}