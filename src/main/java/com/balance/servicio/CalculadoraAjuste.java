package com.balance.servicio;

import com.balance.modelo.FactorPonderacion;
import com.balance.modelo.RegistroGranulometrico;
import com.balance.modelo.ResultadoAjuste;
import com.balance.modelo.ResultadoLagrange;
import com.balance.modelo.ResultadoTonelajes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalculadoraAjuste {

    private static final double EPSILON = 1.0e-12;

    private final CalculadoraLagrange calculadoraLagrange;
    private final CalculadoraAjusteAlimentacion calculadoraAlimentacion;

    public CalculadoraAjuste() {
        this.calculadoraLagrange =
                new CalculadoraLagrange();

        this.calculadoraAlimentacion =
                new CalculadoraAjusteAlimentacion();
    }

    public ResultadoAjuste calcular(
            RegistroGranulometrico registro,
            ResultadoTonelajes tonelajes,
            FactorPonderacion pesos) {

        validarEntradas(
                registro,
                tonelajes,
                pesos
        );

        ResultadoLagrange lagrange =
                calculadoraLagrange.calcular(
                        registro,
                        tonelajes,
                        pesos
                );

        double lambda1 = lagrange.lambda1();
        double lambda2 = lagrange.lambda2();

        double ms3 = tonelajes.getMs3();
        double ms4 = tonelajes.getMs4();
        double ms5 = tonelajes.getMs5();
        double ms6 = tonelajes.getMs6();
        double ms7 = tonelajes.getMs7();
        double ms8 = tonelajes.getMs8();

        if (Math.abs(ms8) <= EPSILON) {
            throw new IllegalStateException(
                    "No se puede calcular F8 porque MS8 es cero"
            );
        }

        /*
         * Ajuste de F3 a F7.
         */

        double f3Ajustado =
                registro.getF3()
                        - (ms3 * lambda1)
                        / pesos.getWi3();

        double f4Ajustado =
                registro.getF4()
                        + (
                        ms4
                                * (lambda1 - lambda2)
                ) / pesos.getWi4();

        double f5Ajustado =
                registro.getF5()
                        + (ms5 * lambda1)
                        / pesos.getWi5();

        double f6Ajustado =
                registro.getF6()
                        - (ms6 * lambda2)
                        / pesos.getWi6();

        double f7Ajustado =
                registro.getF7()
                        + (ms7 * lambda2)
                        / pesos.getWi7();

        f3Ajustado =
                corregirLimitesNumericos(f3Ajustado);

        f4Ajustado =
                corregirLimitesNumericos(f4Ajustado);

        f5Ajustado =
                corregirLimitesNumericos(f5Ajustado);

        f6Ajustado =
                corregirLimitesNumericos(f6Ajustado);

        f7Ajustado =
                corregirLimitesNumericos(f7Ajustado);

        /*
         * F8 medido:
         *
         * MS8·F8 = MS5·F5 + MS7·F7
         */

        double f8Medido =
                (
                        ms5 * registro.getF5()
                                + ms7 * registro.getF7()
                ) / ms8;

        /*
         * F8 ajustado:
         *
         * MS8·F8aj = MS5·F5aj + MS7·F7aj
         */

        double f8Ajustado =
                (
                        ms5 * f5Ajustado
                                + ms7 * f7Ajustado
                ) / ms8;

        f8Medido =
                corregirLimitesNumericos(f8Medido);

        f8Ajustado =
                corregirLimitesNumericos(f8Ajustado);

        /*
         * Residuo de los nodos I y II.
         */

        double residuoNodo1 =
                ms3 * f3Ajustado
                        - ms4 * f4Ajustado
                        - ms5 * f5Ajustado;

        double residuoNodo2 =
                ms4 * f4Ajustado
                        - ms6 * f6Ajustado
                        - ms7 * f7Ajustado;

        /*
         * Resultado temporal para poder ajustar F1 y F2.
         */

        ResultadoAjuste resultadoTemporal =
                new ResultadoAjuste(
                        registro.getMalla(),
                        registro.getAbertura(),

                        // Medidos F1-F8
                        registro.getF1(),
                        registro.getF2(),
                        registro.getF3(),
                        registro.getF4(),
                        registro.getF5(),
                        registro.getF6(),
                        registro.getF7(),
                        f8Medido,

                        // Ajustados F1-F8
                        // F1 y F2 todavía sin ajustar
                        registro.getF1(),
                        registro.getF2(),
                        f3Ajustado,
                        f4Ajustado,
                        f5Ajustado,
                        f6Ajustado,
                        f7Ajustado,
                        f8Ajustado,

                        lambda1,
                        lambda2,

                        residuoNodo1,
                        residuoNodo2
                );

        /*
         * Ajuste de F1 y F2.
         */

        double[] alimentacionAjustada =
                calculadoraAlimentacion.calcular(
                        registro,
                        resultadoTemporal,
                        tonelajes,
                        pesos.getWi1(),
                        pesos.getWi2()
                );

        double f1Ajustado =
                corregirLimitesNumericos(
                        alimentacionAjustada[0]
                );

        double f2Ajustado =
                corregirLimitesNumericos(
                        alimentacionAjustada[1]
                );

        /*
         * Resultado final completo F1-F8.
         */

        return new ResultadoAjuste(
                registro.getMalla(),
                registro.getAbertura(),

                // Medidos F1-F8
                registro.getF1(),
                registro.getF2(),
                registro.getF3(),
                registro.getF4(),
                registro.getF5(),
                registro.getF6(),
                registro.getF7(),
                f8Medido,

                // Ajustados F1-F8
                f1Ajustado,
                f2Ajustado,
                f3Ajustado,
                f4Ajustado,
                f5Ajustado,
                f6Ajustado,
                f7Ajustado,
                f8Ajustado,

                lambda1,
                lambda2,

                residuoNodo1,
                residuoNodo2
        );
    }

    public List<ResultadoAjuste> calcularTodos(
            List<RegistroGranulometrico> registros,
            ResultadoTonelajes tonelajes,
            Map<String, FactorPonderacion> factoresPorMalla) {

        if (registros == null || registros.isEmpty()) {
            throw new IllegalArgumentException(
                    "No existen registros granulométricos"
            );
        }

        if (tonelajes == null) {
            throw new IllegalArgumentException(
                    "Los tonelajes no pueden ser nulos"
            );
        }

        if (factoresPorMalla == null
                || factoresPorMalla.isEmpty()) {

            throw new IllegalArgumentException(
                    "No existen factores de ponderación"
            );
        }

        List<ResultadoAjuste> resultados =
                new ArrayList<>();

        for (RegistroGranulometrico registro : registros) {

            FactorPonderacion pesos =
                    factoresPorMalla.get(
                            registro.getMalla()
                    );

            if (pesos == null) {
                throw new IllegalArgumentException(
                        "No se encontraron factores de ponderación "
                                + "para la malla "
                                + registro.getMalla()
                );
            }

            resultados.add(
                    calcular(
                            registro,
                            tonelajes,
                            pesos
                    )
            );
        }

        return resultados;
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

    private double corregirLimitesNumericos(
            double valor) {

        final double tolerancia = 1.0e-10;

        if (valor < 0
                && valor >= -tolerancia) {

            return 0.0;
        }

        if (valor > 100
                && valor <= 100 + tolerancia) {

            return 100.0;
        }

        return valor;
    }
}