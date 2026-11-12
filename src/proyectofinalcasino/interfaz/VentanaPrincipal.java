package proyectofinalcasino.interfaz;

// Ventana principal para administrar la mesa y sus jugadores.

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
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import proyectofinalcasino.conexiones.MesaRepository;
import proyectofinalcasino.conexiones.MesaRepositoryJdbc;
import proyectofinalcasino.funciones.GestorMesa;

public final class VentanaPrincipal extends JFrame {

    private final GestorMesa gestor;
    private final PanelJugadores panelJugadores;
    private final JLabel estado = new JLabel("Panel de administración", SwingConstants.RIGHT);
    private VentanaPartida partidaActiva;
    private JTabbedPane pestañas;

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
        pestañas = new JTabbedPane();
        pestañas.addTab("Jugadores", panelJugadores);
        pestañas.addTab("Configuración", panelConfiguracion);
        contenido.add(pestañas, BorderLayout.CENTER);
        setContentPane(contenido);
        setJMenuBar(crearBarraMenu());
    }

    private JMenuBar crearBarraMenu() {
        JMenuBar barra = new JMenuBar();

        JMenu mesa = new JMenu("Mesa");
        JMenuItem comenzar = new JMenuItem("Comenzar partida");
        comenzar.addActionListener(evento -> comenzarPartida());
        JMenuItem actualizar = new JMenuItem("Actualizar jugadores");
        actualizar.addActionListener(evento -> {
            refrescarJugadores();
            estado.setText("Jugadores actualizados");
        });
        mesa.add(comenzar);
        mesa.add(actualizar);

        JMenu jugadores = new JMenu("Jugadores");
        JMenuItem verJugadores = new JMenuItem("Ver jugadores");
        verJugadores.addActionListener(evento -> pestañas.setSelectedIndex(0));
        jugadores.add(verJugadores);

        JMenu configuracion = new JMenu("Configuración");
        JMenuItem abrirConfiguracion = new JMenuItem("Abrir configuración");
        abrirConfiguracion.addActionListener(
                evento -> pestañas.setSelectedIndex(1));
        configuracion.add(abrirConfiguracion);

        JMenu ayuda = new JMenu("Ayuda");
        JMenuItem acercaDe = new JMenuItem("Acerca de Casino Blackjack");
        acercaDe.addActionListener(evento -> JOptionPane.showMessageDialog(this,
                "Administración de mesa de Blackjack\n"
                + "Gestiona jugadores, configuración y partidas.",
                "Acerca de", JOptionPane.INFORMATION_MESSAGE));
        ayuda.add(acercaDe);

        JMenu archivo = new JMenu("Archivo");
        JMenuItem salir = new JMenuItem("Salir");
        salir.addActionListener(evento -> dispose());
        archivo.add(salir);

        barra.add(archivo);
        barra.add(mesa);
        barra.add(jugadores);
        barra.add(configuracion);
        barra.add(ayuda);
        return barra;
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
        if (partidaActiva != null) {
            if (!partidaActiva.mismosJugadores(gestor.obtenerJugadores())) {
                partidaActiva.dispose();
                partidaActiva = null;
            }
        }
        if (partidaActiva != null) {
            setVisible(false);
            partidaActiva.mostrarPartida();
            return;
        }
        proyectofinalcasino.funciones.BarajaBlackjack baraja = gestor.comenzarPartida();
        if (baraja == null) {
            javax.swing.JOptionPane.showMessageDialog(this,
                    "Agrega al menos un jugador antes de comenzar.",
                    "No se puede comenzar", javax.swing.JOptionPane.WARNING_MESSAGE);
            return;
        }
        setVisible(false);
        partidaActiva = new VentanaPartida(this, gestor, baraja);
    }

    public void refrescarJugadores() {
        panelJugadores.refrescar();
    }
}
