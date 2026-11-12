package blackcasino.elementosCasino;

// Modelo de ejemplo que agrupa las cartas de una mano.

import blackcasino.modelos.Carta;
import java.util.ArrayList;
import java.util.List;
import proyectofinalcasino.funciones.BarajaBlackjack;

public final class Mano {
    
    private final List<Carta> cartas = new ArrayList<>();
    private boolean plantada;
    
    
    public Carta agregarCarta(BarajaBlackjack baraja, int posicionActual) {
        
        List<String> cartasBaraja = baraja.obtenerCartas();
        
        if (cartasBaraja.isEmpty()) {
            throw new IllegalStateException("La baraja no contiene cartas");
        }
        if (posicionActual < 0 || posicionActual >= cartasBaraja.size()) {
            throw new IndexOutOfBoundsException("La posición no existe en la baraja");
        }

        String texto = cartasBaraja.get(posicionActual);
        int separadorValor = texto.indexOf(" de ");
        int inicioMazo = texto.lastIndexOf(" (baraja ");
        if (separadorValor < 1 || inicioMazo <= separadorValor + 4
                || !texto.endsWith(")")) {
            throw new IllegalArgumentException(
                    "Formato de carta no reconocido: " + texto);
        }
        Carta carta = new Carta(
                texto.substring(0, separadorValor),
                texto.substring(separadorValor + 4, inicioMazo),
                Integer.parseInt(texto.substring(inicioMazo + 9,
                        texto.length() - 1)));
        
        cartas.add(carta);
        return carta;
    }

    public void agregarCarta(Carta carta) {
        cartas.add(carta);
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
}