package proyectofinalcasino.interfaz;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;
import proyectofinalcasino.funciones.BarajaBlackjack;
import proyectofinalcasino.funciones.GestorApuestas;
import proyectofinalcasino.funciones.GestorCartas;
import proyectofinalcasino.funciones.PartidaEventoListener;
import proyectofinalcasino.modelo.Jugador;

/**
 * Anima el reparto respetando el orden de la baraja mezclada.
 */
public final class AnimacionReparto extends JPanel {

    private static final int CARTA_ANCHO = 74;
    private static final int CARTA_ALTO = 106;
    private static final double DESPLAZAMIENTO_CARTA = 0.20;
    private static final double DESPLAZAMIENTO_FICHA_IZQUIERDA = 0.29;
    private static final int MARGEN_MESA = 40;
    private static final int DURACION_MOVIMIENTO = 15;
    private static final int PAUSA_ENTRE_CARTAS = 8;
    private static final int SEPARACION_MANOS = 150;

    private final List<Jugador> jugadores;
    private final GestorApuestas gestorApuestas;
    private final GestorCartas gestorCartas = new GestorCartas();
    private final FichaCasinoPanel[] fichas;
    private final Deque<CartaReparto> pendientes = new ArrayDeque<>();
    private final List<CartaReparto> repartidas = new ArrayList<>();
    private final Timer reloj;
    private List<String> cartasBaraja;
    private CartaReparto cartaActual;
    private int indiceBaraja;
    private int pausa;
    private int progreso;
    private boolean finalizada;
    private boolean turnoFinalizado;
    private boolean turnoDealer;
    private boolean dealerRevelado;
    private boolean doblado;
    private boolean separado;
    private boolean animandoSeparacion;
    private boolean partidaIniciada;
    private int jugadorActivo;
    private int manoActiva;
    private final boolean[] jugadoresFinalizados;
    private final boolean[] manosFinalizadas = new boolean[2];
    private final boolean[] manosDobladas = new boolean[2];
    private String resultado = "";
    private final String[] resultadosJugadores;
    private boolean nuevaRondaPendiente;
    private PartidaEventoListener eventos = new PartidaEventoListener() {
        @Override
        public void cartaRepartida(int jugador, int mano, String carta,
                boolean dealer) {
        }

        @Override
        public void turnoIniciado(int jugador, int mano, boolean dealer) {
        }

        @Override
        public void decisionTomada(int jugador, int mano, String decision,
                double valorAdicional) {
        }

        @Override
        public void partidaFinalizada(String[] resultados, int[] puntuaciones) {
        }
    };

    public AnimacionReparto(List<Jugador> jugadores, BarajaBlackjack baraja,
            int multiploApuesta) {
        this.jugadores = List.copyOf(jugadores);
        gestorApuestas = new GestorApuestas(jugadores.size(), multiploApuesta);
        fichas = new FichaCasinoPanel[jugadores.size()];
        jugadoresFinalizados = new boolean[jugadores.size()];
        resultadosJugadores = new String[jugadores.size()];
        cartasBaraja = baraja.obtenerCartas();
        crearCola(cartasBaraja);
        setOpaque(false);
        setLayout(null);
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evento) {
                actualizarPosicionesFichas();
            }
        });
        reloj = new Timer(35, evento -> actualizar());
    }

    public boolean isPartidaIniciada() {
        return partidaIniciada;
    }

    public void establecerEventos(PartidaEventoListener eventos) {
        if (eventos != null) {
            this.eventos = eventos;
        }
    }

    public boolean puedeComenzarPartida() {
        if (partidaIniciada || jugadores.isEmpty()) {
            return false;
        }
        return gestorApuestas.todosApostaron();
    }

    public boolean iniciarPartida() {
        if (!puedeComenzarPartida()) {
            return false;
        }
        partidaIniciada = true;
        jugadorActivo = 0;
        java.util.Arrays.fill(jugadoresFinalizados, false);
        reloj.start();
        repaint();
        return true;
    }

    public boolean apostar() {
        if (partidaIniciada || turnoDealer || turnoFinalizado || jugadores.isEmpty()) {
            return false;
        }
        int jugador = jugadorActivo;
        if (gestorApuestas.obtener(jugador) == 0) {
            FichaCasinoPanel ficha = new FichaCasinoPanel(gestorApuestas.obtenerMultiplo());
            fichas[jugador] = ficha;
            add(ficha);
        }
        if (!gestorApuestas.agregarFicha(jugador, partidaIniciada)) {
            return false;
        }
        fichas[jugador].setValorApuesta(gestorApuestas.obtener(jugador));
        actualizarPosicionesFichas();
        repaint();
        return true;
    }

    public boolean retirarApuesta() {
        if (partidaIniciada || turnoDealer || turnoFinalizado || jugadores.isEmpty()) {
            return false;
        }
        int jugador = jugadorActivo;
        if (!gestorApuestas.retirarFicha(jugador, partidaIniciada)) {
            return false;
        }
        if (gestorApuestas.obtener(jugador) == 0 && fichas[jugador] != null) {
            remove(fichas[jugador]);
            fichas[jugador] = null;
        } else if (fichas[jugador] != null) {
            fichas[jugador].setValorApuesta(gestorApuestas.obtener(jugador));
        }
        actualizarPosicionesFichas();
        repaint();
        return true;
    }

    public int obtenerApuestaActual() {
        return gestorApuestas.obtener(jugadorActivo);
    }

    public void seleccionarJugador(int indice) {
        if (indice >= 0 && indice < jugadores.size()
                && (!partidaIniciada || !jugadoresFinalizados[indice])) {
            jugadorActivo = indice;
            manoActiva = 0;
        }
    }

    public int obtenerJugadorActivo() {
        return jugadorActivo;
    }

    public String obtenerTextoTurno() {
        if (!partidaIniciada) {
            return "Esperando apuestas";
        }
        if (turnoDealer) {
            return "Turno del dealer";
        }
        if (!finalizada) {
            return "Repartiendo cartas...";
        }
        if (!resultado.isEmpty()) {
            return "Ronda finalizada";
        }
        return "Turno de " + jugadores.get(jugadorActivo).getNombre();
    }

    public int obtenerApuesta(int jugador) {
        if (jugador < 0 || jugador >= gestorApuestas.cantidad()) {
            return 0;
        }
        return gestorApuestas.obtener(jugador);
    }

    private void actualizarPosicionesFichas() {
        for (int jugador = 0; jugador < fichas.length; jugador++) {
            if (fichas[jugador] == null) {
                continue;
            }
            int x = calcularPosicionHorizontal(jugador, false, 0)
                    + (CARTA_ANCHO - FichaCasinoPanel.TAMANO_COMPONENTE) / 2
                    - (int) (CARTA_ANCHO * DESPLAZAMIENTO_FICHA_IZQUIERDA);
            int y = getHeight() - 205 + CARTA_ALTO
                    + (int) (CARTA_ALTO * 0.40);
            int limiteInferior = getHeight() - FichaCasinoPanel.TAMANO_COMPONENTE;
            if (y > limiteInferior) {
                y = limiteInferior;
            }
            if (y < 0) {
                y = 0;
            }
            fichas[jugador].setBounds(x, y,
                    FichaCasinoPanel.TAMANO_COMPONENTE,
                    FichaCasinoPanel.TAMANO_COMPONENTE);
        }
        revalidate();
        repaint();
    }

    private void crearCola(List<String> cartas) {
        int indiceCarta = 0;
        for (int ronda = 0; ronda < 2; ronda++) {
            for (int jugador = 0; jugador < jugadores.size(); jugador++) {
                pendientes.add(new CartaReparto(cartas.get(indiceCarta++),
                        jugador, false, ronda));
            }
            pendientes.add(new CartaReparto(cartas.get(indiceCarta++), -1, true, ronda));
        }
        indiceBaraja = indiceCarta;
    }

    private void actualizar() {
        if (animandoSeparacion) {
            actualizarSeparacion();
            return;
        }
        if (pausa > 0) {
            pausa--;
            repaint();
            return;
        }
        if (cartaActual == null) {
            cartaActual = pendientes.pollFirst();
            progreso = 0;
            if (cartaActual == null) {
                if (separado && manosFinalizadas[manoActiva] && manoActiva == 0) {
                    manoActiva = 1;
                    manosFinalizadas[manoActiva] = false;
                } else {
                    finalizada = true;
                }
                reloj.stop();
                if (finalizada && !turnoDealer && jugadoresFinalizados[jugadorActivo]
                        == false) {
                    eventos.turnoIniciado(jugadorActivo, manoActiva, false);
                }
                procesarFinDeReparto();
                repaint();
                return;
            }

            cartaActual.configurarDestino();
        }
        progreso++;
        if (progreso >= DURACION_MOVIMIENTO) {
            cartaActual.x = cartaActual.destinoX;
            cartaActual.y = cartaActual.destinoY;
            repartidas.add(cartaActual);
            eventos.cartaRepartida(cartaActual.jugador, cartaActual.mano,
                    cartaActual.valor, cartaActual.dealer);
            cartaActual = null;
            pausa = PAUSA_ENTRE_CARTAS;
        } else {
            cartaActual.x = interpolar(cartaActual.inicioX, cartaActual.destinoX, progreso);
            cartaActual.y = interpolar(cartaActual.inicioY, cartaActual.destinoY, progreso);
        }
        repaint();
    }

    public boolean puedePedirCarta() {
        return partidaIniciada && finalizada && !turnoFinalizado
                && !jugadoresFinalizados[jugadorActivo]
                && !manosFinalizadas[manoActiva]
                && !tieneBlackjack(manoActiva) && !manoSuperaVeintiuno(manoActiva)
                && indiceBaraja < cartasBaraja.size();
    }

    public boolean pedirCarta() {
        if (!puedePedirCarta()) {
            return false;
        }
        eventos.decisionTomada(jugadorActivo, manoActiva, "Pedir", 0);
        agregarCarta(manoActiva);
        iniciarAnimacion();
        return true;
    }

    public boolean plantarse() {
        if (!finalizada || turnoFinalizado || jugadoresFinalizados[jugadorActivo]
                || manosFinalizadas[manoActiva]) {
            return false;
        }
        eventos.decisionTomada(jugadorActivo, manoActiva, "Plantarse", 0);
        finalizarManoActiva();
        repaint();
        return true;
    }

    public boolean puedePlantarse() {
        return partidaIniciada && finalizada && !turnoFinalizado
                && !jugadoresFinalizados[jugadorActivo]
                && !manosFinalizadas[manoActiva];
    }

    public boolean puedePedirDealer() {
        return partidaIniciada && finalizada && turnoDealer && dealerRevelado
                && indiceBaraja < cartasBaraja.size();
    }

    public boolean pedirCartaDealer() {
        if (!puedePedirDealer()) {
            return false;
        }
        if (obtenerValorDealer() >= 17) {
            finalizarResultado();
            return false;
        }
        pendientes.add(new CartaReparto(cartasBaraja.get(indiceBaraja++),
                -1, true, 2));
        iniciarAnimacion();
        return true;
    }

    public String obtenerResultado() {
        return resultado;
    }

    public String obtenerResultado(int jugador) {
        if (jugador < 0 || jugador >= resultadosJugadores.length) {
            return "";
        }
        return resultadosJugadores[jugador] == null
                ? "" : resultadosJugadores[jugador];
    }

    public boolean resultadoListo() {
        return !resultado.isEmpty();
    }

    public double multiplicadorResultado() {
        return multiplicadorResultado(jugadorActivo);
    }

    public double multiplicadorResultado(int jugador) {
        String resultadoJugador = obtenerResultado(jugador);
        if (resultadoJugador.contains("Empate")) {
            return 1.0;
        }
        if (resultadoJugador.contains("Blackjack")) {
            return 2.5;
        }
        return resultadoJugador.contains("Ganaste") ? 2.0 : 0.0;
    }

    private void iniciarTurnoDealer() {
        dealerRevelado = true;
        turnoDealer = true;
        eventos.turnoIniciado(-1, 0, true);
        if (obtenerValorJugador() > 21) {
            finalizarResultado();
        }
        repaint();
    }

    private void procesarFinDeReparto() {
        if (!partidaIniciada || turnoDealer || !finalizada) {
            return;
        }
        if (manosFinalizadas[manoActiva]
                || tieneBlackjack(manoActiva)
                || manoSuperaVeintiuno(manoActiva)) {
            finalizarManoActiva();
        }
    }

    public boolean puedeDoblar() {
        return partidaIniciada && finalizada && !turnoFinalizado
                && !jugadoresFinalizados[jugadorActivo]
                && !manosDobladas[manoActiva]
                && cartasJugador(jugadorActivo, manoActiva).size() == 2
                && indiceBaraja < cartasBaraja.size();
    }

    public boolean doblarApuesta() {
        if (!puedeDoblar()) {
            return false;
        }
        eventos.decisionTomada(jugadorActivo, manoActiva, "Doblar",
                gestorApuestas.obtener(jugadorActivo));
        manosDobladas[manoActiva] = true;
        manosFinalizadas[manoActiva] = true;
        agregarCarta(manoActiva);
        iniciarAnimacion();
        return true;
    }

    private void finalizarManoActiva() {
        manosFinalizadas[manoActiva] = true;
        if (separado && manoActiva == 0) {
            manoActiva = 1;
            manosFinalizadas[manoActiva] = false;
            repaint();
            return;
        }
        jugadoresFinalizados[jugadorActivo] = true;
        int siguiente = siguienteJugadorActivo();
        if (siguiente >= 0) {
            jugadorActivo = siguiente;
            manoActiva = 0;
            manosFinalizadas[0] = false;
            manosFinalizadas[1] = false;
            manosDobladas[0] = false;
            manosDobladas[1] = false;
            eventos.turnoIniciado(jugadorActivo, manoActiva, false);
            repaint();
            return;
        }
        turnoFinalizado = true;
        iniciarTurnoDealer();
    }

    private int siguienteJugadorActivo() {
        for (int indice = jugadorActivo + 1; indice < jugadores.size(); indice++) {
            if (!jugadoresFinalizados[indice]) {
                return indice;
            }
        }
        for (int indice = 0; indice < jugadorActivo; indice++) {
            if (!jugadoresFinalizados[indice]) {
                return indice;
            }
        }
        return -1;
    }

    public boolean puedeSeparar() {
        return partidaIniciada && finalizada && !turnoFinalizado && !separado
                && gestorCartas.puedeSeparar(obtenerCartasJugador(jugadorActivo, manoActiva))
                && indiceBaraja + 1 < cartasBaraja.size();
    }

    public boolean separarCartas() {
        if (!puedeSeparar()) {
            return false;
        }
        eventos.decisionTomada(jugadorActivo, manoActiva, "Separar", 0);
        separado = true;
        CartaReparto segunda = cartasJugador(jugadorActivo, 0).get(1);
        segunda.mano = 1;
        segunda.posicionEnMano = 0;
        animandoSeparacion = true;
        progreso = 0;
        reloj.start();
        return true;
    }

    private void actualizarSeparacion() {
        progreso++;
        CartaReparto primera = cartasJugador(jugadorActivo, 0).get(0);
        CartaReparto segunda = cartasJugador(jugadorActivo, 1).get(0);
        int destinoPrimera = calcularPosicionMano(0, 0);
        int destinoSegunda = calcularPosicionMano(0, 1);
        primera.x = interpolar(primera.x, destinoPrimera, progreso);
        primera.y = interpolar(primera.y, getHeight() - 205, progreso);
        segunda.x = interpolar(segunda.x, destinoSegunda, progreso);
        segunda.y = interpolar(segunda.y, getHeight() - 205, progreso);
        if (progreso >= DURACION_MOVIMIENTO) {
            primera.x = destinoPrimera;
            segunda.x = destinoSegunda;
            animandoSeparacion = false;
            agregarCarta(0);
            agregarCarta(1);
            iniciarAnimacion();
        }
        repaint();
    }

    private void iniciarAnimacion() {
        finalizada = false;
        pausa = 0;
        reloj.start();
    }

    private void agregarCarta(int mano) {
        int posicion = cartasJugador(jugadorActivo, mano).size();
        CartaReparto carta = new CartaReparto(cartasBaraja.get(indiceBaraja++),
                jugadorActivo, false, 2, mano, posicion);
        pendientes.add(carta);
    }

    private List<CartaReparto> cartasJugador(int jugador, int mano) {
        List<CartaReparto> resultado = new ArrayList<>();
        for (CartaReparto carta : repartidas) {
            if (!carta.dealer && carta.jugador == jugador && carta.mano == mano) {
                resultado.add(carta);
            }
        }
        return resultado;
    }

    private boolean tieneBlackjack(int mano) {
        return gestorCartas.tieneBlackjack(obtenerCartasJugador(mano));
    }

    private int obtenerValorJugador() {
        return gestorCartas.calcularValor(obtenerCartasJugador());
    }

    private int obtenerValorDealer() {
        return gestorCartas.calcularValor(obtenerCartasDealer());
    }

    private void finalizarResultado() {
        List<String> cartasDealer = obtenerCartasDealer();
        for (int jugador = 0; jugador < jugadores.size(); jugador++) {
            resultadosJugadores[jugador] = gestorCartas.resolver(
                    obtenerCartasJugador(jugador, 0), cartasDealer);
        }
        resultado = resultadosJugadores.length == 0 ? "" : resultadosJugadores[0];
        int[] puntuaciones = new int[jugadores.size()];
        for (int indice = 0; indice < puntuaciones.length; indice++) {
            puntuaciones[indice] = gestorCartas.calcularValor(
                    obtenerCartasJugador(indice, 0));
        }
        eventos.partidaFinalizada(resultadosJugadores.clone(), puntuaciones);
        turnoDealer = false;
        turnoFinalizado = true;
        Timer siguienteRonda = new Timer(3500, evento -> reiniciarRonda());
        siguienteRonda.setRepeats(false);
        siguienteRonda.start();
        repaint();
    }

    private void reiniciarRonda() {
        reloj.stop();
        pendientes.clear();
        repartidas.clear();
        cartaActual = null;
        indiceBaraja = 0;
        pausa = 0;
        progreso = 0;
        finalizada = false;
        turnoFinalizado = false;
        turnoDealer = false;
        dealerRevelado = false;
        doblado = false;
        separado = false;
        animandoSeparacion = false;
        partidaIniciada = false;
        manoActiva = 0;
        manosFinalizadas[0] = false;
        manosFinalizadas[1] = false;
        manosDobladas[0] = false;
        manosDobladas[1] = false;
        resultado = "";
        java.util.Arrays.fill(resultadosJugadores, "");
        java.util.Arrays.fill(jugadoresFinalizados, false);
        jugadorActivo = 0;
        gestorApuestas.reiniciar();
        for (int indice = 0; indice < fichas.length; indice++) {
            if (fichas[indice] != null) {
                remove(fichas[indice]);
                fichas[indice] = null;
            }
        }
        int cantidadBarajas = Math.max(1, cartasBaraja.size() / 52);
        cartasBaraja = new BarajaBlackjack(cantidadBarajas).obtenerCartas();
        crearCola(cartasBaraja);
        nuevaRondaPendiente = true;
        revalidate();
        repaint();
    }

    public boolean consumirNuevaRondaPendiente() {
        boolean pendiente = nuevaRondaPendiente;
        nuevaRondaPendiente = false;
        return pendiente;
    }

    private boolean manoSuperaVeintiuno(int mano) {
        return gestorCartas.superaVeintiuno(obtenerCartasJugador(mano));
    }

    private List<String> obtenerCartasJugador() {
        return obtenerCartasJugador(jugadorActivo);
    }

    private List<String> obtenerCartasJugador(int mano) {
        return obtenerCartasJugador(jugadorActivo, mano);
    }

    private List<String> obtenerCartasJugador(int jugador, int mano) {
        List<String> cartas = new ArrayList<>();
        for (CartaReparto carta : cartasJugador(jugador, mano)) {
            cartas.add(carta.valor);
        }
        return cartas;
    }

    private List<String> obtenerCartasDealer() {
        List<String> cartas = new ArrayList<>();
        for (CartaReparto carta : repartidas) {
            if (carta.dealer) {
                cartas.add(carta.valor);
            }
        }
        return cartas;
    }

    private int interpolar(int inicio, int destino, int paso) {
        double avance = paso / (double) DURACION_MOVIMIENTO;
        return (int) (inicio + (destino - inicio) * avance);
    }

    @Override
    protected void paintComponent(Graphics graficos) {
        super.paintComponent(graficos);
        Graphics2D g = (Graphics2D) graficos.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        for (CartaReparto carta : repartidas) {
            pintarCarta(g, carta);
        }
        if (cartaActual != null) {
            pintarCarta(g, cartaActual);
        }
        pintarIndicadorTurno(g);
        pintarValores(g);
        if (finalizada && resultado.isEmpty()) {
            g.setColor(new Color(255, 255, 255, 210));
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            g.drawString("Reparto completado", 30, getHeight() - 35);
        }
        g.dispose();
    }

    private void pintarIndicadorTurno(Graphics2D g) {
        String texto = obtenerTextoTurno();
        Color color = texto.startsWith("Turno de ")
                ? new Color(255, 210, 70)
                : texto.equals("Turno del dealer")
                ? new Color(120, 210, 255)
                : new Color(220, 230, 240);

        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        int anchoTexto = g.getFontMetrics().stringWidth(texto);
        int xTexto = (getWidth() - anchoTexto) / 2;
        int yTexto = 42;
        g.setColor(new Color(0, 0, 0, 175));
        g.fillRoundRect(xTexto - 18, yTexto - 27, anchoTexto + 36, 38, 18, 18);
        g.setColor(color);
        g.drawString(texto, xTexto, yTexto);

        if (!partidaIniciada || turnoDealer || !finalizada
                || !resultado.isEmpty() || jugadores.isEmpty()) {
            return;
        }
        int x = calcularPosicionMano(jugadorActivo, 0) - 12;
        int y = getHeight() - 217;
        int ancho = CARTA_ANCHO + 24;
        g.setColor(new Color(255, 210, 70, 55));
        g.fillRoundRect(x, y, ancho, CARTA_ALTO + 24, 18, 18);
        g.setColor(color);
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(x, y, ancho, CARTA_ALTO + 24, 18, 18);
    }

    private void pintarValores(Graphics2D g) {
        g.setFont(new Font("SansSerif", Font.BOLD, 16));
        for (int jugador = 0; jugador < jugadores.size(); jugador++) {
            int manos = separado && jugador == 0 ? 2 : 1;
            for (int mano = 0; mano < manos; mano++) {
                List<String> cartasJugador = obtenerCartasJugador(jugador, mano);
                if (!cartasJugador.isEmpty()) {
                    int x = calcularPosicionMano(jugador, mano);
                    int y = getHeight() - 205 - (int) (CARTA_ALTO * 0.10);
                    pintarEtiquetaValor(g, "Valor: "
                            + calcularValorTexto(cartasJugador), x, y);
                    if (mano == 0 && !obtenerResultado(jugador).isEmpty()) {
                        pintarResultadoJugador(g, obtenerResultado(jugador),
                                x, y - 25);
                    }
                }
            }
        }

        List<String> cartasDealer = new ArrayList<>();
        for (CartaReparto carta : repartidas) {
            if (carta.dealer && !carta.estaOculta()) {
                cartasDealer.add(carta.valor);
            }
        }
        if (!cartasDealer.isEmpty()) {
            int x = calcularPosicionHorizontal(-1, true, 0);
            int y = 145 - (int) (CARTA_ALTO * 0.10);
            pintarEtiquetaValor(g, "Dealer: " + calcularValorTexto(cartasDealer), x, y);
        }
    }

    private void pintarEtiquetaValor(Graphics2D g, String texto, int x, int y) {
        g.setColor(new Color(0, 0, 0, 160));
        g.drawString(texto, x + 1, y + 1);
        g.setColor(Color.WHITE);
        g.drawString(texto, x, y);
    }

    private void pintarResultadoJugador(Graphics2D g, String texto, int x, int y) {
        Color color = texto.contains("Ganaste") || texto.contains("Blackjack")
                ? new Color(90, 255, 130)
                : texto.contains("Empate") ? new Color(255, 220, 90)
                : new Color(255, 120, 120);
        g.setFont(new Font("SansSerif", Font.BOLD, 15));
        g.setColor(new Color(0, 0, 0, 190));
        g.drawString(texto, x + 1, y + 1);
        g.setColor(color);
        g.drawString(texto, x, y);
    }

    private String calcularValorTexto(List<String> cartas) {
        return gestorCartas.valorVisible(cartas);
    }

    private int calcularPosicionHorizontal(int jugador, boolean esDealer, int ronda) {
        int desplazamientoX = (int) (CARTA_ANCHO * (DESPLAZAMIENTO_CARTA * 1.5));
        if (esDealer) {
            int anchoManoDealer = CARTA_ANCHO + desplazamientoX;
            return getWidth() / 2 - anchoManoDealer / 2 + ronda * desplazamientoX;
        }

        int anchoMano = CARTA_ANCHO + desplazamientoX;
        int espacioDisponible = Math.max(0, getWidth() - MARGEN_MESA * 2);
        int separacionJugadores = jugadores.size() == 1
                ? anchoMano
                : Math.max(anchoMano,
                        (espacioDisponible - anchoMano) / (jugadores.size() - 1));
        int anchoTotal = anchoMano
                + separacionJugadores * (jugadores.size() - 1);
        int inicio = (getWidth() - anchoTotal) / 2;
        return inicio + jugador * separacionJugadores + ronda * desplazamientoX;
    }

    private int calcularPosicionMano(int jugador, int mano) {
        int base = calcularPosicionHorizontal(jugador, false, 0);
        if (!separado || jugador != 0) {
            return base;
        }
        return base + (mano == 0 ? -SEPARACION_MANOS / 2 : SEPARACION_MANOS / 2);
    }

    private void pintarCarta(Graphics2D g, CartaReparto carta) {
        if (carta.estaOculta()
                || (cartaActual == carta && progreso < DURACION_MOVIMIENTO / 2)) {
            pintarReverso(g, carta.x, carta.y);
        } else {
            pintarFrente(g, carta.x, carta.y, carta.valor);
        }
    }

    private void pintarReverso(Graphics2D g, int x, int y) {
        g.setColor(new Color(0, 0, 0, 80));
        g.fillRoundRect(x + 3, y + 3, CARTA_ANCHO, CARTA_ALTO, 12, 12);
        g.setColor(new Color(180, 0, 0));
        g.fillRoundRect(x, y, CARTA_ANCHO, CARTA_ALTO, 12, 12);
        g.setColor(Color.WHITE);
        g.drawRoundRect(x, y, CARTA_ANCHO, CARTA_ALTO, 12, 12);
        g.drawRect(x + 6, y + 6, CARTA_ANCHO - 12, CARTA_ALTO - 12);
    }

    private void pintarFrente(Graphics2D g, int x, int y, String carta) {
        String valor = carta.substring(0, carta.indexOf(" de "));
        String palo = obtenerSimboloPalo(carta);
        Color colorPalo = palo.equals("♥") || palo.equals("♦")
                ? new Color(200, 0, 0) : Color.BLACK;
        RoundRectangle2D forma = new RoundRectangle2D.Double(
                x, y, CARTA_ANCHO, CARTA_ALTO, 12, 12);

        g.setColor(new Color(0, 0, 0, 80));
        g.fillRoundRect(x + 3, y + 3, CARTA_ANCHO, CARTA_ALTO, 12, 12);
        g.setColor(Color.WHITE);
        g.fill(forma);
        g.setColor(new Color(180, 180, 180));
        g.setStroke(new BasicStroke(1.5f));
        g.draw(forma);
        g.setColor(colorPalo);

        g.setFont(new Font("Serif", Font.BOLD, 15));
        g.drawString(valor, x + 8, y + 20);
        g.setFont(new Font("Serif", Font.BOLD, 14));
        g.drawString(palo, x + 9, y + 36);
        g.setFont(new Font("Serif", Font.BOLD, 36));
        g.drawString(palo, x + CARTA_ANCHO / 2 - 12, y + CARTA_ALTO / 2 + 12);
        g.setFont(new Font("Serif", Font.BOLD, 14));
        g.drawString(palo, x + CARTA_ANCHO - 19, y + CARTA_ALTO - 25);
        g.setFont(new Font("Serif", Font.BOLD, 15));
        g.drawString(valor, x + CARTA_ANCHO - 19, y + CARTA_ALTO - 10);
    }

    private String obtenerSimboloPalo(String carta) {
        if (carta.contains("Corazones")) {
            return "♥";
        }
        if (carta.contains("Diamantes")) {
            return "♦";
        }
        if (carta.contains("Treboles")) {
            return "♣";
        }
        return "♠";
    }

    private final class CartaReparto {

        private final String valor;
        private final int jugador;
        private final boolean dealer;
        private final int ronda;
        private int mano;
        private int posicionEnMano;
        private int x;
        private int y;
        private int inicioX;
        private int inicioY;
        private int destinoX;
        private int destinoY;

        private CartaReparto(String valor, int jugador, boolean dealer, int ronda) {
            this(valor, jugador, dealer, ronda, 0, ronda);
        }

        private CartaReparto(String valor, int jugador, boolean dealer,
                int ronda, int mano) {
            this(valor, jugador, dealer, ronda, mano, 0);
        }

        private CartaReparto(String valor, int jugador, boolean dealer,
                int ronda, int mano, int posicionEnMano) {
            this.valor = valor;
            this.jugador = jugador;
            this.dealer = dealer;
            this.ronda = ronda;
            this.mano = mano;
            this.posicionEnMano = posicionEnMano;
            inicioX = 100;
            inicioY = 100;
            x = inicioX;
            y = inicioY;
        }

        private void configurarDestino() {
            destinoX = dealer ? calcularDestinoX(jugador, true, ronda)
                    : calcularDestinoMano(mano)
                    + posicionEnMano * (int) (CARTA_ANCHO * DESPLAZAMIENTO_CARTA);
            destinoY = dealer ? calcularDestinoY(true, ronda)
                    : calcularDestinoY(false, posicionEnMano);
        }

        private int calcularDestinoMano(int mano) {
            return calcularPosicionMano(jugador, mano);
        }

        private boolean estaOculta() {
            return dealer && ronda == 1 && !dealerRevelado;
        }

        private int calcularDestinoX(int jugador, boolean esDealer, int ronda) {
            return calcularPosicionHorizontal(jugador, esDealer, ronda);
        }

        private int calcularDestinoY(boolean esDealer, int ronda) {
            int desplazamientoY = (int) (CARTA_ALTO * DESPLAZAMIENTO_CARTA);
            return (esDealer ? 145 : getHeight() - 205) + ronda * desplazamientoY;
        }
    }
}
