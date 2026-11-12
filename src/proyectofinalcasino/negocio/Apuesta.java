package proyectofinalcasino.negocio;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Importe de una apuesta, representado sin errores de precisión binaria.
 */
public final class Apuesta {

    private BigDecimal importe;

    public Apuesta(BigDecimal importe) {
        validar(importe);
        this.importe = importe;
    }

    public BigDecimal obtenerImporte() {
        return importe;
    }

    public void doblar() {
        importe = importe.multiply(BigDecimal.valueOf(2));
    }

    private void validar(BigDecimal importe) {
        Objects.requireNonNull(importe, "El importe no puede ser null");
        if (importe.signum() <= 0) {
            throw new IllegalArgumentException("La apuesta debe ser positiva");
        }
    }
}
