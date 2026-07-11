package com.balance.servicio;

import com.balance.modelo.FactorPonderacion;
import com.balance.modelo.RegistroGranulometrico;
import com.balance.modelo.ResultadoAjuste;
import com.balance.modelo.ResultadoBalance;
import com.balance.modelo.ResultadoTonelajes;

import java.util.List;
import java.util.Map;

public class BalanceMetalurgicoService {

    private final CalculadoraTonelajes calculadoraTonelajes;
    private final CalculadoraAjuste calculadoraAjuste;
    private final ValidadorBalance validadorBalance;

    public BalanceMetalurgicoService() {
        this.calculadoraTonelajes = new CalculadoraTonelajes();
        this.calculadoraAjuste = new CalculadoraAjuste();
        this.validadorBalance = new ValidadorBalance();
    }

    public ResultadoBalance ejecutar(
            List<RegistroGranulometrico> registros,
            double ms3Base,
            Map<String, FactorPonderacion> factoresPorMalla) {

        ResultadoTonelajes tonelajes =
                calculadoraTonelajes.calcular(
                        registros,
                        ms3Base
                );

        List<ResultadoAjuste> ajustes =
                calculadoraAjuste.calcularTodos(
                        registros,
                        tonelajes,
                        factoresPorMalla
                );

        boolean valido =
                validadorBalance.todosSonValidos(ajustes);

        double errorMaximo =
                validadorBalance.calcularErrorMaximo(ajustes);

        return new ResultadoBalance(
                tonelajes,
                ajustes,
                valido,
                errorMaximo
        );
    }
}