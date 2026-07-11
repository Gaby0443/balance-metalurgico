package com.balance.util;

import com.balance.modelo.DatosEntradaCSV;
import com.balance.modelo.RegistroGranulometrico;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ImportadorCSV {

    private static final int CANTIDAD_COLUMNAS = 9;

    public DatosEntradaCSV importar(
            Path rutaArchivo) throws IOException {

        if (rutaArchivo == null) {
            throw new IllegalArgumentException(
                    "La ruta del archivo no puede ser nula"
            );
        }

        if (!Files.exists(rutaArchivo)) {
            throw new IOException(
                    "No existe el archivo: "
                            + rutaArchivo.toAbsolutePath()
            );
        }

        List<RegistroGranulometrico> registros =
                new ArrayList<>();

        try (BufferedReader lector =
                     Files.newBufferedReader(
                             rutaArchivo,
                             StandardCharsets.UTF_8)) {

            String linea;
            int numeroLinea = 0;
            boolean encabezadoProcesado = false;

            while ((linea = lector.readLine()) != null) {

                numeroLinea++;
                linea = linea.trim();

                if (linea.isEmpty()
                        || linea.startsWith("#")) {
                    continue;
                }

                String[] columnas = separarLinea(linea);

                if (!encabezadoProcesado
                        && esEncabezado(columnas)) {

                    encabezadoProcesado = true;
                    continue;
                }

                encabezadoProcesado = true;

                if (columnas.length != CANTIDAD_COLUMNAS) {
                    throw new IOException(
                            "La línea " + numeroLinea
                                    + " tiene "
                                    + columnas.length
                                    + " columnas. Se esperaban "
                                    + CANTIDAD_COLUMNAS
                    );
                }

                try {
                    String malla =
                            limpiarTexto(columnas[0]);

                    double abertura =
                            convertirDouble(columnas[1]);

                    double f1 =
                            convertirDouble(columnas[2]);

                    double f2 =
                            convertirDouble(columnas[3]);

                    double f3 =
                            convertirDouble(columnas[4]);

                    double f4 =
                            convertirDouble(columnas[5]);

                    double f5 =
                            convertirDouble(columnas[6]);

                    double f6 =
                            convertirDouble(columnas[7]);

                    double f7 =
                            convertirDouble(columnas[8]);

                    registros.add(
                            new RegistroGranulometrico(
                                    malla,
                                    abertura,
                                    f1,
                                    f2,
                                    f3,
                                    f4,
                                    f5,
                                    f6,
                                    f7
                            )
                    );

                } catch (IllegalArgumentException excepcion) {

                    throw new IOException(
                            "Error en la línea "
                                    + numeroLinea
                                    + ": "
                                    + excepcion.getMessage(),
                            excepcion
                    );
                }
            }
        }

        if (registros.isEmpty()) {
            throw new IOException(
                    "El CSV no contiene registros"
            );
        }

        return new DatosEntradaCSV(registros);
    }

    private String[] separarLinea(String linea) {

        String separador =
                linea.contains(";") ? ";" : ",";

        return linea.split(separador, -1);
    }

    private boolean esEncabezado(
            String[] columnas) {

        if (columnas.length == 0) {
            return false;
        }

        String primera =
                limpiarTexto(columnas[0])
                        .toLowerCase(Locale.ROOT);

        return primera.equals("malla")
                || primera.equals("mesh");
    }

    private String limpiarTexto(String texto) {

        return texto
                .replace("\uFEFF", "")
                .replace("\"", "")
                .trim();
    }

    private double convertirDouble(String texto) {

        String valor = limpiarTexto(texto);

        if (valor.isBlank()) {
            throw new IllegalArgumentException(
                    "Se encontró una celda vacía"
            );
        }

        try {
            return Double.parseDouble(valor);

        } catch (NumberFormatException excepcion) {

            try {
                return Double.parseDouble(
                        valor.replace(",", ".")
                );

            } catch (NumberFormatException segunda) {

                throw new IllegalArgumentException(
                        "Valor numérico inválido: "
                                + texto
                );
            }
        }
    }
}