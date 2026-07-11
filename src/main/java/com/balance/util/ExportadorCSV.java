package com.balance.util;

import com.balance.modelo.FactorPonderacion;
import com.balance.modelo.ResultadoAjuste;
import com.balance.modelo.ResultadoTonelajes;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExportadorCSV {

    public void exportarAjustes(
            Path rutaArchivo,
            List<ResultadoAjuste> resultados)
            throws IOException {

        if (resultados == null || resultados.isEmpty()) {
            throw new IllegalArgumentException(
                    "No existen resultados de ajuste para exportar"
            );
        }

        crearDirectorioPadre(rutaArchivo);

        try (BufferedWriter escritor = Files.newBufferedWriter(
                rutaArchivo,
                StandardCharsets.UTF_8)) {

            escritor.write(
                    "malla,abertura,"
                            + "f3_medido,f3_ajustado,"
                            + "f4_medido,f4_ajustado,"
                            + "f5_medido,f5_ajustado,"
                            + "f6_medido,f6_ajustado,"
                            + "f7_medido,f7_ajustado,"
                            + "lambda1,lambda2,"
                            + "residuo_nodo1,residuo_nodo2"
            );

            escritor.newLine();

            for (ResultadoAjuste resultado : resultados) {

                escritor.write(String.join(",",
                        escaparTexto(resultado.getMalla()),
                        numero(resultado.getAbertura()),

                        numero(resultado.getF3Medido()),
                        numero(resultado.getF3Ajustado()),

                        numero(resultado.getF4Medido()),
                        numero(resultado.getF4Ajustado()),

                        numero(resultado.getF5Medido()),
                        numero(resultado.getF5Ajustado()),

                        numero(resultado.getF6Medido()),
                        numero(resultado.getF6Ajustado()),

                        numero(resultado.getF7Medido()),
                        numero(resultado.getF7Ajustado()),

                        numero(resultado.getLambda1()),
                        numero(resultado.getLambda2()),

                        numero(resultado.getResiduoNodo1()),
                        numero(resultado.getResiduoNodo2())
                ));

                escritor.newLine();
            }
        }
    }

    public void exportarTonelajes(
            Path rutaArchivo,
            ResultadoTonelajes tonelajes)
            throws IOException {

        if (tonelajes == null) {
            throw new IllegalArgumentException(
                    "Los tonelajes no pueden ser nulos"
            );
        }

        crearDirectorioPadre(rutaArchivo);

        try (BufferedWriter escritor = Files.newBufferedWriter(
                rutaArchivo,
                StandardCharsets.UTF_8)) {

            escritor.write("flujo,tonelaje");
            escritor.newLine();

            escribirTonelaje(
                    escritor,
                    "MS3",
                    tonelajes.getMs3()
            );

            escribirTonelaje(
                    escritor,
                    "MS4",
                    tonelajes.getMs4()
            );

            escribirTonelaje(
                    escritor,
                    "MS5",
                    tonelajes.getMs5()
            );

            escribirTonelaje(
                    escritor,
                    "MS6",
                    tonelajes.getMs6()
            );

            escribirTonelaje(
                    escritor,
                    "MS7",
                    tonelajes.getMs7()
            );

            escribirTonelaje(
                    escritor,
                    "MS8",
                    tonelajes.getMs8()
            );
        }
    }

    public void exportarFactoresPonderacion(
            Path rutaArchivo,
            Map<String, FactorPonderacion> factores)
            throws IOException {

        if (factores == null || factores.isEmpty()) {
            throw new IllegalArgumentException(
                    "No existen factores de ponderación para exportar"
            );
        }

        crearDirectorioPadre(rutaArchivo);

        try (BufferedWriter escritor = Files.newBufferedWriter(
                rutaArchivo,
                StandardCharsets.UTF_8)) {

            escritor.write(
                    "malla,wi3,wi4,wi5,wi6,wi7"
            );

            escritor.newLine();

            for (FactorPonderacion factor : factores.values()) {

                escritor.write(String.join(",",
                        escaparTexto(factor.getMalla()),
                        numero(factor.getWi3()),
                        numero(factor.getWi4()),
                        numero(factor.getWi5()),
                        numero(factor.getWi6()),
                        numero(factor.getWi7())
                ));

                escritor.newLine();
            }
        }
    }

    private void escribirTonelaje(
            BufferedWriter escritor,
            String flujo,
            double valor)
            throws IOException {

        escritor.write(
                escaparTexto(flujo)
                        + ","
                        + numero(valor)
        );

        escritor.newLine();
    }

    private void crearDirectorioPadre(
            Path rutaArchivo)
            throws IOException {

        if (rutaArchivo == null) {
            throw new IllegalArgumentException(
                    "La ruta de exportación no puede ser nula"
            );
        }

        Path padre =
                rutaArchivo.toAbsolutePath().getParent();

        if (padre != null) {
            Files.createDirectories(padre);
        }
    }

    private String numero(double valor) {

        if (!Double.isFinite(valor)) {
            throw new IllegalArgumentException(
                    "No se puede exportar un valor numérico no válido: "
                            + valor
            );
        }

        /*
         * Se utiliza notación científica para números muy grandes
         * o muy pequeños, como los factores de ponderación asociados
         * a porcentajes de 0 % o 100 %.
         */
        if (Math.abs(valor) >= 1.0e12
                || (valor != 0 && Math.abs(valor) < 1.0e-9)) {

            return String.format(
                    Locale.US,
                    "%.12e",
                    valor
            );
        }

        return String.format(
                Locale.US,
                "%.12f",
                valor
        );
    }

    private String escaparTexto(String texto) {

        if (texto == null) {
            return "";
        }

        /*
         * Escapa textos que contengan comas, comillas
         * o saltos de línea para mantener un CSV válido.
         */
        if (texto.contains(",")
                || texto.contains("\"")
                || texto.contains("\n")
                || texto.contains("\r")) {

            return "\""
                    + texto.replace("\"", "\"\"")
                    + "\"";
        }

        return texto;
    }
}