package blackcasino.modelos;

// Modelo de ejemplo que representa una carta de Blackjack.

import java.util.Objects;

public final class Carta {

    private final String valor;
    private final String palo;
    private final int numeroMazo;

    public Carta(String valor, String palo, int numeroMazo) {
        this.valor = Objects.requireNonNull(valor, "El valor no puede ser null");
        this.palo = Objects.requireNonNull(palo, "El palo no puede ser null");
        this.numeroMazo = numeroMazo;
    }

    public String obtenerValor() {
        return valor;
    }

    public String obtenerPalo() {
        return palo;
    }

    public int obtenerNumeroMazo() {
        return numeroMazo;
    }

    @Override
    public String toString() {
        return valor + " de " + palo + "[" + numeroMazo + "]";
    }
}
