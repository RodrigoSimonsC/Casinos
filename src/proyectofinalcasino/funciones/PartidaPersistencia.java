package proyectofinalcasino.funciones;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import proyectofinalcasino.conexiones.PartidaRepository;
import proyectofinalcasino.modelo.ConfiguracionMesa;
import proyectofinalcasino.modelo.Jugador;

/**
 * Coordina la persistencia de una sesión y de sus rondas sin exponer JDBC a la
 * interfaz ni al motor visual.
 */
public final class PartidaPersistencia implements PartidaEventoListener {

    private final PartidaRepository repository;
    private final List<Jugador> jugadores;
    private final Map<Integer, Integer> manos = new HashMap<>();
    private int partidaId;
    private int rondaId;
    private int numeroRonda;
    private int ordenTurno;

    public PartidaPersistencia(PartidaRepository repository,
            List<Jugador> jugadores) {
        this.repository = repository;
        this.jugadores = List.copyOf(jugadores);
    }

    public boolean abrirPartida(ConfiguracionMesa configuracion) {
        partidaId = repository.crearPartida(configuracion, jugadores);
        return partidaId > 0;
    }

    public boolean abrirRonda(int[] apuestas) {
        if (partidaId == 0) {
            return true;
        }
        numeroRonda++;
        rondaId = repository.crearRonda(partidaId, numeroRonda, jugadores, apuestas);
        manos.clear();
        ordenTurno = 0;
        if (rondaId == 0) {
            return false;
        }
        for (int indice = 0; indice < jugadores.size(); indice++) {
            int jugadorId = repository.obtenerEstadoUsuarioId(
                    jugadores.get(indice).getNombre());
            int manoId = repository.crearMano(rondaId, jugadorId, 1,
                    apuestas[indice]);
            if (manoId == 0) {
                return false;
            }
            manos.put(indice, manoId);
        }
        return true;
    }

    public void registrarSaldo(String jugador, double saldo) {
        if (partidaId > 0) {
            repository.actualizarSaldoEnPartida(partidaId, jugador, saldo);
        }
    }

    public void registrarPago(int jugador, double monto) {
        if (rondaId == 0) {
            return;
        }
        int jugadorId = repository.obtenerEstadoUsuarioId(
                jugadores.get(jugador).getNombre());
        repository.registrarPago(rondaId, jugadorId, monto);
    }

    public void cerrar(String estado) {
        if (partidaId > 0) {
            repository.cerrarPartida(partidaId, estado);
        }
    }

    @Override
    public void cartaRepartida(int jugador, int mano, String carta,
            boolean dealer) {
        if (rondaId == 0) {
            return;
        }
        int manoId = dealer ? 0 : manos.getOrDefault(jugador, 0);
        int jugadorId = dealer ? 0 : repository.obtenerEstadoUsuarioId(
                jugadores.get(jugador).getNombre());
        repository.registrarCarta(rondaId, manoId, jugadorId, carta, dealer);
    }

    @Override
    public void turnoIniciado(int jugador, int mano, boolean dealer) {
        if (rondaId == 0) {
            return;
        }
        int jugadorId = dealer ? 0 : repository.obtenerEstadoUsuarioId(
                jugadores.get(jugador).getNombre());
        repository.crearTurno(rondaId, jugadorId, dealer, ++ordenTurno);
    }

    @Override
    public void decisionTomada(int jugador, int mano, String decision,
            double valorAdicional) {
        if (rondaId == 0) {
            return;
        }
        int jugadorId = repository.obtenerEstadoUsuarioId(
                jugadores.get(jugador).getNombre());
        int turnoId = repository.crearTurno(rondaId, jugadorId, false,
                ++ordenTurno);
        int manoId = manos.getOrDefault(jugador, 0);
        if (turnoId > 0 && manoId > 0) {
            repository.registrarDecision(turnoId, manoId, decision,
                    valorAdicional);
        }
    }

    @Override
    public void partidaFinalizada(String[] resultados, int[] puntuaciones) {
        for (int indice = 0; indice < jugadores.size(); indice++) {
            int jugadorId = repository.obtenerEstadoUsuarioId(
                    jugadores.get(indice).getNombre());
            repository.registrarResultado(rondaId, jugadorId,
                    puntuaciones[indice], resultados[indice]);
        }
    }
}
