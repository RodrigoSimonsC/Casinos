package proyectofinalcasino.funciones;

import java.util.List;
import proyectofinalcasino.conexiones.MesaRepository;
import proyectofinalcasino.modelo.ConfiguracionMesa;
import proyectofinalcasino.modelo.Jugador;

/**
 * Centraliza las reglas de negocio de la mesa y evita que la interfaz las
 * conozca directamente.
 */
public final class GestorMesa {

    private final MesaRepository repository;

    public GestorMesa(MesaRepository repository) {
        this.repository = repository;
    }

    public List<Jugador> obtenerJugadores() {
        return repository.consultarJugadores();
    }

    public List<Jugador> obtenerHistorialJugadores() {
        return repository.consultarHistorialJugadores();
    }

    public ConfiguracionMesa obtenerConfiguracion() {
        return repository.consultarConfiguracion();
    }

    public double obtenerSaldo(String nombre) {
        for (Jugador jugador : obtenerJugadores()) {
            if (jugador.getNombre().equals(nombre)) {
                return jugador.getSaldo();
            }
        }
        return 0;
    }

    public boolean modificarSaldo(String nombre, double cambio) {
        double saldoActual = obtenerSaldo(nombre);
        if (saldoActual + cambio < 0) {
            return false;
        }
        return repository.actualizarSaldo(nombre, saldoActual + cambio);
    }

    public boolean recargarSaldo(String nombre, double monto) {
        return monto > 0 && modificarSaldo(nombre, monto);
    }

    public String agregarJugador(String nombre, double saldo) {
        String nombreNormalizado = nombre == null ? "" : nombre.trim();
        if (nombreNormalizado.isEmpty()) {
            return "El nombre del jugador es obligatorio.";
        }
        if (saldo < 0) {
            return "El saldo no puede ser negativo.";
        }

        List<Jugador> jugadores = obtenerJugadores();
        ConfiguracionMesa configuracion = obtenerConfiguracion();
        if (jugadores.size() >= configuracion.getMaximoJugadores()) {
            return "La mesa alcanzó el máximo de jugadores.";
        }
        jugadores.add(new Jugador(nombreNormalizado, saldo));
        repository.guardarJugadores(jugadores);
        return "";
    }

    public String eliminarJugador(int indice) {
        List<Jugador> jugadores = obtenerJugadores();
        if (indice < 0 || indice >= jugadores.size()) {
            return "Selecciona un jugador válido.";
        }
        jugadores.remove(indice);
        repository.guardarJugadores(jugadores);
        return "";
    }

    public String actualizarConfiguracion(int barajas, int maximoJugadores) {
        return actualizarConfiguracion(barajas, maximoJugadores, 2,
                "Clásico (Paga 3:2)", true, false);
    }

    public String actualizarConfiguracion(int barajas, int maximoJugadores,
            int multiplesApuesta, String varianteReglas, boolean efectosSonido,
            boolean recargaAutomatica) {
        if (barajas < 1 || maximoJugadores < 1 || multiplesApuesta < 1) {
            return "Las cantidades deben ser mayores que cero.";
        }
        if (obtenerJugadores().size() > maximoJugadores) {
            return "No puedes reducir la mesa por debajo de los jugadores actuales.";
        }

        ConfiguracionMesa configuracion = new ConfiguracionMesa();
        configuracion.setCantidadBarajas(barajas);
        configuracion.setMaximoJugadores(maximoJugadores);
        configuracion.setMultiplesApuesta(multiplesApuesta);
        configuracion.setVarianteReglas(varianteReglas);
        configuracion.setEfectosSonido(efectosSonido);
        configuracion.setRecargaAutomatica(recargaAutomatica);
        repository.guardarConfiguracion(configuracion);
        return "";
    }

    public BarajaBlackjack comenzarPartida() {
        if (obtenerJugadores().isEmpty()) {
            return null;
        }
        BarajaBlackjack baraja = new BarajaBlackjack(obtenerConfiguracion());
        baraja.imprimirPosiciones();
        return baraja;
    }

}
