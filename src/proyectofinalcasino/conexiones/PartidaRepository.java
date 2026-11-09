package proyectofinalcasino.conexiones;

import java.util.List;
import proyectofinalcasino.modelo.ConfiguracionMesa;
import proyectofinalcasino.modelo.Jugador;

/**
 * Persistencia de una partida y de los eventos que ocurren durante sus rondas.
 */
public interface PartidaRepository {

    int crearPartida(ConfiguracionMesa configuracion, List<Jugador> jugadores);

    int crearRonda(int partidaId, int numeroRonda, List<Jugador> jugadores,
            int[] apuestas);

    int crearMano(int rondaId, int jugador, int numeroMano, double apuesta);

    int obtenerEstadoUsuarioId(String nombre);

    int obtenerUsuarioId(String nombre);

    boolean registrarCarta(int rondaId, int manoId, int jugador,
            String carta, boolean dealer);

    int crearTurno(int rondaId, int jugador, boolean dealer, int orden);

    boolean registrarDecision(int turnoId, int manoId, String decision,
            double valorAdicional);

    boolean registrarResultado(int rondaId, int jugador, int puntuacion,
            String resultado);

    boolean registrarPago(int rondaId, int jugador, double monto);

    boolean actualizarSaldoEnPartida(int partidaId, String jugador,
            double saldo);

    boolean cerrarPartida(int partidaId, String estado);
}
