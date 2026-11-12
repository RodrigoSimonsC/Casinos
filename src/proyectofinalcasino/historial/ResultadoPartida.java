package proyectofinalcasino.historial;

import java.math.BigDecimal;
import java.util.Objects;
import proyectofinalcasino.modelo.Jugador;

/**
 * Resultado liquidado de una mano.
 */
public record ResultadoPartida(Jugador jugador, BigDecimal pago,
        String resultado) {

    public ResultadoPartida {
        Objects.requireNonNull(jugador, "El jugador no puede ser null");
        Objects.requireNonNull(pago, "El pago no puede ser null");
        Objects.requireNonNull(resultado, "El resultado no puede ser null");
    }
}
