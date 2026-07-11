package com.balance.util;

import com.balance.modelo.ResultadoBalance;
import com.balance.modelo.ResultadoTonelajes;

public class FormateadorResultados {

    public void imprimir(ResultadoBalance resultado) {

        ResultadoTonelajes t = resultado.getTonelajes();

        System.out.println("===== TONELAJES =====");
        System.out.printf("MS3: %.8f%n", t.getMs3());
        System.out.printf("MS4: %.8f%n", t.getMs4());
        System.out.printf("MS5: %.8f%n", t.getMs5());
        System.out.printf("MS6: %.8f%n", t.getMs6());
        System.out.printf("MS7: %.8f%n", t.getMs7());
        System.out.printf("MS8: %.8f%n", t.getMs8());

        System.out.println();
        System.out.println("Balance válido: "
                + resultado.isBalanceValido());

        System.out.printf(
                "Error máximo: %.12f%n",
                resultado.getErrorMaximo()
        );
    }
}