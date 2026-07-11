package com.balance.servicio;

import com.balance.modelo.ResultadoAjuste;

import java.util.List;

public class ValidadorBalance {

    private final double tolerancia;

    public ValidadorBalance() {
        this(1.0e-8);
    }

    public ValidadorBalance(double tolerancia) {

        if (!Double.isFinite(tolerancia)
                || tolerancia <= 0) {

            throw new IllegalArgumentException(
                    "La tolerancia debe ser mayor que cero"
            );
        }

        this.tolerancia = tolerancia;
    }

    public boolean esValido(ResultadoAjuste resultado) {

        return Math.abs(resultado.getResiduoNodo1())
                <= tolerancia
                && Math.abs(resultado.getResiduoNodo2())
                <= tolerancia;
    }

    public boolean todosSonValidos(
            List<ResultadoAjuste> resultados) {

        return resultados.stream()
                .allMatch(this::esValido);
    }

    public double calcularErrorMaximo(
            List<ResultadoAjuste> resultados) {

        return resultados.stream()
                .mapToDouble(resultado ->
                        Math.max(
                                Math.abs(
                                        resultado.getResiduoNodo1()
                                ),
                                Math.abs(
                                        resultado.getResiduoNodo2()
                                )
                        )
                )
                .max()
                .orElse(0);
    }
    public double calcularErrorTotal(
            List<ResultadoAjuste> resultados) {

        return resultados.stream()
                .mapToDouble(ResultadoAjuste::getErrorTotalFila)
                .sum();
    }

    public double calcularErrorPromedio(
            List<ResultadoAjuste> resultados) {

        if (resultados == null || resultados.isEmpty()) {
            return 0;
        }

        int cantidadValores = resultados.size() * 7;

        double suma = resultados.stream()
                .mapToDouble(ResultadoAjuste::getErrorTotalFila)
                .sum();

        return suma / cantidadValores;
    }
}