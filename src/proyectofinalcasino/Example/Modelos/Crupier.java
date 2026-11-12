package blackcasino.modelos;

// Modelo de ejemplo para el crupier y su mano.

import blackcasino.elementosCasino.Mano;

public final class Crupier {

    private final Mano mano = new Mano();

    public Mano obtenerMano() {
        return mano;
    }
}
