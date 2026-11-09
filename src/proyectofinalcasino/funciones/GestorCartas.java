package proyectofinalcasino.funciones;

import java.util.ArrayList;
import java.util.List;

/**
 * Contiene las reglas de puntuación y validación de manos.
 */
public final class GestorCartas {

    public int calcularValor(List<String> cartas) {
        return ResultadoBlackjack.calcularValor(cartas);
    }

    public boolean tieneBlackjack(List<String> cartas) {
        return ResultadoBlackjack.tieneBlackjack(cartas);
    }

    public boolean superaVeintiuno(List<String> cartas) {
        return ResultadoBlackjack.superaVeintiuno(cartas);
    }

    public String valorVisible(List<String> cartas) {
        int total = calcularValor(cartas);
        boolean tieneAs = false;
        for (String carta : cartas) {
            if ("As".equals(ResultadoBlackjack.obtenerValorCarta(carta))) {
                tieneAs = true;
                break;
            }
        }
        return tieneAs && total < 21 ? total + " (As: 1/11)" : String.valueOf(total);
    }

    public boolean puedeSeparar(List<String> cartas) {
        return cartas.size() == 2
                && normalizarValor(cartas.get(0)).equals(normalizarValor(cartas.get(1)));
    }

    public String resolver(List<String> jugador, List<String> dealer) {
        return ResultadoBlackjack.resolver(jugador, dealer);
    }

    public List<String> copiar(List<String> cartas) {
        return new ArrayList<>(cartas);
    }

    private String normalizarValor(String carta) {
        String valor = ResultadoBlackjack.obtenerValorCarta(carta);
        return "10".equals(valor) || "J".equals(valor)
                || "Q".equals(valor) || "K".equals(valor) ? "10" : valor;
    }
}
