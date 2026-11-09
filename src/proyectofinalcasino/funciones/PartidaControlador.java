package proyectofinalcasino.funciones;

import java.util.List;
import proyectofinalcasino.conexiones.PartidaRepositoryJdbc;
import proyectofinalcasino.modelo.ConfiguracionMesa;
import proyectofinalcasino.modelo.Jugador;

/**
 * Coordina el ciclo de una partida. No crea componentes Swing ni dibuja.
 */
public final class PartidaControlador implements PartidaEventoListener {

    private final GestorMesa gestorMesa;
    private final PartidaPersistencia persistencia;
    private final List<Jugador> jugadores;
    private EstadoPartida estado = EstadoPartida.ESPERANDO_APUESTAS;

    public PartidaControlador(GestorMesa gestorMesa) {
        this.gestorMesa = gestorMesa;
        jugadores = List.copyOf(gestorMesa.obtenerJugadores());
        persistencia = new PartidaPersistencia(new PartidaRepositoryJdbc(),
                jugadores);
    }

    public boolean abrir(ConfiguracionMesa configuracion) {
        return persistencia.abrirPartida(configuracion);
    }

    public boolean puedeApostar(int jugador, int apuestaActual) {
        if (estado != EstadoPartida.ESPERANDO_APUESTAS
                || jugador < 0 || jugador >= jugadores.size()) {
            return false;
        }
        return gestorMesa.obtenerSaldo(jugadores.get(jugador).getNombre())
                >= apuestaActual + gestorMesa.obtenerConfiguracion()
                        .getMultiplesApuesta();
    }

    public boolean iniciarRonda(int[] apuestas) {
        if (estado != EstadoPartida.ESPERANDO_APUESTAS
                || apuestas.length != jugadores.size()) {
            return false;
        }
        for (int indice = 0; indice < apuestas.length; indice++) {
            if (apuestas[indice] <= 0
                    || gestorMesa.obtenerSaldo(jugadores.get(indice).getNombre())
                    < apuestas[indice]) {
                return false;
            }
        }
        int jugadoresDescontados = 0;
        for (int indice = 0; indice < apuestas.length; indice++) {
            String nombre = jugadores.get(indice).getNombre();
            if (!gestorMesa.modificarSaldo(nombre, -apuestas[indice])) {
                restaurarApuestas(apuestas, jugadoresDescontados);
                return false;
            }
            persistencia.registrarSaldo(nombre, gestorMesa.obtenerSaldo(nombre));
            jugadoresDescontados++;
        }
        if (!persistencia.abrirRonda(apuestas)) {
            restaurarApuestas(apuestas, jugadoresDescontados);
            return false;
        }
        estado = EstadoPartida.REPARTIENDO;
        return true;
    }

    private void restaurarApuestas(int[] apuestas, int cantidadJugadores) {
        for (int indice = 0; indice < cantidadJugadores; indice++) {
            String nombre = jugadores.get(indice).getNombre();
            gestorMesa.modificarSaldo(nombre, apuestas[indice]);
            persistencia.registrarSaldo(nombre, gestorMesa.obtenerSaldo(nombre));
        }
    }

    public void marcarTurnoJugador() {
        estado = EstadoPartida.TURNO_JUGADOR;
    }

    public void marcarTurnoDealer() {
        estado = EstadoPartida.TURNO_DEALER;
    }

    public void finalizarRonda(String[] resultados, int[] puntuaciones) {
        estado = EstadoPartida.RESULTADO;
    }

    public void pagar(int jugador, double pago) {
        if (jugador < 0 || jugador >= jugadores.size()) {
            return;
        }
        String nombre = jugadores.get(jugador).getNombre();
        if (pago > 0) {
            gestorMesa.modificarSaldo(nombre, pago);
        }
        persistencia.registrarPago(jugador, pago);
        persistencia.registrarSaldo(nombre, gestorMesa.obtenerSaldo(nombre));
    }

    public double saldo(int jugador) {
        return jugador < 0 || jugador >= jugadores.size()
                ? 0 : gestorMesa.obtenerSaldo(jugadores.get(jugador).getNombre());
    }

    public int cantidadJugadores() {
        return jugadores.size();
    }

    public EstadoPartida obtenerEstado() {
        return estado;
    }

    public void prepararNuevaRonda() {
        if (estado == EstadoPartida.RESULTADO) {
            estado = EstadoPartida.ESPERANDO_APUESTAS;
        }
    }

    public void cerrar() {
        persistencia.cerrar("Finalizada");
    }

    public PartidaEventoListener eventos() {
        return this;
    }

    @Override
    public void cartaRepartida(int jugador, int mano, String carta,
            boolean dealer) {
        persistencia.cartaRepartida(jugador, mano, carta, dealer);
    }

    @Override
    public void turnoIniciado(int jugador, int mano, boolean dealer) {
        if (dealer) {
            marcarTurnoDealer();
        } else {
            marcarTurnoJugador();
        }
        persistencia.turnoIniciado(jugador, mano, dealer);
    }

    @Override
    public void decisionTomada(int jugador, int mano, String decision,
            double valorAdicional) {
        persistencia.decisionTomada(jugador, mano, decision, valorAdicional);
    }

    @Override
    public void partidaFinalizada(String[] resultados, int[] puntuaciones) {
        finalizarRonda(resultados, puntuaciones);
        persistencia.partidaFinalizada(resultados, puntuaciones);
    }
}
