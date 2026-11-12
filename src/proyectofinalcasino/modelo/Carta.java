package proyectofinalcasino.modelo;

import java.util.Objects;

/**
 * Carta individual de una baraja de Blackjack.
 */
public final class Carta {

    private final String valor;
    private final String palo;
    private final int numeroMazo;

    public Carta(String valor, String palo, int numeroMazo) {
        this.valor = Objects.requireNonNull(valor, "El valor no puede ser null");
        this.palo = Objects.requireNonNull(palo, "El palo no puede ser null");
        if (numeroMazo < 1) {
            throw new IllegalArgumentException("El número de mazo debe ser positivo");
        }
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
        return valor + " de " + palo + " (baraja " + numeroMazo + ")";
    }
}
