package com.balance;

import com.balance.modelo.DatosEntradaCSV;
import com.balance.modelo.FactorPonderacion;
import com.balance.modelo.ResultadoAjuste;
import com.balance.modelo.ResultadoTonelajes;
import com.balance.servicio.CalculadoraAjuste;
import com.balance.servicio.CalculadoraFactoresPonderacion;
import com.balance.servicio.CalculadoraTonelajes;
import com.balance.servicio.ValidadorBalance;
import com.balance.util.ExportadorCSV;
import com.balance.util.ImportadorCSV;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.ToDoubleFunction;

public class Main {

    public static void main(String[] args) {

        Locale.setDefault(Locale.US);

        Path archivoEntrada = Path.of(
                "src",
                "main",
                "resources",
                "datos_balance.csv"
        );

        Path carpetaResultados = Path.of("resultados");

        Path archivoTonelajes =
                carpetaResultados.resolve(
                        "tonelajes_calculados.csv"
                );

        Path archivoAjustes =
                carpetaResultados.resolve(
                        "datos_ajustados.csv"
                );

        Path archivoFactores =
                carpetaResultados.resolve(
                        "factores_ponderacion.csv"
                );

        try {
            /*
             * 1. Importar los datos granulométricos.
             */
            ImportadorCSV importador =
                    new ImportadorCSV();

            DatosEntradaCSV datos =
                    importador.importar(archivoEntrada);

            /*
             * 2. Calcular factores de ponderación.
             */
            double uk = 5.0;

            CalculadoraFactoresPonderacion calculadoraPesos =
                    new CalculadoraFactoresPonderacion(uk);

            Map<String, FactorPonderacion> factoresPorMalla =
                    calculadoraPesos.calcularTodos(
                            datos.getRegistros()
                    );

            /*
             * 3. Calcular tonelajes normalizados.
             *
             * Se conserva MS3 = 1.
             * No se realiza escalamiento posterior.
             */
            double ms3Base = 1.0;

            CalculadoraTonelajes calculadoraTonelajes =
                    new CalculadoraTonelajes();

            ResultadoTonelajes tonelajes =
                    calculadoraTonelajes.calcular(
                            datos.getRegistros(),
                            ms3Base
                    );

            /*
             * 4. Calcular valores ajustados F1-F8.
             */
            CalculadoraAjuste calculadoraAjuste =
                    new CalculadoraAjuste();

            List<ResultadoAjuste> ajustes =
                    calculadoraAjuste.calcularTodos(
                            datos.getRegistros(),
                            tonelajes,
                            factoresPorMalla
                    );

            /*
             * 5. Validar los residuos de cierre.
             */
            ValidadorBalance validador =
                    new ValidadorBalance(1.0e-8);

            boolean balanceValido =
                    validador.todosSonValidos(ajustes);

            double residuoMaximo =
                    validador.calcularErrorMaximo(ajustes);

            /*
             * 6. Calcular promedios de error por flujo.
             *
             * Los métodos getErrorF2() ... getErrorF8()
             * deben devolver error relativo porcentual.
             */
            double promedioF2 =
                    calcularPromedio(
                            ajustes,
                            ResultadoAjuste::getErrorF2
                    );

            double promedioF3 =
                    calcularPromedio(
                            ajustes,
                            ResultadoAjuste::getErrorF3
                    );

            double promedioF4 =
                    calcularPromedio(
                            ajustes,
                            ResultadoAjuste::getErrorF4
                    );

            double promedioF5 =
                    calcularPromedio(
                            ajustes,
                            ResultadoAjuste::getErrorF5
                    );

            double promedioF6 =
                    calcularPromedio(
                            ajustes,
                            ResultadoAjuste::getErrorF6
                    );

            double promedioF7 =
                    calcularPromedio(
                            ajustes,
                            ResultadoAjuste::getErrorF7
                    );

            double promedioF8 =
                    calcularPromedio(
                            ajustes,
                            ResultadoAjuste::getErrorF8
                    );

            /*
             * El error total del ejemplo es la suma de los
             * siete promedios correspondientes a F2-F8.
             */
            double errorTotal =
                    promedioF2
                            + promedioF3
                            + promedioF4
                            + promedioF5
                            + promedioF6
                            + promedioF7
                            + promedioF8;

            double errorPromedio =
                    errorTotal / 7.0;

            /*
             * 7. Mostrar resultados.
             */
            imprimirFactores(factoresPorMalla);
            imprimirTonelajes(tonelajes);
            imprimirValoresAjustados(ajustes);
            imprimirErroresRelativos(ajustes);

            imprimirResumenErrores(
                    promedioF2,
                    promedioF3,
                    promedioF4,
                    promedioF5,
                    promedioF6,
                    promedioF7,
                    promedioF8,
                    errorTotal,
                    errorPromedio
            );

            System.out.println();
            System.out.println(
                    "=========================================="
            );
            System.out.println(
                    "VALIDACIÓN DEL BALANCE"
            );
            System.out.println(
                    "=========================================="
            );

            System.out.println(
                    "Balance válido: " + balanceValido
            );

            System.out.printf(
                    Locale.US,
                    "Residuo máximo: %.12e%n",
                    residuoMaximo
            );

            /*
             * 8. Exportar resultados.
             */
            ExportadorCSV exportador =
                    new ExportadorCSV();

            exportador.exportarFactoresPonderacion(
                    archivoFactores,
                    factoresPorMalla
            );

            exportador.exportarTonelajes(
                    archivoTonelajes,
                    tonelajes
            );

            exportador.exportarAjustes(
                    archivoAjustes,
                    ajustes
            );

            System.out.println();
            System.out.println(
                    "Archivos exportados correctamente:"
            );

            System.out.println(
                    archivoFactores.toAbsolutePath()
            );

            System.out.println(
                    archivoTonelajes.toAbsolutePath()
            );

            System.out.println(
                    archivoAjustes.toAbsolutePath()
            );

        } catch (IOException excepcion) {

            System.err.println(
                    "Error al leer o escribir archivos: "
                            + excepcion.getMessage()
            );

        } catch (IllegalArgumentException
                 | IllegalStateException excepcion) {

            System.err.println(
                    "No se pudo realizar el balance: "
                            + excepcion.getMessage()
            );

        } catch (Exception excepcion) {

            System.err.println(
                    "Ocurrió un error inesperado: "
                            + excepcion.getClass().getSimpleName()
                            + " - "
                            + excepcion.getMessage()
            );

            excepcion.printStackTrace();
        }
    }

    private static void imprimirFactores(
            Map<String, FactorPonderacion> factoresPorMalla) {

        System.out.println();
        System.out.println(
                "============================================================================================================================"
        );
        System.out.println(
                "FACTORES DE PONDERACIÓN"
        );
        System.out.println(
                "============================================================================================================================"
        );

        System.out.printf(
                "%-7s %14s %14s %14s %14s "
                        + "%14s %14s %14s%n",
                "Malla",
                "Wi1",
                "Wi2",
                "Wi3",
                "Wi4",
                "Wi5",
                "Wi6",
                "Wi7"
        );

        for (FactorPonderacion factor
                : factoresPorMalla.values()) {

            System.out.printf(
                    "%-7s %14s %14s %14s %14s "
                            + "%14s %14s %14s%n",

                    factor.getMalla(),

                    formatearFactor(factor.getWi1()),
                    formatearFactor(factor.getWi2()),
                    formatearFactor(factor.getWi3()),
                    formatearFactor(factor.getWi4()),
                    formatearFactor(factor.getWi5()),
                    formatearFactor(factor.getWi6()),
                    formatearFactor(factor.getWi7())
            );
        }
    }

    private static void imprimirTonelajes(
            ResultadoTonelajes tonelajes) {

        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "TONELAJES ÓPTIMOS NORMALIZADOS"
        );
        System.out.println(
                "=========================================="
        );

        System.out.printf(
                Locale.US,
                "MS1 = %.9f%n",
                tonelajes.getMs1()
        );

        System.out.printf(
                Locale.US,
                "MS2 = %.9f%n",
                tonelajes.getMs2()
        );

        System.out.printf(
                Locale.US,
                "MS3 = %.9f%n",
                tonelajes.getMs3()
        );

        System.out.printf(
                Locale.US,
                "MS4 = %.9f%n",
                tonelajes.getMs4()
        );

        System.out.printf(
                Locale.US,
                "MS5 = %.9f%n",
                tonelajes.getMs5()
        );

        System.out.printf(
                Locale.US,
                "MS6 = %.9f%n",
                tonelajes.getMs6()
        );

        System.out.printf(
                Locale.US,
                "MS7 = %.9f%n",
                tonelajes.getMs7()
        );

        System.out.printf(
                Locale.US,
                "MS8 = %.9f%n",
                tonelajes.getMs8()
        );
    }

    /*
     * Tabla de valores ajustados.
     * Aquí ya aparece F8.
     */
    private static void imprimirValoresAjustados(
            List<ResultadoAjuste> ajustes) {

        System.out.println();
        System.out.println(
                "======================================================================================================"
        );
        System.out.println(
                "DATOS GRANULOMÉTRICOS AJUSTADOS"
        );
        System.out.println(
                "======================================================================================================"
        );

        System.out.printf(
                "%-7s %11s %11s %11s %11s "
                        + "%11s %11s %11s %11s%n",

                "Malla",
                "F1 ajust.",
                "F2 ajust.",
                "F3 ajust.",
                "F4 ajust.",
                "F5 ajust.",
                "F6 ajust.",
                "F7 ajust.",
                "F8 ajust."
        );

        for (ResultadoAjuste resultado : ajustes) {

            System.out.printf(
                    Locale.US,
                    "%-7s %11.5f %11.5f %11.5f %11.5f "
                            + "%11.5f %11.5f %11.5f %11.5f%n",

                    resultado.getMalla(),

                    resultado.getF1Ajustado(),
                    resultado.getF2Ajustado(),
                    resultado.getF3Ajustado(),
                    resultado.getF4Ajustado(),
                    resultado.getF5Ajustado(),
                    resultado.getF6Ajustado(),
                    resultado.getF7Ajustado(),
                    resultado.getF8Ajustado()
            );
        }
    }

    /*
     * Tabla de errores relativos porcentuales F2-F8.
     */
    private static void imprimirErroresRelativos(
            List<ResultadoAjuste> ajustes) {

        System.out.println();
        System.out.println(
                "=============================================================================================================================="
        );
        System.out.println(
                "ERRORES RELATIVOS PORCENTUALES"
        );
        System.out.println(
                "=============================================================================================================================="
        );

        System.out.printf(
                "%-7s %11s %11s %11s %11s "
                        + "%11s %11s %11s %15s %15s%n",

                "Malla",
                "Error F2",
                "Error F3",
                "Error F4",
                "Error F5",
                "Error F6",
                "Error F7",
                "Error F8",
                "Residuo N1",
                "Residuo N2"
        );

        for (ResultadoAjuste resultado : ajustes) {

            System.out.printf(
                    Locale.US,
                    "%-7s %11.6f %11.6f %11.6f %11.6f "
                            + "%11.6f %11.6f %11.6f %15.5e %15.5e%n",

                    resultado.getMalla(),

                    resultado.getErrorF2(),
                    resultado.getErrorF3(),
                    resultado.getErrorF4(),
                    resultado.getErrorF5(),
                    resultado.getErrorF6(),
                    resultado.getErrorF7(),
                    resultado.getErrorF8(),

                    resultado.getResiduoNodo1(),
                    resultado.getResiduoNodo2()
            );
        }
    }

    private static void imprimirResumenErrores(
            double promedioF2,
            double promedioF3,
            double promedioF4,
            double promedioF5,
            double promedioF6,
            double promedioF7,
            double promedioF8,
            double errorTotal,
            double errorPromedio) {

        System.out.println();
        System.out.println(
                "=========================================="
        );
        System.out.println(
                "PROMEDIO DE ERROR POR FLUJO"
        );
        System.out.println(
                "=========================================="
        );

        System.out.printf(
                Locale.US,
                "F2 = %.8f%%%n",
                promedioF2
        );

        System.out.printf(
                Locale.US,
                "F3 = %.8f%%%n",
                promedioF3
        );

        System.out.printf(
                Locale.US,
                "F4 = %.8f%%%n",
                promedioF4
        );

        System.out.printf(
                Locale.US,
                "F5 = %.8f%%%n",
                promedioF5
        );

        System.out.printf(
                Locale.US,
                "F6 = %.8f%%%n",
                promedioF6
        );

        System.out.printf(
                Locale.US,
                "F7 = %.8f%%%n",
                promedioF7
        );

        System.out.printf(
                Locale.US,
                "F8 = %.8f%%%n",
                promedioF8
        );

        System.out.println(
                "------------------------------------------"
        );

        System.out.printf(
                Locale.US,
                "Error total = %.8f%n",
                errorTotal
        );

        System.out.printf(
                Locale.US,
                "Promedio general = %.8f%n",
                errorPromedio
        );
    }

    private static double calcularPromedio(
            List<ResultadoAjuste> resultados,
            ToDoubleFunction<ResultadoAjuste> extractor) {

        if (resultados == null
                || resultados.isEmpty()) {

            return 0.0;
        }

        return resultados.stream()
                .mapToDouble(extractor)
                .average()
                .orElse(0.0);
    }

    private static String formatearFactor(
            double valor) {

        if (!Double.isFinite(valor)) {
            return "No válido";
        }

        if (Math.abs(valor) >= 1.0e10) {
            return String.format(
                    Locale.US,
                    "%.5e",
                    valor
            );
        }

        return String.format(
                Locale.US,
                "%.6f",
                valor
        );
    }
}