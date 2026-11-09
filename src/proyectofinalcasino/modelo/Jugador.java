package proyectofinalcasino.modelo;

/**
 * Representa a un jugador sentado en la mesa.
 */
public final class Jugador {

    private final String nombre;
    private final double saldo;

    public Jugador(String nombre, double saldo) {
        this.nombre = nombre;
        this.saldo = saldo;
    }

    public String getNombre() {
        return nombre;
    }

    public double getSaldo() {
        return saldo;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
