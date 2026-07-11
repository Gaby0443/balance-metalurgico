package com.balance.interfaz;

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

import javafx.application.Application;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

public class BalanceApplication extends Application {

    private Stage ventanaPrincipal;

    private final TextField campoArchivo = new TextField();
    private final TextField campoUk = new TextField("5.0");

    private final Button botonCalcular =
            new Button("Calcular balance");

    private final Button botonExportar =
            new Button("Exportar resultados");

    private final Label etiquetaEstado =
            new Label("Seleccione un archivo CSV.");

    private final Label etiquetaValidacion =
            new Label("Balance válido: —");

    private final Label etiquetaResiduo =
            new Label("Residuo máximo: —");

    private final Label etiquetaErrorTotal =
            new Label("Error total: —");

    private final Label etiquetaErrorPromedio =
            new Label("Error promedio: —");

    private final TableView<FilaTonelaje> tablaTonelajes =
            new TableView<>();

    private final TableView<ResultadoAjuste> tablaAjustes =
            new TableView<>();

    private final TableView<ResultadoAjuste> tablaErrores =
            new TableView<>();

    private final TableView<FactorPonderacion> tablaFactores =
            new TableView<>();

    private Path archivoSeleccionado;

    private ResultadoTonelajes tonelajesActuales;
    private List<ResultadoAjuste> ajustesActuales;
    private Map<String, FactorPonderacion> factoresActuales;

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        launch(args);
    }

    @Override
    public void start(Stage stage) {

        this.ventanaPrincipal = stage;

        configurarTablaTonelajes();
        configurarTablaAjustes();
        configurarTablaErrores();
        configurarTablaFactores();

        BorderPane raiz = new BorderPane();

        raiz.setTop(crearPanelEntrada());
        raiz.setCenter(crearPanelResultados());
        raiz.setBottom(crearPanelResumen());

        BorderPane.setMargin(
                raiz.getCenter(),
                new Insets(10)
        );

        Scene escena = new Scene(
                raiz,
                1300,
                760
        );

        stage.setTitle(
                "Balance Metalúrgico"
        );

        stage.setMinWidth(1050);
        stage.setMinHeight(650);
        stage.setScene(escena);
        stage.show();
    }

    private VBox crearPanelEntrada() {

        Label titulo = new Label(
                "BALANCE METALÚRGICO Y AJUSTE GRANULOMÉTRICO"
        );

        titulo.setStyle(
                "-fx-font-size: 20px;"
                        + "-fx-font-weight: bold;"
        );

        campoArchivo.setEditable(false);
        campoArchivo.setPromptText(
                "Seleccione datos_balance.csv"
        );

        Button botonSeleccionar =
                new Button("Seleccionar CSV");

        botonSeleccionar.setOnAction(
                evento -> seleccionarArchivo()
        );

        HBox filaArchivo = new HBox(
                10,
                new Label("Archivo:"),
                campoArchivo,
                botonSeleccionar
        );

        filaArchivo.setAlignment(
                Pos.CENTER_LEFT
        );

        HBox.setHgrow(
                campoArchivo,
                Priority.ALWAYS
        );

        campoUk.setMaxWidth(120);

        HBox filaParametros = new HBox(
                10,
                new Label("Uk:"),
                campoUk,
                botonCalcular,
                botonExportar
        );

        filaParametros.setAlignment(
                Pos.CENTER_LEFT
        );

        botonCalcular.setDisable(true);
        botonExportar.setDisable(true);

        botonCalcular.setOnAction(
                evento -> calcularBalance()
        );

        botonExportar.setOnAction(
                evento -> exportarResultados()
        );

        VBox panel = new VBox(
                12,
                titulo,
                filaArchivo,
                filaParametros
        );

        panel.setPadding(
                new Insets(18)
        );

        return panel;
    }

    private TabPane crearPanelResultados() {

        Tab tabTonelajes =
                new Tab(
                        "Tonelajes MS1–MS8",
                        tablaTonelajes
                );

        Tab tabAjustes =
                new Tab(
                        "Valores ajustados F1–F8",
                        tablaAjustes
                );

        Tab tabErrores =
                new Tab(
                        "Errores F2–F8",
                        tablaErrores
                );

        Tab tabFactores =
                new Tab(
                        "Factores de ponderación",
                        tablaFactores
                );

        tabTonelajes.setClosable(false);
        tabAjustes.setClosable(false);
        tabErrores.setClosable(false);
        tabFactores.setClosable(false);

        return new TabPane(
                tabTonelajes,
                tabAjustes,
                tabErrores,
                tabFactores
        );
    }

    private VBox crearPanelResumen() {

        HBox resultados = new HBox(
                30,
                etiquetaValidacion,
                etiquetaResiduo,
                etiquetaErrorTotal,
                etiquetaErrorPromedio
        );

        resultados.setAlignment(
                Pos.CENTER_LEFT
        );

        VBox panel = new VBox(
                8,
                resultados,
                etiquetaEstado
        );

        panel.setPadding(
                new Insets(15)
        );

        panel.setStyle(
                "-fx-border-color: #cccccc;"
                        + "-fx-border-width: 1 0 0 0;"
        );

        return panel;
    }

    private void seleccionarArchivo() {

        FileChooser selector =
                new FileChooser();

        selector.setTitle(
                "Seleccionar archivo de datos"
        );

        selector.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(
                        "Archivos CSV",
                        "*.csv"
                )
        );

        File archivo =
                selector.showOpenDialog(
                        ventanaPrincipal
                );

        if (archivo == null) {
            return;
        }

        archivoSeleccionado =
                archivo.toPath();

        campoArchivo.setText(
                archivo.getAbsolutePath()
        );

        botonCalcular.setDisable(false);

        etiquetaEstado.setText(
                "Archivo seleccionado: "
                        + archivo.getName()
        );
    }

    private void calcularBalance() {

        if (archivoSeleccionado == null) {
            mostrarError(
                    "Debe seleccionar un archivo CSV."
            );
            return;
        }

        final double uk;

        try {
            uk = Double.parseDouble(
                    campoUk.getText()
                            .trim()
                            .replace(",", ".")
            );

            if (!Double.isFinite(uk)
                    || uk <= 0) {

                throw new NumberFormatException();
            }

        } catch (NumberFormatException excepcion) {

            mostrarError(
                    "Uk debe ser un número mayor que cero."
            );
            return;
        }

        try {
            etiquetaEstado.setText(
                    "Calculando balance..."
            );

            ImportadorCSV importador =
                    new ImportadorCSV();

            DatosEntradaCSV datos =
                    importador.importar(
                            archivoSeleccionado
                    );

            CalculadoraFactoresPonderacion
                    calculadoraFactores =
                    new CalculadoraFactoresPonderacion(
                            uk
                    );

            factoresActuales =
                    calculadoraFactores.calcularTodos(
                            datos.getRegistros()
                    );

            CalculadoraTonelajes calculadoraTonelajes =
                    new CalculadoraTonelajes();

            /*
             * Se conserva MS3 = 1.
             * No se aplica escalamiento posterior.
             */
            tonelajesActuales =
                    calculadoraTonelajes.calcular(
                            datos.getRegistros(),
                            1.0
                    );

            CalculadoraAjuste calculadoraAjuste =
                    new CalculadoraAjuste();

            ajustesActuales =
                    calculadoraAjuste.calcularTodos(
                            datos.getRegistros(),
                            tonelajesActuales,
                            factoresActuales
                    );

            ValidadorBalance validador =
                    new ValidadorBalance(1.0e-8);

            boolean balanceValido =
                    validador.todosSonValidos(
                            ajustesActuales
                    );

            double residuoMaximo =
                    validador.calcularErrorMaximo(
                            ajustesActuales
                    );

            double errorTotal =
                    calcularErrorTotal(
                            ajustesActuales
                    );

            double errorPromedio =
                    errorTotal / 7.0;

            cargarTablas();

            etiquetaValidacion.setText(
                    "Balance válido: "
                            + (balanceValido ? "Sí" : "No")
            );

            etiquetaResiduo.setText(
                    String.format(
                            Locale.US,
                            "Residuo máximo: %.5e",
                            residuoMaximo
                    )
            );

            etiquetaErrorTotal.setText(
                    String.format(
                            Locale.US,
                            "Error total: %.8f",
                            errorTotal
                    )
            );

            etiquetaErrorPromedio.setText(
                    String.format(
                            Locale.US,
                            "Error promedio: %.8f",
                            errorPromedio
                    )
            );

            etiquetaEstado.setText(
                    "Balance calculado correctamente."
            );

            botonExportar.setDisable(false);

        } catch (IOException excepcion) {

            mostrarError(
                    "No se pudo leer el archivo:\n"
                            + excepcion.getMessage()
            );

        } catch (IllegalArgumentException
                 | IllegalStateException excepcion) {

            mostrarError(
                    "No se pudo realizar el balance:\n"
                            + excepcion.getMessage()
            );

        } catch (Exception excepcion) {

            mostrarError(
                    "Ocurrió un error inesperado:\n"
                            + excepcion.getMessage()
            );

            excepcion.printStackTrace();
        }
    }

    private void cargarTablas() {

        List<FilaTonelaje> filasTonelaje =
                new ArrayList<>();

        filasTonelaje.add(
                new FilaTonelaje(
                        "MS1",
                        tonelajesActuales.getMs1()
                )
        );

        filasTonelaje.add(
                new FilaTonelaje(
                        "MS2",
                        tonelajesActuales.getMs2()
                )
        );

        filasTonelaje.add(
                new FilaTonelaje(
                        "MS3",
                        tonelajesActuales.getMs3()
                )
        );

        filasTonelaje.add(
                new FilaTonelaje(
                        "MS4",
                        tonelajesActuales.getMs4()
                )
        );

        filasTonelaje.add(
                new FilaTonelaje(
                        "MS5",
                        tonelajesActuales.getMs5()
                )
        );

        filasTonelaje.add(
                new FilaTonelaje(
                        "MS6",
                        tonelajesActuales.getMs6()
                )
        );

        filasTonelaje.add(
                new FilaTonelaje(
                        "MS7",
                        tonelajesActuales.getMs7()
                )
        );

        filasTonelaje.add(
                new FilaTonelaje(
                        "MS8",
                        tonelajesActuales.getMs8()
                )
        );

        tablaTonelajes.setItems(
                FXCollections.observableArrayList(
                        filasTonelaje
                )
        );

        tablaAjustes.setItems(
                FXCollections.observableArrayList(
                        ajustesActuales
                )
        );

        tablaErrores.setItems(
                FXCollections.observableArrayList(
                        ajustesActuales
                )
        );

        tablaFactores.setItems(
                FXCollections.observableArrayList(
                        factoresActuales.values()
                )
        );
    }

    private void configurarTablaTonelajes() {

        TableColumn<FilaTonelaje, String>
                columnaFlujo =
                columnaTexto(
                        "Flujo",
                        FilaTonelaje::flujo,
                        160
                );

        TableColumn<FilaTonelaje, Number>
                columnaTonelaje =
                columnaNumero(
                        "Tonelaje normalizado",
                        FilaTonelaje::tonelaje,
                        220
                );

        tablaTonelajes.getColumns().addAll(
                columnaFlujo,
                columnaTonelaje
        );

        tablaTonelajes.setColumnResizePolicy(
                TableView.CONSTRAINED_RESIZE_POLICY
        );
    }

    private void configurarTablaAjustes() {

        tablaAjustes.getColumns().add(
                columnaTexto(
                        "Malla",
                        ResultadoAjuste::getMalla,
                        90
                )
        );

        tablaAjustes.getColumns().add(
                columnaNumero(
                        "F1 ajust.",
                        ResultadoAjuste::getF1Ajustado,
                        110
                )
        );

        tablaAjustes.getColumns().add(
                columnaNumero(
                        "F2 ajust.",
                        ResultadoAjuste::getF2Ajustado,
                        110
                )
        );

        tablaAjustes.getColumns().add(
                columnaNumero(
                        "F3 ajust.",
                        ResultadoAjuste::getF3Ajustado,
                        110
                )
        );

        tablaAjustes.getColumns().add(
                columnaNumero(
                        "F4 ajust.",
                        ResultadoAjuste::getF4Ajustado,
                        110
                )
        );

        tablaAjustes.getColumns().add(
                columnaNumero(
                        "F5 ajust.",
                        ResultadoAjuste::getF5Ajustado,
                        110
                )
        );

        tablaAjustes.getColumns().add(
                columnaNumero(
                        "F6 ajust.",
                        ResultadoAjuste::getF6Ajustado,
                        110
                )
        );

        tablaAjustes.getColumns().add(
                columnaNumero(
                        "F7 ajust.",
                        ResultadoAjuste::getF7Ajustado,
                        110
                )
        );

        tablaAjustes.getColumns().add(
                columnaNumero(
                        "F8 ajust.",
                        ResultadoAjuste::getF8Ajustado,
                        110
                )
        );
    }

    private void configurarTablaErrores() {

        tablaErrores.getColumns().add(
                columnaTexto(
                        "Malla",
                        ResultadoAjuste::getMalla,
                        90
                )
        );

        tablaErrores.getColumns().add(
                columnaNumero(
                        "Error F2 (%)",
                        ResultadoAjuste::getErrorF2,
                        130
                )
        );

        tablaErrores.getColumns().add(
                columnaNumero(
                        "Error F3 (%)",
                        ResultadoAjuste::getErrorF3,
                        130
                )
        );

        tablaErrores.getColumns().add(
                columnaNumero(
                        "Error F4 (%)",
                        ResultadoAjuste::getErrorF4,
                        130
                )
        );

        tablaErrores.getColumns().add(
                columnaNumero(
                        "Error F5 (%)",
                        ResultadoAjuste::getErrorF5,
                        130
                )
        );

        tablaErrores.getColumns().add(
                columnaNumero(
                        "Error F6 (%)",
                        ResultadoAjuste::getErrorF6,
                        130
                )
        );

        tablaErrores.getColumns().add(
                columnaNumero(
                        "Error F7 (%)",
                        ResultadoAjuste::getErrorF7,
                        130
                )
        );

        tablaErrores.getColumns().add(
                columnaNumero(
                        "Error F8 (%)",
                        ResultadoAjuste::getErrorF8,
                        130
                )
        );
    }

    private void configurarTablaFactores() {

        tablaFactores.getColumns().add(
                columnaTexto(
                        "Malla",
                        FactorPonderacion::getMalla,
                        90
                )
        );

        tablaFactores.getColumns().add(
                columnaNumero(
                        "Wi1",
                        FactorPonderacion::getWi1,
                        140
                )
        );

        tablaFactores.getColumns().add(
                columnaNumero(
                        "Wi2",
                        FactorPonderacion::getWi2,
                        140
                )
        );

        tablaFactores.getColumns().add(
                columnaNumero(
                        "Wi3",
                        FactorPonderacion::getWi3,
                        140
                )
        );

        tablaFactores.getColumns().add(
                columnaNumero(
                        "Wi4",
                        FactorPonderacion::getWi4,
                        140
                )
        );

        tablaFactores.getColumns().add(
                columnaNumero(
                        "Wi5",
                        FactorPonderacion::getWi5,
                        140
                )
        );

        tablaFactores.getColumns().add(
                columnaNumero(
                        "Wi6",
                        FactorPonderacion::getWi6,
                        140
                )
        );

        tablaFactores.getColumns().add(
                columnaNumero(
                        "Wi7",
                        FactorPonderacion::getWi7,
                        140
                )
        );
    }

    private void exportarResultados() {

        if (tonelajesActuales == null
                || ajustesActuales == null
                || factoresActuales == null) {

            mostrarError(
                    "Primero debe calcular el balance."
            );
            return;
        }

        DirectoryChooser selector =
                new DirectoryChooser();

        selector.setTitle(
                "Seleccionar carpeta de resultados"
        );

        File carpeta =
                selector.showDialog(
                        ventanaPrincipal
                );

        if (carpeta == null) {
            return;
        }

        try {
            Path ruta = carpeta.toPath();

            ExportadorCSV exportador =
                    new ExportadorCSV();

            exportador.exportarTonelajes(
                    ruta.resolve(
                            "tonelajes_calculados.csv"
                    ),
                    tonelajesActuales
            );

            exportador.exportarAjustes(
                    ruta.resolve(
                            "datos_ajustados.csv"
                    ),
                    ajustesActuales
            );

            exportador.exportarFactoresPonderacion(
                    ruta.resolve(
                            "factores_ponderacion.csv"
                    ),
                    factoresActuales
            );

            etiquetaEstado.setText(
                    "Resultados exportados en: "
                            + carpeta.getAbsolutePath()
            );

            Alert alerta =
                    new Alert(
                            Alert.AlertType.INFORMATION
                    );

            alerta.setTitle(
                    "Exportación completada"
            );

            alerta.setHeaderText(
                    "Archivos exportados correctamente"
            );

            alerta.setContentText(
                    carpeta.getAbsolutePath()
            );

            alerta.showAndWait();

        } catch (IOException excepcion) {

            mostrarError(
                    "No se pudieron exportar los resultados:\n"
                            + excepcion.getMessage()
            );
        }
    }

    private double calcularErrorTotal(
            List<ResultadoAjuste> resultados) {

        return promedio(
                resultados,
                ResultadoAjuste::getErrorF2
        )
                + promedio(
                resultados,
                ResultadoAjuste::getErrorF3
        )
                + promedio(
                resultados,
                ResultadoAjuste::getErrorF4
        )
                + promedio(
                resultados,
                ResultadoAjuste::getErrorF5
        )
                + promedio(
                resultados,
                ResultadoAjuste::getErrorF6
        )
                + promedio(
                resultados,
                ResultadoAjuste::getErrorF7
        )
                + promedio(
                resultados,
                ResultadoAjuste::getErrorF8
        );
    }

    private double promedio(
            List<ResultadoAjuste> resultados,
            ToDoubleFunction<ResultadoAjuste> extractor) {

        return resultados.stream()
                .mapToDouble(extractor)
                .average()
                .orElse(0.0);
    }

    private void mostrarError(
            String mensaje) {

        etiquetaEstado.setText(
                "Ocurrió un error."
        );

        Alert alerta =
                new Alert(
                        Alert.AlertType.ERROR
                );

        alerta.setTitle(
                "Error"
        );

        alerta.setHeaderText(
                "No se pudo completar la operación"
        );

        alerta.setContentText(
                mensaje
        );

        alerta.showAndWait();
    }

    private static <T>
    TableColumn<T, String> columnaTexto(
            String titulo,
            Function<T, String> extractor,
            double ancho) {

        TableColumn<T, String> columna =
                new TableColumn<>(titulo);

        columna.setCellValueFactory(
                datos ->
                        new ReadOnlyStringWrapper(
                                extractor.apply(
                                        datos.getValue()
                                )
                        )
        );

        columna.setPrefWidth(ancho);

        return columna;
    }

    private static <T>
    TableColumn<T, Number> columnaNumero(
            String titulo,
            Function<T, Double> extractor,
            double ancho) {

        TableColumn<T, Number> columna =
                new TableColumn<>(titulo);

        columna.setCellValueFactory(
                datos ->
                        new ReadOnlyDoubleWrapper(
                                extractor.apply(
                                        datos.getValue()
                                )
                        )
        );

        columna.setPrefWidth(ancho);

        return columna;
    }

    private record FilaTonelaje(
            String flujo,
            double tonelaje) {
    }
}