package proyectofinalcasino.logica;

import java.util.List;
import java.util.Objects;
import proyectofinalcasino.modelo.Carta;
import proyectofinalcasino.modelo.Mano;

/**
 * Reglas puras de Blackjack sobre el modelo de dominio.
 */
public final class ReglasBlackjack {

    public int calcularValor(Mano mano) {
        Objects.requireNonNull(mano, "La mano no puede ser null");
        return calcularValor(mano.obtenerCartas());
    }

    public int calcularValor(List<Carta> cartas) {
        Objects.requireNonNull(cartas, "Las cartas no pueden ser null");
        int total = 0;
        int ases = 0;
        for (Carta carta : cartas) {
            String valor = carta.obtenerValor();
            if ("As".equals(valor)) {
                total += 11;
                ases++;
            } else if ("J".equals(valor) || "Q".equals(valor)
                    || "K".equals(valor)) {
                total += 10;
            } else {
                total += Integer.parseInt(valor);
            }
        }
        while (total > 21 && ases > 0) {
            total -= 10;
            ases--;
        }
        return total;
    }

    public boolean tieneBlackjack(Mano mano) {
        return mano.obtenerCartas().size() == 2 && calcularValor(mano) == 21;
    }

    public boolean estaReventada(Mano mano) {
        return calcularValor(mano) > 21;
    }

    public boolean puedeDividir(Mano mano) {
        List<Carta> cartas = mano.obtenerCartas();
        return cartas.size() == 2
                && normalizar(cartas.get(0)).equals(normalizar(cartas.get(1)));
    }

    private String normalizar(Carta carta) {
        String valor = carta.obtenerValor();
        return "10".equals(valor) || "J".equals(valor)
                || "Q".equals(valor) || "K".equals(valor) ? "10" : valor;
    }
}
