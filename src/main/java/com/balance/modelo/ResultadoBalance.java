package com.balance.modelo;

import java.util.List;

public class ResultadoBalance {

    private final ResultadoTonelajes tonelajes;
    private final List<ResultadoAjuste> ajustes;
    private final boolean balanceValido;
    private final double errorMaximo;

    public ResultadoBalance(
            ResultadoTonelajes tonelajes,
            List<ResultadoAjuste> ajustes,
            boolean balanceValido,
            double errorMaximo) {

        this.tonelajes = tonelajes;
        this.ajustes = ajustes;
        this.balanceValido = balanceValido;
        this.errorMaximo = errorMaximo;
    }

    public ResultadoTonelajes getTonelajes() {
        return tonelajes;
    }

    public List<ResultadoAjuste> getAjustes() {
        return ajustes;
    }

    public boolean isBalanceValido() {
        return balanceValido;
    }

    public double getErrorMaximo() {
        return errorMaximo;
    }
}