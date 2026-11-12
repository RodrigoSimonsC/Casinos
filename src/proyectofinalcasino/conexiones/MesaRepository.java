package proyectofinalcasino.conexiones;

import java.util.List;
import proyectofinalcasino.modelo.ConfiguracionMesa;
import proyectofinalcasino.modelo.Jugador;

/**
 * Punto de conexión de la aplicación con el origen de datos de la mesa.
 */
public interface MesaRepository {

    List<Jugador> consultarJugadores();

    void guardarJugadores(List<Jugador> jugadores);

    boolean actualizarSaldo(String nombre, double saldo);

    ConfiguracionMesa consultarConfiguracion();

    void guardarConfiguracion(ConfiguracionMesa configuracion);
}
