package proyectofinalcasino.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import proyectofinalcasino.funciones.BarajaBlackjack;
import proyectofinalcasino.elementosCasino.Baraja;

/**
 * Cartas y estado de una mano de Blackjack.
 */
public final class Mano {

    private final List<Carta> cartas = new ArrayList<>();
    private boolean plantada;

    public Carta agregarCarta(BarajaBlackjack baraja, int posicionActual) {
        Objects.requireNonNull(baraja, "La baraja no puede ser null");
        List<String> cartasBaraja = baraja.obtenerCartas();
        if (cartasBaraja.isEmpty()) {
            throw new IllegalStateException("La baraja no contiene cartas");
        }
        if (posicionActual < 0 || posicionActual >= cartasBaraja.size()) {
            throw new IndexOutOfBoundsException(
                    "La posición no existe en la baraja");
        }

        Carta carta = convertir(cartasBaraja.get(posicionActual));
        cartas.add(carta);
        return carta;
    }

    public Carta agregarCarta(Baraja baraja, int posicionActual) {
        Objects.requireNonNull(baraja, "La baraja no puede ser null");
        List<Carta> cartasBaraja = baraja.obtenerCartas();
        if (cartasBaraja.isEmpty()) {
            throw new IllegalStateException("La baraja no contiene cartas");
        }
        if (posicionActual < 0 || posicionActual >= cartasBaraja.size()) {
            throw new IndexOutOfBoundsException(
                    "La posición no existe en la baraja");
        }
        Carta carta = cartasBaraja.get(posicionActual);
        cartas.add(carta);
        return carta;
    }

    public void agregarCarta(Carta carta) {
        cartas.add(Objects.requireNonNull(carta, "La carta no puede ser null"));
    }

    public List<Carta> obtenerCartas() {
        return List.copyOf(cartas);
    }

    public boolean estaPlantada() {
        return plantada;
    }

    public void plantarse() {
        plantada = true;
    }

    public Carta retirarUltimaCarta() {
        if (cartas.isEmpty()) {
            throw new IllegalStateException("La mano no contiene cartas");
        }
        return cartas.remove(cartas.size() - 1);
    }

    public void limpiar() {
        cartas.clear();
        plantada = false;
    }

    private Carta convertir(String texto) {
        int separadorValor = texto.indexOf(" de ");
        int inicioMazo = texto.lastIndexOf(" (baraja ");
        if (separadorValor < 1 || inicioMazo <= separadorValor + 4
                || !texto.endsWith(")")) {
            throw new IllegalArgumentException(
                    "Formato de carta no reconocido: " + texto);
        }
        String valor = texto.substring(0, separadorValor);
        String palo = texto.substring(separadorValor + 4, inicioMazo);
        String numero = texto.substring(inicioMazo + 9, texto.length() - 1);
        try {
            return new Carta(valor, palo, Integer.parseInt(numero));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Número de mazo no válido: " + texto, exception);
        }
    }
}
