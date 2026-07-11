package com.balance.modelo;

public class ResultadoTonelajes {

    private final double ms3;
    private final double ms4;
    private final double ms5;
    private final double ms6;
    private final double ms7;
    private final double ms8;

    public ResultadoTonelajes(
            double ms3,
            double ms4,
            double ms5,
            double ms6,
            double ms7,
            double ms8) {

        this.ms3 = ms3;
        this.ms4 = ms4;
        this.ms5 = ms5;
        this.ms6 = ms6;
        this.ms7 = ms7;
        this.ms8 = ms8;
    }
    public double getMs1() {
        return ms6;
    }

    public double getMs2() {
        return ms3;
    }

    public double getMs3() {
        return ms3;
    }

    public double getMs4() {
        return ms4;
    }

    public double getMs5() {
        return ms5;
    }

    public double getMs6() {
        return ms6;
    }

    public double getMs7() {
        return ms7;
    }

    public double getMs8() {
        return ms8;
    }
    public ResultadoTonelajes escalarConMs6IgualAUno() {

        if (Math.abs(ms6) < 1.0e-12) {
            throw new IllegalStateException(
                    "No se puede escalar porque MS6 es cero"
            );
        }

        double factor = 1.0 / ms6;

        return new ResultadoTonelajes(
                ms3 * factor,
                ms4 * factor,
                ms5 * factor,
                ms6 * factor,
                ms7 * factor,
                ms8 * factor
        );
    }
}