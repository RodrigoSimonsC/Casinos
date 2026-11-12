package proyectofinalcasino.logica;

import java.util.List;
import java.util.Objects;
import proyectofinalcasino.modelo.Jugador;
import proyectofinalcasino.modelo.Mano;

/**
 * Mantiene el jugador y la mano que tienen el turno activo.
 */
public final class ControlTurnos {

    private final List<Jugador> jugadores;
    private int indiceJugador;
    private int indiceMano;

    public ControlTurnos(List<Jugador> jugadores) {
        this.jugadores = List.copyOf(Objects.requireNonNull(jugadores,
                "Los jugadores no pueden ser null"));
    }

    public Jugador obtenerJugadorActivo() {
        validarJugadores();
        return jugadores.get(indiceJugador);
    }

    public Mano obtenerManoActiva() {
        return obtenerJugadorActivo().obtenerManos().get(indiceMano);
    }

    public void avanzar() {
        validarJugadores();
        if (indiceMano + 1 < jugadores.get(indiceJugador).obtenerManos().size()) {
            indiceMano++;
            return;
        }
        if (indiceJugador + 1 < jugadores.size()) {
            indiceJugador++;
            indiceMano = 0;
        }
    }

    public void reiniciar() {
        indiceJugador = 0;
        indiceMano = 0;
    }

    private void validarJugadores() {
        if (jugadores.isEmpty()) {
            throw new IllegalStateException("No hay jugadores en la mesa");
        }
    }
}
