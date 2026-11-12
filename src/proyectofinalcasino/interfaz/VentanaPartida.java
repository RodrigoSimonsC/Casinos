package proyectofinalcasino.interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.KeyStroke;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import proyectofinalcasino.funciones.GestorMesa;
import proyectofinalcasino.funciones.BarajaBlackjack;
import proyectofinalcasino.funciones.PartidaControlador;
import proyectofinalcasino.modelo.Jugador;

/**
 * Vista de la mesa activa en pantalla completa.
 */
public final class VentanaPartida extends JFrame {

    private final JFrame ventanaPrincipal;
    private final GestorMesa gestor;
    private final List<String> nombresJugadores;
    private PartidaControlador controlador;
    private JLabel saldos;
    private boolean resultadoPagado;
    private final Set<String> jugadoresAvisadosSinSaldo = new HashSet<>();

    public VentanaPartida(JFrame ventanaPrincipal, GestorMesa gestor,
            BarajaBlackjack baraja) {
        this.ventanaPrincipal = ventanaPrincipal;
        this.gestor = gestor;
        nombresJugadores = gestor.obtenerJugadores().stream()
                .map(Jugador::getNombre)
                .toList();
        configurarVentana(gestor, baraja);
        setVisible(true);
    }

    public void mostrarPartida() {
        saldos.setText(formatoSaldos(gestor));
        setVisible(true);
        toFront();
    }

    public boolean mismosJugadores(List<Jugador> jugadores) {
        return nombresJugadores.equals(jugadores.stream()
                .map(Jugador::getNombre)
                .toList());
    }

    private void configurarVentana(GestorMesa gestor, BarajaBlackjack baraja) {
        setTitle("Casino | Partida de Blackjack");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setJMenuBar(crearMenuSaldo(gestor));
        setContentPane(new FondoMesa());
        AnimacionReparto animacion = new AnimacionReparto(
                gestor.obtenerJugadores(), baraja,
                gestor.obtenerConfiguracion().getMultiplesApuesta(), gestor);
        controlador = new PartidaControlador(gestor);
        if (!controlador.abrir(gestor.obtenerConfiguracion())) {
            JOptionPane.showMessageDialog(VentanaPartida.this,
                    "La partida se ejecutará, pero no se pudo registrar en MySQL.",
                    "Persistencia no disponible", JOptionPane.WARNING_MESSAGE);
        }
        animacion.establecerEventos(controlador.eventos());
        add(animacion, BorderLayout.CENTER);

        JPanel barra = new JPanel(new BorderLayout(15, 0));
        barra.setOpaque(false);
        barra.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JLabel titulo = new JLabel("PARTIDA DE BLACKJACK");
        titulo.setForeground(Color.WHITE);
        titulo.setFont(new Font("SansSerif", Font.BOLD, 26));
        barra.add(titulo, BorderLayout.WEST);

        // El saldo de cada jugador se pinta debajo de su ficha en la mesa.
        saldos = new JLabel();

        JButton salir = new JButton("Volver a configuración");
        salir.setFocusable(false);
        salir.addActionListener(evento -> cerrarPartida());
        barra.add(salir, BorderLayout.EAST);
        add(barra, BorderLayout.NORTH);

        int[] jugadorSeleccionado = {0};
        PanelControlesPartida controles = new PanelControlesPartida(
                gestor.obtenerJugadores(), new PanelControlesPartida.Acciones() {
            @Override
            public void seleccionarJugador(int indice) {
                jugadorSeleccionado[0] = indice;
                animacion.seleccionarJugador(indice);
            }

            @Override
            public void comenzar() {
            if (!animacion.puedeComenzarPartida()) {
                JOptionPane.showMessageDialog(VentanaPartida.this,
                        "Todos los jugadores deben realizar una apuesta antes de comenzar.",
                        "Apuestas pendientes", JOptionPane.WARNING_MESSAGE);
            } else {
                int[] apuestas = new int[gestor.obtenerJugadores().size()];
                for (int indice = 0; indice < apuestas.length; indice++) {
                    apuestas[indice] = animacion.obtenerApuesta(indice);
                }
                if (!controlador.iniciarRonda(apuestas)) {
                    JOptionPane.showMessageDialog(VentanaPartida.this,
                            "No se pudo iniciar la ronda. Revisa las apuestas y los saldos.",
                            "Partida no iniciada",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
                saldos.setText(formatoSaldos(gestor));
                animacion.iniciarPartida();
            }
            }

            @Override
            public void pedir() {
                animacion.pedirCarta();
            }

            @Override
            public void plantarse() {
                animacion.plantarse();
            }

            @Override
            public void doblar() {
                int jugador = animacion.obtenerJugadorActivo();
                int mano = animacion.obtenerManoActiva();
                double apuesta = animacion.obtenerApuesta(jugador, mano);
                String nombre = gestor.obtenerJugadores().get(jugador).getNombre();
                if (gestor.obtenerSaldo(nombre) < apuesta) {
                    JOptionPane.showMessageDialog(VentanaPartida.this,
                            "El jugador no tiene saldo para doblar la apuesta.",
                            "Saldo insuficiente", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!gestor.modificarSaldo(nombre, -apuesta)) {
                    return;
                }
                if (!animacion.doblarApuesta()
                        || !animacion.registrarApuestaDoblada()) {
                    gestor.modificarSaldo(nombre, apuesta);
                    return;
                }
                animacion.actualizarFichaApuesta(jugador);
                controlador.registrarApuestaDoblada(jugador, mano,
                        animacion.obtenerApuesta(jugador, mano));
                controlador.registrarSaldo(nombre, gestor.obtenerSaldo(nombre));
                saldos.setText(formatoSaldos(gestor));
            }

            @Override
            public void separar() {
                int jugador = animacion.obtenerJugadorActivo();
                double apuesta = animacion.obtenerApuesta(jugador);
                String nombre = gestor.obtenerJugadores().get(jugador).getNombre();
                if (gestor.obtenerSaldo(nombre) < apuesta) {
                    JOptionPane.showMessageDialog(VentanaPartida.this,
                            "El jugador no tiene saldo para pagar la segunda mano.",
                            "Saldo insuficiente", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (!gestor.modificarSaldo(nombre, -apuesta)) {
                    return;
                }
                if (!animacion.separarCartas()
                        || !animacion.registrarApuestaSeparada()) {
                    gestor.modificarSaldo(nombre, apuesta);
                    return;
                }
                animacion.actualizarFichaApuesta(jugador);
                controlador.registrarSaldo(nombre, gestor.obtenerSaldo(nombre));
                saldos.setText(formatoSaldos(gestor));
            }

            @Override
            public void apostar() {
            int indiceJugador = jugadorSeleccionado[0];
            if (!puedeAgregarFicha(gestor, animacion, indiceJugador)) {
                JOptionPane.showMessageDialog(VentanaPartida.this,
                        "El jugador no tiene saldo suficiente para agregar otra ficha.",
                        "Saldo insuficiente", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (animacion.apostar()) {
                saldos.setText(formatoSaldos(gestor));
            }
            }

            @Override
            public void retirar() {
                animacion.retirarApuesta();
            }
        });
        add(controles, BorderLayout.SOUTH);
        javax.swing.Timer estadoControles = new javax.swing.Timer(100, evento -> {
            if (animacion.consumirNuevaRondaPendiente()) {
                resultadoPagado = false;
                controlador.prepararNuevaRonda();
                saldos.setText(formatoSaldos(gestor));
            }
            if (animacion.resultadoListo() && !resultadoPagado) {
                pagarResultado(animacion);
                saldos.setText(formatoSaldos(gestor));
                avisarJugadoresSinSaldo(gestor, saldos);
                resultadoPagado = true;
            }
            controles.actualizar(animacion.puedePedirCarta(),
                    animacion.puedePlantarse(), animacion.puedeDoblar(),
                    animacion.puedeSeparar(),
                    !animacion.isPartidaIniciada()
                    && puedeAgregarFicha(gestor, animacion,
                            jugadorSeleccionado[0]),
                    !animacion.isPartidaIniciada()
                    && animacion.obtenerApuestaActual() > 0,
                    animacion.puedeComenzarPartida());
            animacion.repaint();
        });
        estadoControles.start();

        getRootPane().getInputMap(
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cerrarPartida");
        getRootPane().getActionMap().put("cerrarPartida",
                new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evento) {
                cerrarPartida();
            }

        });
        getRootPane().getInputMap(
                javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "pedirDealer");
        getRootPane().getActionMap().put("pedirDealer",
                new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evento) {
                animacion.pedirCartaDealer();
            }
        });
    }

    private boolean puedeAgregarFicha(GestorMesa gestor,
            AnimacionReparto animacion, int indiceJugador) {
        if (indiceJugador < 0
                || indiceJugador >= gestor.obtenerJugadores().size()
                || animacion.isPartidaIniciada()) {
            return false;
        }
        String jugador = gestor.obtenerJugadores().get(indiceJugador).getNombre();
        double saldo = gestor.obtenerSaldo(jugador);
        double apuestaActual = animacion.obtenerApuesta(indiceJugador);
        double siguienteApuesta = apuestaActual
                + gestor.obtenerConfiguracion().getMultiplesApuesta();
        return saldo > 0 && saldo >= siguienteApuesta;
    }

    private void pagarResultado(AnimacionReparto animacion) {
        for (int indice = 0; indice < controlador.cantidadJugadores(); indice++) {
            for (int mano = 0; mano < animacion.cantidadManos(indice); mano++) {
                double apuesta = animacion.obtenerApuesta(indice, mano);
                double pago = apuesta * animacion.multiplicadorResultado(indice, mano);
                controlador.pagar(indice, pago);
            }
        }
    }

    private JMenuBar crearMenuSaldo(GestorMesa gestor) {
        JMenuBar barra = new JMenuBar();
        JMenu menuSaldo = new JMenu("Saldo");
        JMenuItem recargar = new JMenuItem("Recargar saldo");
        recargar.addActionListener(evento -> recargarSaldo(gestor));
        menuSaldo.add(recargar);
        barra.add(menuSaldo);
        return barra;
    }

    private void recargarSaldo(GestorMesa gestor) {
        if (gestor.obtenerJugadores().isEmpty()) {
            return;
        }
        String[] nombres = gestor.obtenerJugadores().stream()
                .map(proyectofinalcasino.modelo.Jugador::getNombre)
                .toArray(String[]::new);
        String jugador = (String) JOptionPane.showInputDialog(this,
                "Selecciona el jugador:", "Recargar saldo",
                JOptionPane.PLAIN_MESSAGE, null, nombres, nombres[0]);
        if (jugador == null) {
            return;
        }
        String texto = JOptionPane.showInputDialog(this,
                "Monto a recargar para " + jugador + ":",
                "Recargar saldo", JOptionPane.PLAIN_MESSAGE);
        if (texto == null) {
            return;
        }
        try {
            double monto = Double.parseDouble(texto.trim());
            if (!gestor.recargarSaldo(jugador, monto)) {
                JOptionPane.showMessageDialog(this, "Escribe un monto mayor que cero.",
                        "Recarga no válida", JOptionPane.WARNING_MESSAGE);
                return;
            }
            jugadoresAvisadosSinSaldo.remove(jugador);
            JOptionPane.showMessageDialog(this, "Saldo actualizado correctamente.",
                    "Recarga realizada", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "El monto debe ser un número válido.",
                    "Recarga no válida", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void avisarJugadoresSinSaldo(GestorMesa gestor, JLabel saldos) {
        for (proyectofinalcasino.modelo.Jugador jugador : gestor.obtenerJugadores()) {
            if (jugador.getSaldo() <= 0
                    && jugadoresAvisadosSinSaldo.add(jugador.getNombre())) {
                mostrarDialogoSaldoAgotado(gestor, jugador.getNombre(), saldos);
            }
        }
    }

    private void mostrarDialogoSaldoAgotado(GestorMesa gestor, String jugador,
            JLabel saldos) {
        JTextField campoMonto = new JTextField(10);
        JPanel contenido = new JPanel(new BorderLayout(8, 8));
        contenido.add(new JLabel(jugador + ", te quedaste sin saldo."),
                BorderLayout.NORTH);
        JPanel formulario = new JPanel(new BorderLayout(5, 0));
        formulario.add(new JLabel("Monto a recargar:"), BorderLayout.WEST);
        formulario.add(campoMonto, BorderLayout.CENTER);
        contenido.add(formulario, BorderLayout.CENTER);

        while (true) {
            int opcion = JOptionPane.showOptionDialog(this, contenido,
                    "Saldo agotado", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.WARNING_MESSAGE, null,
                    new Object[]{"Recargar saldo", "Después"},
                    "Recargar saldo");
            if (opcion != 0) {
                return;
            }
            try {
                double monto = Double.parseDouble(campoMonto.getText().trim());
                if (gestor.recargarSaldo(jugador, monto)) {
                    jugadoresAvisadosSinSaldo.remove(jugador);
                    saldos.setText(formatoSaldos(gestor));
                    JOptionPane.showMessageDialog(this,
                            "Saldo actualizado correctamente.",
                            "Recarga realizada",
                            JOptionPane.INFORMATION_MESSAGE);
                    return;
                }
            } catch (NumberFormatException exception) {
            }
            JOptionPane.showMessageDialog(this,
                    "Escribe un monto numérico mayor que cero.",
                    "Recarga no válida", JOptionPane.WARNING_MESSAGE);
            campoMonto.requestFocusInWindow();
        }
    }

    private String formatoSaldos(GestorMesa gestor) {
        StringBuilder texto = new StringBuilder("Saldos: ");
        for (int indice = 0; indice < gestor.obtenerJugadores().size(); indice++) {
            if (indice > 0) {
                texto.append("  |  ");
            }
            proyectofinalcasino.modelo.Jugador jugador =
                    gestor.obtenerJugadores().get(indice);
            texto.append(jugador.getNombre()).append(": $")
                    .append(String.format("%.2f", jugador.getSaldo()));
        }
        return texto.toString();
    }

    private void cerrarPartida() {
        setVisible(false);
        if (ventanaPrincipal instanceof VentanaPrincipal principal) {
            principal.refrescarJugadores();
        }
        ventanaPrincipal.setVisible(true);
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private static final class FondoMesa extends JPanel {

        private final Image imagen;

        private FondoMesa() {
            setLayout(new BorderLayout());
            Image imagenCargada;
            try {
                URL recurso = FondoMesa.class.getResource(
                        "/proyectofinalcasino/imagen/Minimalist Poker Table Layout.png");
                if (recurso != null) {
                    imagenCargada = ImageIO.read(recurso);
                } else {
                    imagenCargada = ImageIO.read(new File(
                            "src/proyectofinalcasino/imagen/Minimalist Poker Table Layout.png"));
                }
                if (imagenCargada == null) {
                    imagenCargada = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
                }
            } catch (IOException exception) {
                imagenCargada = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            }
            imagen = imagenCargada;
        }

        @Override
        protected void paintComponent(Graphics graficos) {
            super.paintComponent(graficos);
            Graphics2D graficos2D = (Graphics2D) graficos.create();
            graficos2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graficos2D.drawImage(imagen, 0, 0, getWidth(), getHeight(), this);
            graficos2D.dispose();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(1365, 768);
        }
    }
}
