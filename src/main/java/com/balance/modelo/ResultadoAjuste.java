package com.balance.modelo;

public class ResultadoAjuste {

    private static final double EPSILON = 1.0e-12;

    private final String malla;
    private final double abertura;

    private final double f1Medido;
    private final double f2Medido;
    private final double f3Medido;
    private final double f4Medido;
    private final double f5Medido;
    private final double f6Medido;
    private final double f7Medido;
    private final double f8Medido;

    private final double f1Ajustado;
    private final double f2Ajustado;
    private final double f3Ajustado;
    private final double f4Ajustado;
    private final double f5Ajustado;
    private final double f6Ajustado;
    private final double f7Ajustado;
    private final double f8Ajustado;

    private final double lambda1;
    private final double lambda2;

    private final double residuoNodo1;
    private final double residuoNodo2;

    public ResultadoAjuste(
            String malla,
            double abertura,

            double f1Medido,
            double f2Medido,
            double f3Medido,
            double f4Medido,
            double f5Medido,
            double f6Medido,
            double f7Medido,
            double f8Medido,

            double f1Ajustado,
            double f2Ajustado,
            double f3Ajustado,
            double f4Ajustado,
            double f5Ajustado,
            double f6Ajustado,
            double f7Ajustado,
            double f8Ajustado,

            double lambda1,
            double lambda2,

            double residuoNodo1,
            double residuoNodo2) {

        this.malla = malla;
        this.abertura = abertura;

        this.f1Medido = f1Medido;
        this.f2Medido = f2Medido;
        this.f3Medido = f3Medido;
        this.f4Medido = f4Medido;
        this.f5Medido = f5Medido;
        this.f6Medido = f6Medido;
        this.f7Medido = f7Medido;
        this.f8Medido = f8Medido;

        this.f1Ajustado = f1Ajustado;
        this.f2Ajustado = f2Ajustado;
        this.f3Ajustado = f3Ajustado;
        this.f4Ajustado = f4Ajustado;
        this.f5Ajustado = f5Ajustado;
        this.f6Ajustado = f6Ajustado;
        this.f7Ajustado = f7Ajustado;
        this.f8Ajustado = f8Ajustado;

        this.lambda1 = lambda1;
        this.lambda2 = lambda2;

        this.residuoNodo1 = residuoNodo1;
        this.residuoNodo2 = residuoNodo2;
    }

    public String getMalla() {
        return malla;
    }

    public double getAbertura() {
        return abertura;
    }

    public double getF1Medido() {
        return f1Medido;
    }

    public double getF2Medido() {
        return f2Medido;
    }

    public double getF3Medido() {
        return f3Medido;
    }

    public double getF4Medido() {
        return f4Medido;
    }

    public double getF5Medido() {
        return f5Medido;
    }

    public double getF6Medido() {
        return f6Medido;
    }

    public double getF7Medido() {
        return f7Medido;
    }

    public double getF8Medido() {
        return f8Medido;
    }

    public double getF1Ajustado() {
        return f1Ajustado;
    }

    public double getF2Ajustado() {
        return f2Ajustado;
    }

    public double getF3Ajustado() {
        return f3Ajustado;
    }

    public double getF4Ajustado() {
        return f4Ajustado;
    }

    public double getF5Ajustado() {
        return f5Ajustado;
    }

    public double getF6Ajustado() {
        return f6Ajustado;
    }

    public double getF7Ajustado() {
        return f7Ajustado;
    }

    public double getF8Ajustado() {
        return f8Ajustado;
    }

    public double getLambda1() {
        return lambda1;
    }

    public double getLambda2() {
        return lambda2;
    }

    public double getResiduoNodo1() {
        return residuoNodo1;
    }

    public double getResiduoNodo2() {
        return residuoNodo2;
    }

    /*
     * Error relativo porcentual:
     *
     * |ajustado - medido| / |medido| × 100
     */

    private double calcularErrorRelativoPorcentual(
            double medido,
            double ajustado) {

        if (Math.abs(medido) <= EPSILON) {
            return 0.0;
        }

        return Math.abs(
                (ajustado - medido) / medido
        ) * 100.0;
    }

    public double getErrorF1() {
        return calcularErrorRelativoPorcentual(
                f1Medido,
                f1Ajustado
        );
    }

    public double getErrorF2() {
        return calcularErrorRelativoPorcentual(
                f2Medido,
                f2Ajustado
        );
    }

    public double getErrorF3() {
        return calcularErrorRelativoPorcentual(
                f3Medido,
                f3Ajustado
        );
    }

    public double getErrorF4() {
        return calcularErrorRelativoPorcentual(
                f4Medido,
                f4Ajustado
        );
    }

    public double getErrorF5() {
        return calcularErrorRelativoPorcentual(
                f5Medido,
                f5Ajustado
        );
    }

    public double getErrorF6() {
        return calcularErrorRelativoPorcentual(
                f6Medido,
                f6Ajustado
        );
    }

    public double getErrorF7() {
        return calcularErrorRelativoPorcentual(
                f7Medido,
                f7Ajustado
        );
    }

    public double getErrorF8() {
        return calcularErrorRelativoPorcentual(
                f8Medido,
                f8Ajustado
        );
    }

    /*
     * Para reproducir la tabla del manual,
     * el error total considera F2 hasta F8.
     * F1 no se incluye.
     */

    public double getErrorTotalFila() {
        return getErrorF2()
                + getErrorF3()
                + getErrorF4()
                + getErrorF5()
                + getErrorF6()
                + getErrorF7()
                + getErrorF8();
    }

    public double getErrorPromedioFila() {
        return getErrorTotalFila() / 7.0;
    }
}