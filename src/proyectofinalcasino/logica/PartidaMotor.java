package proyectofinalcasino.logica;

import proyectofinalcasino.funciones.GestorApuestas;

/**
 * Estado y reglas de interacción de una ronda de Blackjack.
 *
 * <p>La clase no conoce Swing ni la persistencia. La vista sólo consulta este
 * motor y le comunica las acciones del jugador; el componente de animación se
 * ocupa exclusivamente de representar y repartir las cartas.</p>
 */
public final class PartidaMotor {

    private final GestorApuestas apuestas;
    private boolean iniciada;
    private int jugadorActivo;
    private int manoActiva;

    public PartidaMotor(int cantidadJugadores, int multiploApuesta) {
        apuestas = new GestorApuestas(cantidadJugadores, multiploApuesta);
    }

    public boolean estaIniciada() {
        return iniciada;
    }

    public boolean puedeComenzar() {
        return !iniciada && apuestas.todosApostaron();
    }

    public boolean iniciar() {
        if (!puedeComenzar()) {
            return false;
        }
        iniciada = true;
        jugadorActivo = 0;
        manoActiva = 0;
        return true;
    }

    public boolean apostar() {
        return !iniciada && apuestas.agregarFicha(jugadorActivo, false);
    }

    public boolean retirarApuesta() {
        return !iniciada && apuestas.retirarFicha(jugadorActivo, false);
    }

    public void seleccionarJugador(int indice) {
        if (indice >= 0 && indice < apuestas.cantidad()) {
            jugadorActivo = indice;
            manoActiva = 0;
        }
    }

    public int obtenerJugadorActivo() {
        return jugadorActivo;
    }

    public int obtenerManoActiva() {
        return manoActiva;
    }

    public int obtenerApuestaActual() {
        return apuestas.obtener(jugadorActivo);
    }

    public int obtenerApuesta(int jugador) {
        return apuestas.obtener(jugador);
    }

    public int obtenerApuesta(int jugador, int mano) {
        return apuestas.obtener(jugador, mano);
    }

    public int obtener(int jugador) {
        return obtenerApuesta(jugador);
    }

    public int obtener(int jugador, int mano) {
        return obtenerApuesta(jugador, mano);
    }

    public int cantidad() {
        return cantidadJugadores();
    }

    public boolean registrarApuestaSeparada() {
        return apuestas.registrarSplit(jugadorActivo);
    }

    public boolean registrarApuestaDoblada() {
        return apuestas.registrarDoble(jugadorActivo, manoActiva);
    }

    public int obtenerApuestaTotal(int jugador) {
        return apuestas.obtenerTotal(jugador);
    }

    public int obtenerTotal(int jugador) {
        return obtenerApuestaTotal(jugador);
    }

    public int obtenerMultiploApuesta() {
        return apuestas.obtenerMultiplo();
    }

    public void avanzarMano() {
        manoActiva = Math.min(1, manoActiva + 1);
    }

    public void seleccionarMano(int mano) {
        if (mano >= 0 && mano < 2) {
            manoActiva = mano;
        }
    }

    public void reiniciar() {
        iniciada = false;
        jugadorActivo = 0;
        manoActiva = 0;
        apuestas.reiniciar();
    }

    public int cantidadJugadores() {
        return apuestas.cantidad();
    }
}
