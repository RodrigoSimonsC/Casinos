package proyectofinalcasino.logica;

import java.util.List;
import java.util.Objects;
import proyectofinalcasino.elementosCasino.Baraja;
import proyectofinalcasino.modelo.Carta;
import proyectofinalcasino.modelo.Crupier;
import proyectofinalcasino.modelo.Jugador;
import proyectofinalcasino.modelo.Mano;

/**
 * Motor de acciones de Blackjack, sin dependencias de Swing.
 */
public final class AccionesDeJuego {

    private final Baraja baraja;
    private final ReglasBlackjack reglas = new ReglasBlackjack();
    private final int maximoSplits;
    private int posicionSiguienteCarta;
    private int splitsRealizados;

    public AccionesDeJuego(Baraja baraja, int maximoSplits) {
        this.baraja = Objects.requireNonNull(baraja, "La baraja es obligatoria");
        this.maximoSplits = Math.max(0, maximoSplits);
    }

    public void repartirInicial(List<Jugador> jugadores, Crupier crupier) {
        validarJugadores(jugadores);
        for (int vuelta = 0; vuelta < 2; vuelta++) {
            for (Jugador jugador : jugadores) {
                jugador.obtenerMano().agregarCarta(siguienteCarta());
            }
            if (crupier != null) {
                crupier.obtenerMano().agregarCarta(siguienteCarta());
            }
        }
    }

    public Carta pedirCarta(Jugador jugador, int mano) {
        Mano destino = obtenerMano(jugador, mano);
        if (destino.estaPlantada()) {
            throw new IllegalStateException("La mano está plantada");
        }
        Carta carta = siguienteCarta();
        destino.agregarCarta(carta);
        return carta;
    }

    public Carta pedirCarta(Crupier crupier) {
        Objects.requireNonNull(crupier, "El crupier es obligatorio");
        return siguienteCartaYAgregar(crupier.obtenerMano());
    }

    public void plantarse(Jugador jugador, int mano) {
        obtenerMano(jugador, mano).plantarse();
    }

    public void plantarCrupier(Crupier crupier) {
        Objects.requireNonNull(crupier, "El crupier es obligatorio")
                .obtenerMano().plantarse();
    }

    public Mano dividir(Jugador jugador, int mano) {
        if (splitsRealizados >= maximoSplits) {
            throw new IllegalStateException("Se alcanzó el máximo de splits");
        }
        Mano original = obtenerMano(jugador, mano);
        if (!reglas.puedeDividir(original) || original.estaPlantada()) {
            throw new IllegalStateException("La mano no se puede dividir");
        }
        Carta segunda = original.retirarUltimaCarta();
        Mano nueva = new Mano();
        nueva.agregarCarta(segunda);
        jugador.agregarMano(nueva);
        original.agregarCarta(siguienteCarta());
        nueva.agregarCarta(siguienteCarta());
        splitsRealizados++;
        return nueva;
    }

    public void jugarDealer(Crupier crupier) {
        Objects.requireNonNull(crupier, "El crupier es obligatorio");
        while (reglas.calcularValor(crupier.obtenerMano()) < 17
                && !reglas.estaReventada(crupier.obtenerMano())) {
            pedirCarta(crupier);
        }
        if (!reglas.estaReventada(crupier.obtenerMano())) {
            plantarCrupier(crupier);
        }
    }

    public void reiniciar() {
        posicionSiguienteCarta = 0;
        splitsRealizados = 0;
        baraja.reiniciar();
    }

    private Carta siguienteCartaYAgregar(Mano mano) {
        Carta carta = siguienteCarta();
        mano.agregarCarta(carta);
        return carta;
    }

    private Carta siguienteCarta() {
        List<Carta> cartas = baraja.obtenerCartas();
        if (posicionSiguienteCarta >= cartas.size()) {
            throw new IllegalStateException("No quedan cartas en la baraja");
        }
        return cartas.get(posicionSiguienteCarta++);
    }

    private Mano obtenerMano(Jugador jugador, int mano) {
        Objects.requireNonNull(jugador, "El jugador es obligatorio");
        if (mano < 0 || mano >= jugador.obtenerManos().size()) {
            throw new IndexOutOfBoundsException("La mano no existe");
        }
        return jugador.obtenerManos().get(mano);
    }

    private void validarJugadores(List<Jugador> jugadores) {
        if (jugadores == null || jugadores.isEmpty()) {
            throw new IllegalArgumentException("Debe existir al menos un jugador");
        }
    }
}
