package proyectofinalcasino.modelo;

/**
 * Representa al crupier y su mano actual.
 */
public final class Crupier {

    private final Mano mano = new Mano();

    public Mano obtenerMano() {
        return mano;
    }
}
