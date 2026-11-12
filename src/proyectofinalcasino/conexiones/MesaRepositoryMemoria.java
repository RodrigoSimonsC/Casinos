package proyectofinalcasino.conexiones;

import java.util.ArrayList;
import java.util.List;
import proyectofinalcasino.modelo.ConfiguracionMesa;
import proyectofinalcasino.modelo.Jugador;

/**
 * Conexión local que permite trabajar sin una base de datos externa.
 */
public final class MesaRepositoryMemoria implements MesaRepository {

    private final List<Jugador> jugadores = new ArrayList<>();
    private ConfiguracionMesa configuracion = new ConfiguracionMesa();

    @Override
    public List<Jugador> consultarJugadores() {
        return new ArrayList<>(jugadores);
    }

    @Override
    public void guardarJugadores(List<Jugador> jugadores) {
        this.jugadores.clear();
        this.jugadores.addAll(jugadores);
    }

    @Override
    public boolean actualizarSaldo(String nombre, double saldo) {
        for (int indice = 0; indice < jugadores.size(); indice++) {
            Jugador jugador = jugadores.get(indice);
            if (jugador.getNombre().equals(nombre)) {
                jugadores.set(indice, new Jugador(nombre, saldo));
                return true;
            }
        }
        return false;
    }

    @Override
    public ConfiguracionMesa consultarConfiguracion() {
        return configuracion;
    }

    @Override
    public void guardarConfiguracion(ConfiguracionMesa configuracion) {
        this.configuracion = configuracion;
    }
}
