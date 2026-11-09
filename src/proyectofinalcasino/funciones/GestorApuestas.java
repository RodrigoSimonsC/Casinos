package proyectofinalcasino.funciones;

/**
 * Administra las apuestas de los jugadores de una partida.
 */
public final class GestorApuestas {

    private final int multiplo;
    private final int[] apuestas;

    public GestorApuestas(int cantidadJugadores, int multiplo) {
        this.multiplo = multiplo > 0 ? multiplo : 1;
        apuestas = new int[Math.max(0, cantidadJugadores)];
    }

    public boolean agregarFicha(int jugador, boolean partidaIniciada) {
        if (partidaIniciada || jugador < 0 || jugador >= apuestas.length) {
            return false;
        }
        apuestas[jugador] += multiplo;
        return true;
    }

    public boolean retirarFicha(int jugador, boolean partidaIniciada) {
        if (partidaIniciada || jugador < 0 || jugador >= apuestas.length
                || apuestas[jugador] < multiplo) {
            return false;
        }
        apuestas[jugador] -= multiplo;
        return true;
    }

    public boolean todosApostaron() {
        if (apuestas.length == 0) {
            return false;
        }
        for (int apuesta : apuestas) {
            if (apuesta <= 0) {
                return false;
            }
        }
        return true;
    }

    public int obtener(int jugador) {
        return jugador >= 0 && jugador < apuestas.length ? apuestas[jugador] : 0;
    }

    public int obtenerMultiplo() {
        return multiplo;
    }

    public int cantidad() {
        return apuestas.length;
    }

    public void reiniciar() {
        java.util.Arrays.fill(apuestas, 0);
    }
}
