package proyectofinalcasino.interfaz;

import java.util.List;
import javax.swing.JPanel;
import proyectofinalcasino.funciones.BarajaBlackjack;
import proyectofinalcasino.funciones.GestorMesa;
import proyectofinalcasino.funciones.PartidaEventoListener;
import proyectofinalcasino.modelo.Jugador;

/**
 * Fachada visual de la partida.
 *
 * <p>La implementación de cartas, temporizadores y pintura vive en
 * {@link PanelAnimacionMesa}. Esta clase conserva el contrato utilizado por
 * las ventanas, pero no contiene reglas ni estado de juego.</p>
 */
public final class AnimacionReparto extends JPanel {

    private final PanelAnimacionMesa panel;

    public AnimacionReparto(List<Jugador> jugadores, BarajaBlackjack baraja,
            int multiploApuesta) {
        this(jugadores, baraja, multiploApuesta, null);
    }

    public AnimacionReparto(List<Jugador> jugadores, BarajaBlackjack baraja,
            int multiploApuesta, GestorMesa gestorMesa) {
        panel = new PanelAnimacionMesa(jugadores, baraja, multiploApuesta,
                gestorMesa);
        setOpaque(false);
        setLayout(new java.awt.BorderLayout());
        add(panel, java.awt.BorderLayout.CENTER);
    }

    public boolean isPartidaIniciada() {
        return panel.isPartidaIniciada();
    }

    public void establecerEventos(PartidaEventoListener eventos) {
        panel.establecerEventos(eventos);
    }

    public boolean puedeComenzarPartida() {
        return panel.puedeComenzarPartida();
    }

    public boolean iniciarPartida() {
        return panel.iniciarPartida();
    }

    public boolean apostar() {
        return panel.apostar();
    }

    public boolean retirarApuesta() {
        return panel.retirarApuesta();
    }

    public int obtenerApuestaActual() {
        return panel.obtenerApuestaActual();
    }

    public void seleccionarJugador(int indice) {
        panel.seleccionarJugador(indice);
    }

    public int obtenerJugadorActivo() {
        return panel.obtenerJugadorActivo();
    }

    public int obtenerManoActiva() {
        return panel.obtenerManoActiva();
    }

    public int cantidadManos(int jugador) {
        return panel.cantidadManos(jugador);
    }

    public boolean registrarApuestaSeparada() {
        return panel.registrarApuestaSeparada();
    }

    public boolean registrarApuestaDoblada() {
        return panel.registrarApuestaDoblada();
    }

    public void actualizarFichaApuesta(int jugador) {
        panel.actualizarFichaApuesta(jugador);
    }

    public String obtenerTextoTurno() {
        return panel.obtenerTextoTurno();
    }

    public int obtenerApuesta(int jugador) {
        return panel.obtenerApuesta(jugador);
    }

    public int obtenerApuesta(int jugador, int mano) {
        return panel.obtenerApuesta(jugador, mano);
    }

    public boolean puedePedirCarta() {
        return panel.puedePedirCarta();
    }

    public boolean pedirCarta() {
        return panel.pedirCarta();
    }

    public boolean plantarse() {
        return panel.plantarse();
    }

    public boolean puedePlantarse() {
        return panel.puedePlantarse();
    }

    public boolean puedePedirDealer() {
        return panel.puedePedirDealer();
    }

    public boolean pedirCartaDealer() {
        return panel.pedirCartaDealer();
    }

    public String obtenerResultado() {
        return panel.obtenerResultado();
    }

    public String obtenerResultado(int jugador) {
        return panel.obtenerResultado(jugador);
    }

    public String obtenerResumenManos() {
        return panel.obtenerResumenManos();
    }

    public boolean resultadoListo() {
        return panel.resultadoListo();
    }

    public double multiplicadorResultado() {
        return panel.multiplicadorResultado();
    }

    public double multiplicadorResultado(int jugador) {
        return panel.multiplicadorResultado(jugador);
    }

    public double multiplicadorResultado(int jugador, int mano) {
        return panel.multiplicadorResultado(jugador, mano);
    }

    public String obtenerResultado(int jugador, int mano) {
        return panel.obtenerResultado(jugador, mano);
    }

    public boolean puedeDoblar() {
        return panel.puedeDoblar();
    }

    public boolean doblarApuesta() {
        return panel.doblarApuesta();
    }

    public boolean puedeSeparar() {
        return panel.puedeSeparar();
    }

    public boolean separarCartas() {
        return panel.separarCartas();
    }

    public boolean consumirNuevaRondaPendiente() {
        return panel.consumirNuevaRondaPendiente();
    }
}
