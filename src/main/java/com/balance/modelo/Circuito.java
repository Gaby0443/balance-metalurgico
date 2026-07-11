package com.balance.modelo;

import java.util.List;

public class Circuito {

    private final String nombre;
    private final List<RegistroGranulometrico> registros;
    private final double tonelajeBase;

    public Circuito(
            String nombre,
            List<RegistroGranulometrico> registros,
            double tonelajeBase) {

        this.nombre = nombre;
        this.registros = registros;
        this.tonelajeBase = tonelajeBase;
    }

    public String getNombre() {
        return nombre;
    }

    public List<RegistroGranulometrico> getRegistros() {
        return registros;
    }

    public double getTonelajeBase() {
        return tonelajeBase;
    }
}