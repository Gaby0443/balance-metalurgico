package com.balance.modelo;

import java.util.List;

public class DatosEntradaCSV {

    private final List<RegistroGranulometrico> registros;

    public DatosEntradaCSV(
            List<RegistroGranulometrico> registros) {

        if (registros == null || registros.isEmpty()) {
            throw new IllegalArgumentException(
                    "La lista de registros no puede estar vacía"
            );
        }

        this.registros = List.copyOf(registros);
    }

    public List<RegistroGranulometrico> getRegistros() {
        return registros;
    }
}