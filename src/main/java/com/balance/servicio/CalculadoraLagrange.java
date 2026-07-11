package com.balance.servicio;

import com.balance.modelo.FactorPonderacion;
import com.balance.modelo.RegistroGranulometrico;
import com.balance.modelo.ResultadoLagrange;
import com.balance.modelo.ResultadoTonelajes;

public class CalculadoraLagrange {

    private static final double TOLERANCIA_MATRIZ = 1.0e-14;
    private static final double TOLERANCIA_RESIDUO = 1.0e-10;

    public ResultadoLagrange calcular(
            RegistroGranulometrico registro,
            ResultadoTonelajes tonelajes,
            FactorPonderacion pesos) {

        validarEntradas(registro, tonelajes, pesos);

        double ms3 = tonelajes.getMs3();
        double ms4 = tonelajes.getMs4();
        double ms5 = tonelajes.getMs5();
        double ms6 = tonelajes.getMs6();
        double ms7 = tonelajes.getMs7();

        /*
         * Desajustes de los datos granulométricos medidos.
         */
        double delta1 =
                ms3 * registro.getF3()
                        - ms4 * registro.getF4()
                        - ms5 * registro.getF5();

        double delta2 =
                ms4 * registro.getF4()
                        - ms6 * registro.getF6()
                        - ms7 * registro.getF7();

        /*
         * Es más estable calcular directamente 1/W.
         *
         * Para porcentajes iguales a 0 % o 100 %,
         * el peso tiende a infinito y 1/W tiende a cero.
         */
        double invWi3 = inversoSeguro(pesos.getWi3());
        double invWi4 = inversoSeguro(pesos.getWi4());
        double invWi5 = inversoSeguro(pesos.getWi5());
        double invWi6 = inversoSeguro(pesos.getWi6());
        double invWi7 = inversoSeguro(pesos.getWi7());

        double terminoComun = ms4 * ms4 * invWi4;

        double a11 =
                ms3 * ms3 * invWi3
                        + terminoComun
                        + ms5 * ms5 * invWi5;

        double a12 = -terminoComun;
        double a21 = -terminoComun;

        double a22 =
                terminoComun
                        + ms6 * ms6 * invWi6
                        + ms7 * ms7 * invWi7;

        double[] lambdas = resolverSistemaSemidefinido(
                a11,
                a12,
                a21,
                a22,
                delta1,
                delta2,
                registro.getMalla()
        );

        return new ResultadoLagrange(
                lambdas[0],
                lambdas[1],
                delta1,
                delta2
        );
    }

    private double[] resolverSistemaSemidefinido(
            double a11,
            double a12,
            double a21,
            double a22,
            double b1,
            double b2,
            String malla) {

        double escala = Math.max(
                1.0,
                Math.max(
                        Math.max(Math.abs(a11), Math.abs(a12)),
                        Math.max(Math.abs(a21), Math.abs(a22))
                )
        );

        double tolerancia =
                TOLERANCIA_MATRIZ * escala;

        double determinante =
                a11 * a22 - a12 * a21;

        /*
         * Caso normal: matriz invertible.
         */
        if (Math.abs(determinante)
                > TOLERANCIA_MATRIZ * escala * escala) {

            double lambda1 =
                    (b1 * a22 - a12 * b2)
                            / determinante;

            double lambda2 =
                    (a11 * b2 - b1 * a21)
                            / determinante;

            validarResultado(lambda1, lambda2, malla);

            return new double[]{lambda1, lambda2};
        }

        /*
         * Caso típico para F4 = F6 = F7 = 100 %:
         *
         * La segunda ecuación no contiene información para
         * determinar lambda2, pero el desajuste delta2 es cero.
         *
         * Se adopta lambda2 = 0 y se resuelve lambda1.
         */
        if (Math.abs(a22) <= tolerancia
                && Math.abs(a12) <= tolerancia
                && Math.abs(a21) <= tolerancia
                && Math.abs(b2) <= TOLERANCIA_RESIDUO) {

            if (Math.abs(a11) <= tolerancia) {

                if (Math.abs(b1)
                        <= TOLERANCIA_RESIDUO) {

                    return new double[]{0.0, 0.0};
                }

                throw new IllegalStateException(
                        "No se puede ajustar la malla "
                                + malla
                                + ": la ecuación del nodo I "
                                + "no tiene coeficientes, pero presenta "
                                + "un desajuste de " + b1
                );
            }

            double lambda1 = b1 / a11;
            double lambda2 = 0.0;

            validarResultado(lambda1, lambda2, malla);

            return new double[]{lambda1, lambda2};
        }

        /*
         * Caso simétrico: la primera ecuación no aporta
         * información y ya está balanceada.
         */
        if (Math.abs(a11) <= tolerancia
                && Math.abs(a12) <= tolerancia
                && Math.abs(a21) <= tolerancia
                && Math.abs(b1) <= TOLERANCIA_RESIDUO) {

            if (Math.abs(a22) <= tolerancia) {

                if (Math.abs(b2)
                        <= TOLERANCIA_RESIDUO) {

                    return new double[]{0.0, 0.0};
                }

                throw new IllegalStateException(
                        "No se puede ajustar la malla "
                                + malla
                                + ": la ecuación del nodo II "
                                + "no tiene coeficientes, pero presenta "
                                + "un desajuste de " + b2
                );
            }

            double lambda1 = 0.0;
            double lambda2 = b2 / a22;

            validarResultado(lambda1, lambda2, malla);

            return new double[]{lambda1, lambda2};
        }

        /*
         * Si la matriz es singular, pero ninguna de las
         * ecuaciones puede eliminarse de manera segura,
         * se aplica una regularización numérica pequeña.
         */
        double regularizacion =
                Math.max(1.0e-14, escala * 1.0e-12);

        double ar11 = a11 + regularizacion;
        double ar22 = a22 + regularizacion;

        double determinanteRegularizado =
                ar11 * ar22 - a12 * a21;

        if (Math.abs(determinanteRegularizado)
                <= TOLERANCIA_MATRIZ
                * escala
                * escala) {

            throw new IllegalStateException(
                    "No se pudo resolver la matriz de Lagrange "
                            + "para la malla " + malla
                            + ". Determinante: " + determinante
            );
        }

        double lambda1 =
                (b1 * ar22 - a12 * b2)
                        / determinanteRegularizado;

        double lambda2 =
                (ar11 * b2 - b1 * a21)
                        / determinanteRegularizado;

        validarResultado(lambda1, lambda2, malla);

        return new double[]{lambda1, lambda2};
    }

    private double inversoSeguro(double peso) {

        if (!Double.isFinite(peso)) {
            return 0.0;
        }

        if (peso <= 0) {
            throw new IllegalArgumentException(
                    "El factor de ponderación debe ser positivo"
            );
        }

        /*
         * Para pesos extremadamente grandes, su inverso
         * se considera numéricamente cero.
         */
        if (peso >= 1.0e30) {
            return 0.0;
        }

        return 1.0 / peso;
    }

    private void validarEntradas(
            RegistroGranulometrico registro,
            ResultadoTonelajes tonelajes,
            FactorPonderacion pesos) {

        if (registro == null) {
            throw new IllegalArgumentException(
                    "El registro granulométrico no puede ser nulo"
            );
        }

        if (tonelajes == null) {
            throw new IllegalArgumentException(
                    "Los tonelajes no pueden ser nulos"
            );
        }

        if (pesos == null) {
            throw new IllegalArgumentException(
                    "Los factores de ponderación no pueden ser nulos"
            );
        }
    }

    private void validarResultado(
            double lambda1,
            double lambda2,
            String malla) {

        if (!Double.isFinite(lambda1)
                || !Double.isFinite(lambda2)) {

            throw new IllegalStateException(
                    "Se obtuvieron multiplicadores no válidos "
                            + "para la malla " + malla
            );
        }
    }
}