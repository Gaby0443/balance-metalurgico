package com.balance.servicio;

import com.balance.modelo.FactorPonderacion;
import com.balance.modelo.RegistroGranulometrico;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CalculadoraFactoresPonderacion {

    /*
     * Se evita usar exactamente 0 o 1 porque:
     *
     * W = Uk / [F²(1-F)²]
     *
     * tendría división entre cero.
     */
    private static final double EPSILON = 1.0e-12;
    private static final double PESO_MAXIMO = 4.0e34;

    private final double uk;

    public CalculadoraFactoresPonderacion() {
        this(4.0);
    }

    public CalculadoraFactoresPonderacion(double uk) {

        if (!Double.isFinite(uk) || uk <= 0) {
            throw new IllegalArgumentException(
                    "Uk debe ser un número mayor que cero"
            );
        }

        this.uk = uk;
    }

    public FactorPonderacion calcular(
            RegistroGranulometrico registro) {

        if (registro == null) {
            throw new IllegalArgumentException(
                    "El registro granulométrico no puede ser nulo"
            );
        }

        double wi3 = calcularPeso(registro.getF3());
        double wi4 = calcularPeso(registro.getF4());
        double wi5 = calcularPeso(registro.getF5());
        double wi6 = calcularPeso(registro.getF6());
        double wi7 = calcularPeso(registro.getF7());

        return new FactorPonderacion(
                registro.getMalla(),
                calcularPeso(registro.getF1()),
                calcularPeso(registro.getF2()),
                calcularPeso(registro.getF3()),
                calcularPeso(registro.getF4()),
                calcularPeso(registro.getF5()),
                calcularPeso(registro.getF6()),
                calcularPeso(registro.getF7())
        );
    }

    public Map<String, FactorPonderacion> calcularTodos(
            List<RegistroGranulometrico> registros) {

        if (registros == null || registros.isEmpty()) {
            throw new IllegalArgumentException(
                    "No existen registros granulométricos"
            );
        }

        Map<String, FactorPonderacion> factores =
                new LinkedHashMap<>();

        for (RegistroGranulometrico registro : registros) {

            if (factores.containsKey(registro.getMalla())) {
                throw new IllegalArgumentException(
                        "La malla está repetida: "
                                + registro.getMalla()
                );
            }

            factores.put(
                    registro.getMalla(),
                    calcular(registro)
            );
        }

        return factores;
    }

    public double calcularPeso(double porcentajePasante) {

        if (!Double.isFinite(porcentajePasante)) {
            throw new IllegalArgumentException(
                    "El porcentaje pasante no es válido"
            );
        }

        if (porcentajePasante < 0
                || porcentajePasante > 100) {

            throw new IllegalArgumentException(
                    "El porcentaje pasante debe estar entre 0 y 100"
            );
        }

        /*
         * Para 0 % y 100 %, el peso matemático es infinito.
         * Se utiliza un valor máximo equivalente a una medición
         * prácticamente fija.
         */
        if (porcentajePasante == 0.0
                || porcentajePasante == 100.0) {

            return PESO_MAXIMO;
        }

        double f = porcentajePasante / 100.0;

        double denominador =
                f * f
                        * (1.0 - f)
                        * (1.0 - f);

        double peso = uk / denominador;

        if (!Double.isFinite(peso) || peso <= 0) {
            throw new IllegalStateException(
                    "No se pudo calcular el peso para F = "
                            + porcentajePasante
            );
        }

        return Math.min(peso, PESO_MAXIMO);
    }
}