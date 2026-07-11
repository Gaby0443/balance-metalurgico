package com.balance.servicio;

import com.balance.modelo.ResultadoAjuste;

import java.util.List;
import java.util.function.ToDoubleFunction;

public class CalculadoraErrores {

    public double promedioF2(List<ResultadoAjuste> resultados) {
        return promedio(resultados, ResultadoAjuste::getErrorF2);
    }

    public double promedioF3(List<ResultadoAjuste> resultados) {
        return promedio(resultados, ResultadoAjuste::getErrorF3);
    }

    public double promedioF4(List<ResultadoAjuste> resultados) {
        return promedio(resultados, ResultadoAjuste::getErrorF4);
    }

    public double promedioF5(List<ResultadoAjuste> resultados) {
        return promedio(resultados, ResultadoAjuste::getErrorF5);
    }

    public double promedioF6(List<ResultadoAjuste> resultados) {
        return promedio(resultados, ResultadoAjuste::getErrorF6);
    }

    public double promedioF7(List<ResultadoAjuste> resultados) {
        return promedio(resultados, ResultadoAjuste::getErrorF7);
    }

    public double promedioF8(List<ResultadoAjuste> resultados) {
        return promedio(resultados, ResultadoAjuste::getErrorF8);
    }

    public double calcularErrorTotal(
            List<ResultadoAjuste> resultados) {

        return promedioF2(resultados)
                + promedioF3(resultados)
                + promedioF4(resultados)
                + promedioF5(resultados)
                + promedioF6(resultados)
                + promedioF7(resultados)
                + promedioF8(resultados);
    }

    public double calcularErrorPromedio(
            List<ResultadoAjuste> resultados) {

        return calcularErrorTotal(resultados) / 7.0;
    }

    private double promedio(
            List<ResultadoAjuste> resultados,
            ToDoubleFunction<ResultadoAjuste> extractor) {

        if (resultados == null || resultados.isEmpty()) {
            return 0.0;
        }

        return resultados.stream()
                .mapToDouble(extractor)
                .average()
                .orElse(0.0);
    }
}