package proyectofinalcasino.funciones;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import proyectofinalcasino.modelo.ConfiguracionMesa;

/**
 * Genera y mezcla las cartas que se utilizaran en una partida.
 */
public final class BarajaBlackjack {

    private static final String[] PALOS = {
        "Corazones", "Diamantes", "Treboles", "Picas"
    };
    private static final String[] VALORES = {
        "As", "2", "3", "4", "5", "6", "7", "8", "9", "10",
        "J", "Q", "K"
    };

    private final SecureRandom aleatorio;
    private final List<String> cartas;

    public BarajaBlackjack(int cantidadBarajas) {
        if (cantidadBarajas < 1) {
            cantidadBarajas = 1;
        }
        aleatorio = new SecureRandom();
        cartas = generarCartas(cantidadBarajas);
        barajar();
    }

    public BarajaBlackjack(ConfiguracionMesa configuracion) {
        this(configuracion.getCantidadBarajas());
    }

    private List<String> generarCartas(int cantidadBarajas) {
        List<String> cartasGeneradas = new ArrayList<>();
        for (int numeroBaraja = 1; numeroBaraja <= cantidadBarajas; numeroBaraja++) {
            for (String palo : PALOS) {
                for (String valor : VALORES) {
                    cartasGeneradas.add(valor + " de " + palo
                            + " (baraja " + numeroBaraja + ")");
                }
            }
        }
        return cartasGeneradas;
    }

    private void barajar() {
        for (int posicion = cartas.size() - 1; posicion > 0; posicion--) {
            int posicionAleatoria = aleatorio.nextInt(posicion + 1);
            String carta = cartas.get(posicion);
            cartas.set(posicion, cartas.get(posicionAleatoria));
            cartas.set(posicionAleatoria, carta);
        }
    }

    public List<String> obtenerCartas() {
        return List.copyOf(cartas);
    }

    public void imprimirPosiciones() {
        System.out.println("=== Baraja de blackjack revuelta ===");
        System.out.println("Total de cartas: " + cartas.size());
        for (int posicion = 0; posicion < cartas.size(); posicion++) {
            
            System.out.printf("Posicion %d: %s%n", posicion + 1, cartas.get(posicion));
        }
        System.out.println("=== Fin de la baraja ===");
    }
}
