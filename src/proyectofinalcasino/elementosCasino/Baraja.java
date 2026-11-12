package proyectofinalcasino.elementosCasino;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import proyectofinalcasino.funciones.BarajaBlackjack;
import proyectofinalcasino.modelo.Carta;

/**
 * Adaptador tipado de la baraja utilizada por el motor actual.
 */
public final class Baraja {

    private final int cantidadMazos;
    private BarajaBlackjack baraja;

    public Baraja(int cantidadMazos) {
        this.cantidadMazos = Math.max(1, cantidadMazos);
        baraja = new BarajaBlackjack(this.cantidadMazos);
    }

    public List<Carta> obtenerCartas() {
        List<Carta> resultado = new ArrayList<>();
        for (String texto : baraja.obtenerCartas()) {
            resultado.add(convertir(texto));
        }
        return List.copyOf(resultado);
    }

    public void reiniciar() {
        baraja = new BarajaBlackjack(cantidadMazos);
    }

    private Carta convertir(String texto) {
        Objects.requireNonNull(texto, "La carta no puede ser null");
        int separadorValor = texto.indexOf(" de ");
        int inicioMazo = texto.lastIndexOf(" (baraja ");
        if (separadorValor < 1 || inicioMazo <= separadorValor + 4
                || !texto.endsWith(")")) {
            throw new IllegalArgumentException(
                    "Formato de carta no reconocido: " + texto);
        }
        try {
            return new Carta(
                    texto.substring(0, separadorValor),
                    texto.substring(separadorValor + 4, inicioMazo),
                    Integer.parseInt(texto.substring(inicioMazo + 9,
                            texto.length() - 1)));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Número de mazo no válido: " + texto, exception);
        }
    }
}
