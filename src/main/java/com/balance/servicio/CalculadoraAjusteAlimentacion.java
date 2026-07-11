package com.balance.servicio;

import com.balance.modelo.FactorPonderacion;
import com.balance.modelo.RegistroGranulometrico;
import com.balance.modelo.ResultadoAjuste;
import com.balance.modelo.ResultadoTonelajes;

public class CalculadoraAjusteAlimentacion {

    private static final double EPSILON = 1.0e-15;

    public double[] calcular(
            RegistroGranulometrico registro,
            ResultadoAjuste ajusteClasificacion,
            ResultadoTonelajes tonelajes,
            double wi1,
            double wi2) {

        if (wi1 <= 0 || wi2 <= 0) {
            throw new IllegalArgumentException(
                    "Los factores Wi1 y Wi2 deben ser positivos"
            );
        }

        /*
         * De acuerdo con la tabla del circuito:
         *
         * MS1 = MS6
         * MS2 = MS3
         */
        double ms1 = tonelajes.getMs6();
        double ms2 = tonelajes.getMs3();
        double ms5 = tonelajes.getMs5();
        double ms7 = tonelajes.getMs7();

        /*
         * Material proveniente de los retornos.
         */
        double retorno =
                ms5 * ajusteClasificacion.getF5Ajustado()
                        + ms7 * ajusteClasificacion.getF7Ajustado();

        /*
         * Desajuste de la alimentación:
         *
         * MS2*F2 - MS1*F1 - retorno
         */
        double delta =
                ms2 * registro.getF2()
                        - ms1 * registro.getF1()
                        - retorno;

        double denominador =
                (ms2 * ms2) / wi2
                        + (ms1 * ms1) / wi1;

        if (Math.abs(denominador) <= EPSILON) {
            throw new IllegalStateException(
                    "No se puede ajustar F1 y F2 para la malla "
                            + registro.getMalla()
            );
        }

        double lambda = delta / denominador;

        double f1Ajustado =
                registro.getF1()
                        + (ms1 * lambda) / wi1;

        double f2Ajustado =
                registro.getF2()
                        - (ms2 * lambda) / wi2;

        return new double[]{
                corregirLimite(f1Ajustado),
                corregirLimite(f2Ajustado)
        };
    }

    private double corregirLimite(double valor) {
        double tolerancia = 1.0e-10;

        if (valor < 0 && valor >= -tolerancia) {
            return 0;
        }

        if (valor > 100 && valor <= 100 + tolerancia) {
            return 100;
        }

        return valor;
    }
}