package proyectofinalcasino.interfaz;

import java.awt.FlowLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import proyectofinalcasino.modelo.Jugador;

/**
 * Construye únicamente los controles Swing de la mesa.
 */
public final class PanelControlesPartida extends JPanel {

    public interface Acciones {

        void seleccionarJugador(int indice);

        void comenzar();

        void pedir();

        void plantarse();

        void doblar();

        void separar();

        void apostar();

        void retirar();
    }

    private final JButton comenzar = boton("Comenzar partida");
    private final JButton pedir = boton("Pedir carta");
    private final JButton plantarse = boton("Plantarse");
    private final JButton doblar = boton("Doblar");
    private final JButton separar = boton("Separar");
    private final JButton apostar = boton("Apostar ficha");
    private final JButton retirar = boton("Retirar ficha");

    public PanelControlesPartida(List<Jugador> jugadores, Acciones acciones) {
        setOpaque(false);
        setLayout(new FlowLayout());
        JComboBox<String> selector = new JComboBox<>();
        jugadores.forEach(jugador -> selector.addItem(jugador.getNombre()));
        selector.setFocusable(false);
        selector.addActionListener(evento
                -> acciones.seleccionarJugador(selector.getSelectedIndex()));
        comenzar.addActionListener(evento -> acciones.comenzar());
        pedir.addActionListener(evento -> acciones.pedir());
        plantarse.addActionListener(evento -> acciones.plantarse());
        doblar.addActionListener(evento -> acciones.doblar());
        separar.addActionListener(evento -> acciones.separar());
        apostar.addActionListener(evento -> acciones.apostar());
        retirar.addActionListener(evento -> acciones.retirar());
        add(pedir);
        add(selector);
        add(plantarse);
        add(doblar);
        add(separar);
        add(apostar);
        add(retirar);
        add(comenzar);
    }

    private static JButton boton(String texto) {
        JButton boton = new JButton(texto);
        boton.setFocusable(false);
        return boton;
    }

    public void actualizar(boolean puedePedir, boolean puedePlantarse,
            boolean puedeDoblar, boolean puedeSeparar, boolean puedeApostar,
            boolean puedeRetirar, boolean puedeComenzar) {
        pedir.setEnabled(puedePedir);
        plantarse.setEnabled(puedePlantarse);
        doblar.setEnabled(puedeDoblar);
        separar.setEnabled(puedeSeparar);
        apostar.setEnabled(puedeApostar);
        retirar.setEnabled(puedeRetirar);
        comenzar.setEnabled(puedeComenzar);
    }
}
