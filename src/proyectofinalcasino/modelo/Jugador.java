package proyectofinalcasino.modelo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Representa a un jugador sentado en la mesa.
 */
public final class Jugador {

    private final String nombre;
    private BigDecimal saldo;
    private final List<Mano> manos = new ArrayList<>();

    public Jugador(String nombre, double saldo) {
        this(nombre, BigDecimal.valueOf(saldo));
    }

    public Jugador(String nombre, BigDecimal saldo) {
        this.nombre = nombre;
        this.saldo = Objects.requireNonNull(saldo, "El saldo no puede ser null");
        if (saldo.signum() < 0) {
            throw new IllegalArgumentException("El saldo no puede ser negativo");
        }
        manos.add(new Mano());
    }

    public String getNombre() {
        return nombre;
    }

    public double getSaldo() {
        return saldo.doubleValue();
    }

    public BigDecimal obtenerSaldoMonetario() {
        return saldo;
    }

    public void retirarSaldo(BigDecimal importe) {
        validarImporte(importe);
        if (saldo.compareTo(importe) < 0) {
            throw new IllegalStateException("El jugador no tiene saldo suficiente");
        }
        saldo = saldo.subtract(importe);
    }

    public void ingresarSaldo(BigDecimal importe) {
        validarImporte(importe);
        saldo = saldo.add(importe);
    }

    public List<Mano> obtenerManos() {
        return List.copyOf(manos);
    }

    public Mano obtenerMano() {
        return manos.get(0);
    }

    public void asignarMano(Mano mano) {
        manos.clear();
        agregarMano(mano);
    }

    public void agregarMano(Mano mano) {
        if (mano == null) {
            throw new NullPointerException("La mano no puede ser null");
        }
        manos.add(mano);
    }

    public void limpiarManos() {
        manos.clear();
        manos.add(new Mano());
    }

    private void validarImporte(BigDecimal importe) {
        Objects.requireNonNull(importe, "El importe no puede ser null");
        if (importe.signum() <= 0) {
            throw new IllegalArgumentException("El importe debe ser positivo");
        }
    }

    @Override
    public String toString() {
        return nombre;
    }
}
