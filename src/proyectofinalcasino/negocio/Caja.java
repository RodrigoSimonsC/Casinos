package proyectofinalcasino.negocio;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Saldo disponible para liquidar apuestas.
 */
public final class Caja {

    private BigDecimal saldo;

    public Caja(BigDecimal saldoInicial) {
        Objects.requireNonNull(saldoInicial, "El saldo no puede ser null");
        if (saldoInicial.signum() < 0) {
            throw new IllegalArgumentException("El saldo no puede ser negativo");
        }
        saldo = saldoInicial;
    }

    public BigDecimal obtenerSaldo() {
        return saldo;
    }

    public void ingresar(BigDecimal importe) {
        validarImporte(importe);
        saldo = saldo.add(importe);
    }

    public void retirar(BigDecimal importe) {
        validarImporte(importe);
        if (saldo.compareTo(importe) < 0) {
            throw new IllegalStateException("La caja no tiene saldo suficiente");
        }
        saldo = saldo.subtract(importe);
    }

    private void validarImporte(BigDecimal importe) {
        Objects.requireNonNull(importe, "El importe no puede ser null");
        if (importe.signum() <= 0) {
            throw new IllegalArgumentException("El importe debe ser positivo");
        }
    }
}
