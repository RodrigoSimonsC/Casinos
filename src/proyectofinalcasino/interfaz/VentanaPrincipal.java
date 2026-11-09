package proyectofinalcasino.interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import proyectofinalcasino.conexiones.MesaRepository;
import proyectofinalcasino.conexiones.MesaRepositoryJdbc;
import proyectofinalcasino.funciones.GestorMesa;

public final class VentanaPrincipal extends JFrame {

    private final GestorMesa gestor;
    private final PanelJugadores panelJugadores;
    private final JLabel estado = new JLabel("Panel de administración", SwingConstants.RIGHT);

    public VentanaPrincipal() {
        this(new MesaRepositoryJdbc());
    }

    public VentanaPrincipal(MesaRepository repository) {
        gestor = new GestorMesa(repository);
        panelJugadores = new PanelJugadores(gestor);
        configurarVentana();
        setVisible(true);
    }

    private void configurarVentana() {
        setTitle("Casino | Administración de mesa Blackjack");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1100, 700);
        setLocationRelativeTo(null);

        JPanel contenido = new JPanel(new BorderLayout(15, 15));
        contenido.setBorder(new EmptyBorder(15, 15, 15, 15));
        contenido.setBackground(new Color(245, 247, 250));
        contenido.add(crearEncabezado(), BorderLayout.NORTH);

        PanelConfiguracion panelConfiguracion =
                new PanelConfiguracion(gestor, panelJugadores::refrescar);
        JTabbedPane pestañas = new JTabbedPane();
        pestañas.addTab("Jugadores", panelJugadores);
        pestañas.addTab("Configuración", panelConfiguracion);
        contenido.add(pestañas, BorderLayout.CENTER);
        setContentPane(contenido);
    }

    private JPanel crearEncabezado() {
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(new Color(31, 78, 121));
        encabezado.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        JLabel titulo = new JLabel("MESA DE BLACKJACK");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 24));
        encabezado.add(titulo, BorderLayout.WEST);
        estado.setForeground(new Color(220, 230, 240));
        JPanel acciones = new JPanel(new BorderLayout(15, 0));
        acciones.setOpaque(false);
        JButton comenzar = new JButton("Comenzar partida");
        comenzar.addActionListener(evento -> comenzarPartida());
        acciones.add(estado, BorderLayout.CENTER);
        acciones.add(comenzar, BorderLayout.EAST);
        encabezado.add(acciones, BorderLayout.EAST);
        return encabezado;
    }

    private void comenzarPartida() {
        proyectofinalcasino.funciones.BarajaBlackjack baraja = gestor.comenzarPartida();
        if (baraja == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Agrega al menos un jugador antes de comenzar.",
                    "No se puede comenzar", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        setVisible(false);
        new VentanaPartida(this, gestor, baraja);
    }

    public void refrescarJugadores() {
        panelJugadores.refrescar();
    }
}
