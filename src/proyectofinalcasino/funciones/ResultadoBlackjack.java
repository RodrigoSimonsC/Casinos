
package proyectofinalcasino.funciones;

import java.util.List;

public final class ResultadoBlackjack {

    private ResultadoBlackjack() {
    }

    public static int calcularValor(List<String> cartas) {
        int total = 0;
        int ases = 0;
        for (String carta : cartas) {
            String valor = obtenerValorCarta(carta);
            if ("As".equals(valor)) {
                total += 11;
                ases++;
            } else if ("J".equals(valor) || "Q".equals(valor) || "K".equals(valor)) {
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

    public static boolean tieneBlackjack(List<String> cartas) {
        return cartas.size() == 2 && calcularValor(cartas) == 21;
    }

    public static boolean superaVeintiuno(List<String> cartas) {
        return calcularValor(cartas) > 21;
    }

    public static String resolver(List<String> cartasJugador, List<String> cartasDealer) {
        int jugador = calcularValor(cartasJugador);
        int dealer = calcularValor(cartasDealer);
        if (tieneBlackjack(cartasJugador) && !tieneBlackjack(cartasDealer)) {
            return "Blackjack: ganaste";
        }
        if (tieneBlackjack(cartasDealer) && !tieneBlackjack(cartasJugador)) {
            return "Perdiste: blackjack del dealer";
        }
        if (jugador > 21) {
            
            return "Perdiste: tu mano supera 21";
        }
        if (dealer > 21) {
            return "Ganaste: el dealer supera 21";
        }
        if (jugador > dealer) {
            return "Ganaste";
        }
        if (jugador < dealer) {
            return "Perdiste";
        }
        return "Empate";
    }

    public static String obtenerValorCarta(String carta) {
        return carta.substring(0, carta.indexOf(" de "));
    }

    public static String formatearMano(String nombre, List<String> cartas,
            boolean plantada) {
        return nombre + ": " + cartas + " | Valor: "
                + calcularValor(cartas) + " | Plantada: " + plantada;
    }
}
