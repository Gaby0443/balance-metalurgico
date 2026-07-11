package com.balance.excepcion;

public class BalanceInconsistenteException extends RuntimeException {

    public BalanceInconsistenteException(String mensaje) {
        super(mensaje);
    }
}