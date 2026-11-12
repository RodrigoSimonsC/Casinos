package proyectofinalcasino.logica;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import proyectofinalcasino.funciones.GestorCartas;
import proyectofinalcasino.modelo.Jugador;

/**
 * Coordina el estado de una ronda sin depender de Swing ni de la persistencia.
 */
public final class ControlPartida {

    private final PartidaMotor motor;
    private final int cantidadJugadores;
    private final boolean[] jugadoresFinalizados;
    private final boolean[] manosFinalizadas = new boolean[2];
    private final boolean[] manosDobladas = new boolean[2];
    private boolean iniciada;
    private boolean repartoFinalizado;
    private boolean turnoDealer;
    private boolean turnoFinalizado;
    private boolean dealerRevelado;
    private boolean separado;
    private int jugadorSeparado = -1;
    private int jugadorActivo;
    private int manoActiva;
    private String resultado = "";
    private final String[] resultadosJugadores;
    private final String[][] resultadosManos;

    public ControlPartida(List<Jugador> jugadores, int multiploApuesta) {
        Objects.requireNonNull(jugadores, "Los jugadores no pueden ser null");
        cantidadJugadores = jugadores.size();
        motor = new PartidaMotor(cantidadJugadores, multiploApuesta);
        jugadoresFinalizados = new boolean[cantidadJugadores];
        resultadosJugadores = new String[cantidadJugadores];
        resultadosManos = new String[cantidadJugadores][2];
    }

    public ControlPartida(List<Jugador> jugadores, PartidaMotor motor) {
        Objects.requireNonNull(jugadores, "Los jugadores no pueden ser null");
        this.motor = Objects.requireNonNull(motor, "El motor no puede ser null");
        cantidadJugadores = jugadores.size();
        jugadoresFinalizados = new boolean[cantidadJugadores];
        resultadosJugadores = new String[cantidadJugadores];
        resultadosManos = new String[cantidadJugadores][2];
    }

    public boolean puedeComenzar() {
        return !iniciada && cantidadJugadores > 0 && motor.puedeComenzar();
    }

    public boolean iniciar() {
        if (!puedeComenzar() || !motor.iniciar()) {
            return false;
        }
        iniciada = true;
        jugadorActivo = 0;
        manoActiva = 0;
        Arrays.fill(jugadoresFinalizados, false);
        return true;
    }

    public boolean estaIniciada() {
        return iniciada;
    }

    public boolean estaFinalizadoElReparto() {
        return repartoFinalizado;
    }

    public void marcarRepartoFinalizado() {
        repartoFinalizado = true;
    }

    public boolean esTurnoDealer() {
        return turnoDealer;
    }

    public boolean esTurnoFinalizado() {
        return turnoFinalizado;
    }

    public boolean dealerRevelado() {
        return dealerRevelado;
    }

    public void iniciarTurnoDealer() {
        dealerRevelado = true;
        turnoDealer = true;
        turnoFinalizado = true;
    }

    public int jugadorActivo() {
        return jugadorActivo;
    }

    public int manoActiva() {
        return manoActiva;
    }

    public void seleccionarJugador(int indice) {
        if (indice < 0 || indice >= cantidadJugadores
                || iniciada && jugadoresFinalizados[indice]) {
            return;
        }
        motor.seleccionarJugador(indice);
        jugadorActivo = indice;
        manoActiva = 0;
    }

    public void seleccionarMano(int mano) {
        if (mano >= 0 && mano < 2) {
            manoActiva = mano;
            motor.seleccionarMano(mano);
        }
    }

    public boolean puedePedir(int cartas, boolean blackjack, boolean reventada,
            int indiceBaraja, int totalCartas) {
        return iniciada && repartoFinalizado && !turnoFinalizado
                && !jugadoresFinalizados[jugadorActivo]
                && !manosFinalizadas[manoActiva] && !blackjack && !reventada
                && indiceBaraja < totalCartas;
    }

    public boolean puedePlantarse() {
        return iniciada && repartoFinalizado && !turnoFinalizado
                && !jugadoresFinalizados[jugadorActivo]
                && !manosFinalizadas[manoActiva];
    }

    public boolean puedeDoblar(int cantidadCartas, int indiceBaraja,
            int totalCartas) {
        return iniciada && repartoFinalizado && !turnoFinalizado
                && !jugadoresFinalizados[jugadorActivo]
                && !manosDobladas[manoActiva] && cantidadCartas == 2
                && indiceBaraja < totalCartas;
    }

    public boolean puedeSeparar(boolean cartasSeparables, int indiceBaraja,
            int totalCartas) {
        return iniciada && repartoFinalizado && !turnoFinalizado && !separado
                && cartasSeparables && indiceBaraja + 1 < totalCartas;
    }

    public void marcarDoblada() {
        manosDobladas[manoActiva] = true;
        manosFinalizadas[manoActiva] = true;
    }

    public void marcarSeparada(int jugador) {
        separado = true;
        jugadorSeparado = jugador;
    }

    public boolean tieneManosSeparadas(int jugador) {
        return separado && jugador == jugadorSeparado;
    }

    public void terminarMano() {
        manosFinalizadas[manoActiva] = true;
        if (separado && manoActiva == 0) {
            seleccionarMano(1);
            manosFinalizadas[1] = false;
            return;
        }
        jugadoresFinalizados[jugadorActivo] = true;
        int siguiente = siguienteJugador();
        if (siguiente >= 0) {
            seleccionarJugador(siguiente);
            manosFinalizadas[0] = false;
            manosFinalizadas[1] = false;
            manosDobladas[0] = false;
            manosDobladas[1] = false;
            return;
        }
        turnoFinalizado = true;
        iniciarTurnoDealer();
    }

    public boolean jugadorFinalizado(int jugador) {
        return jugador < 0 || jugador >= jugadoresFinalizados.length
                || jugadoresFinalizados[jugador];
    }

    public boolean manoFinalizada(int mano) {
        return mano < 0 || mano >= manosFinalizadas.length
                || manosFinalizadas[mano];
    }

    private int siguienteJugador() {
        for (int indice = jugadorActivo + 1; indice < cantidadJugadores;
                indice++) {
            if (!jugadoresFinalizados[indice]) {
                return indice;
            }
        }
        for (int indice = 0; indice < jugadorActivo; indice++) {
            if (!jugadoresFinalizados[indice]) {
                return indice;
            }
        }
        return -1;
    }

    public void reiniciar() {
        motor.reiniciar();
        iniciada = false;
        repartoFinalizado = false;
        turnoDealer = false;
        turnoFinalizado = false;
        dealerRevelado = false;
        separado = false;
        jugadorSeparado = -1;
        jugadorActivo = 0;
        manoActiva = 0;
        Arrays.fill(jugadoresFinalizados, false);
        Arrays.fill(manosFinalizadas, false);
        Arrays.fill(manosDobladas, false);
        Arrays.fill(resultadosJugadores, "");
        for (String[] manos : resultadosManos) {
            Arrays.fill(manos, "");
        }
        resultado = "";
    }

    public PartidaMotor motor() {
        return motor;
    }

    public boolean apostar() {
        return !iniciada && motor.apostar();
    }

    public boolean retirarApuesta() {
        return !iniciada && motor.retirarApuesta();
    }

    public boolean registrarApuestaSeparada() {
        return motor.registrarApuestaSeparada();
    }

    public boolean registrarApuestaDoblada() {
        return motor.registrarApuestaDoblada();
    }

    public int obtenerApuesta(int jugador) {
        return motor.obtenerApuesta(jugador);
    }

    public int obtenerApuesta(int jugador, int mano) {
        return motor.obtenerApuesta(jugador, mano);
    }

    public int obtenerApuestaTotal(int jugador) {
        return motor.obtenerApuestaTotal(jugador);
    }

    public int obtenerMultiploApuesta() {
        return motor.obtenerMultiploApuesta();
    }

    public int cantidadJugadores() {
        return cantidadJugadores;
    }

    public void registrarResultados(List<List<List<String>>> cartasJugadores,
            List<String> cartasDealer, GestorCartas reglas) {
        Objects.requireNonNull(cartasJugadores,
                "Las cartas de jugadores no pueden ser null");
        Objects.requireNonNull(cartasDealer,
                "Las cartas del dealer no pueden ser null");
        Objects.requireNonNull(reglas, "Las reglas no pueden ser null");
        for (int jugador = 0; jugador < cantidadJugadores; jugador++) {
            List<List<String>> manosJugador = cartasJugadores.get(jugador);
            List<String> manoPrincipal = manosJugador.get(0);
            resultadosManos[jugador][0] = reglas.resolver(manoPrincipal,
                    cartasDealer);
            resultadosManos[jugador][1] = "";
            if (jugador == jugadorSeparado && separado
                    && manosJugador.size() > 1) {
                resultadosManos[jugador][1] = reglas.resolver(
                        manosJugador.get(1), cartasDealer);
            }
            resultadosJugadores[jugador] = resultadosManos[jugador][0];
        }
        resultado = resultadosJugadores.length == 0 ? ""
                : resultadosJugadores[0];
        turnoDealer = false;
        turnoFinalizado = true;
    }

    public String resultado() {
        return resultado;
    }

    public String resultado(int jugador) {
        if (jugador < 0 || jugador >= resultadosJugadores.length) {
            return "";
        }
        return resultadosJugadores[jugador] == null
                ? "" : resultadosJugadores[jugador];
    }

    public String resultado(int jugador, int mano) {
        if (jugador < 0 || jugador >= resultadosManos.length
                || mano < 0 || mano >= resultadosManos[jugador].length) {
            return "";
        }
        return resultadosManos[jugador][mano] == null
                ? "" : resultadosManos[jugador][mano];
    }

    public double multiplicadorResultado(int jugador, int mano) {
        String resultadoMano = resultado(jugador, mano);
        if (resultadoMano.contains("Empate")) {
            return 1.0;
        }
        if (resultadoMano.contains("Blackjack")) {
            return 2.5;
        }
        return resultadoMano.contains("Ganaste") ? 2.0 : 0.0;
    }

    public String[] resultados() {
        return resultadosJugadores.clone();
    }
}
