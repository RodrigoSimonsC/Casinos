package proyectofinalcasino.logica;

import java.util.List;
import java.util.Objects;
import proyectofinalcasino.elementosCasino.Baraja;
import proyectofinalcasino.modelo.Crupier;
import proyectofinalcasino.modelo.Jugador;

/**
 * Fachada del motor de Blackjack para la capa de aplicación.
 */
public final class Mesa {

    private final Baraja baraja;
    private final List<Jugador> jugadores;
    private final Crupier crupier;
    private final AccionesDeJuego acciones;
    private final ControlTurnos turnos;

    public Mesa(int cantidadMazos, List<Jugador> jugadores, int maximoSplits) {
        this.baraja = new Baraja(cantidadMazos);
        this.jugadores = List.copyOf(Objects.requireNonNull(jugadores,
                "Los jugadores no pueden ser null"));
        this.crupier = new Crupier();
        this.acciones = new AccionesDeJuego(baraja, maximoSplits);
        this.turnos = new ControlTurnos(this.jugadores);
    }

    public void repartirInicial() {
        acciones.repartirInicial(jugadores, crupier);
    }

    public AccionesDeJuego obtenerAcciones() {
        return acciones;
    }

    public ControlTurnos obtenerTurnos() {
        return turnos;
    }

    public List<Jugador> obtenerJugadores() {
        return jugadores;
    }

    public Crupier obtenerCrupier() {
        return crupier;
    }

    public Baraja obtenerBaraja() {
        return baraja;
    }
}
