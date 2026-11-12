package proyectofinalcasino.funciones;

/**
 * Administra las apuestas de los jugadores de una partida.
 */
public final class GestorApuestas {

    private final int multiplo;
    private final int[] apuestas;
    private final int[][] apuestasManos;

    public GestorApuestas(int cantidadJugadores, int multiplo) {
        this.multiplo = multiplo > 0 ? multiplo : 1;
        apuestas = new int[Math.max(0, cantidadJugadores)];
        apuestasManos = new int[apuestas.length][2];
    }

    public boolean agregarFicha(int jugador, boolean partidaIniciada) {
        if (partidaIniciada || jugador < 0 || jugador >= apuestas.length) {
            return false;
        }
        apuestas[jugador] += multiplo;
        apuestasManos[jugador][0] = apuestas[jugador];
        return true;
    }

    public boolean retirarFicha(int jugador, boolean partidaIniciada) {
        if (partidaIniciada || jugador < 0 || jugador >= apuestas.length
                || apuestas[jugador] < multiplo) {
            return false;
        }
        apuestas[jugador] -= multiplo;
        apuestasManos[jugador][0] = apuestas[jugador];
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

    public int obtener(int jugador, int mano) {
        if (jugador < 0 || jugador >= apuestasManos.length
                || mano < 0 || mano >= apuestasManos[jugador].length) {
            return 0;
        }
        return apuestasManos[jugador][mano];
    }

    public int obtenerTotal(int jugador) {
        if (jugador < 0 || jugador >= apuestasManos.length) {
            return 0;
        }
        return apuestasManos[jugador][0] + apuestasManos[jugador][1];
    }

    public boolean registrarSplit(int jugador) {
        if (jugador < 0 || jugador >= apuestasManos.length
                || apuestasManos[jugador][0] <= 0
                || apuestasManos[jugador][1] > 0) {
            return false;
        }
        apuestasManos[jugador][1] = apuestasManos[jugador][0];
        return true;
    }

    public boolean registrarDoble(int jugador, int mano) {
        if (jugador < 0 || jugador >= apuestasManos.length
                || mano < 0 || mano >= apuestasManos[jugador].length
                || apuestasManos[jugador][mano] <= 0) {
            return false;
        }
        apuestasManos[jugador][mano] += apuestasManos[jugador][mano];
        if (mano == 0) {
            apuestas[jugador] = apuestasManos[jugador][mano];
        }
        return true;
    }

    public int obtenerMultiplo() {
        return multiplo;
    }

    public int cantidad() {
        return apuestas.length;
    }

    public void reiniciar() {
        java.util.Arrays.fill(apuestas, 0);
        for (int[] manos : apuestasManos) {
            java.util.Arrays.fill(manos, 0);
        }
    }
}
