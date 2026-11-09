package proyectofinalcasino.interfaz;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

/**
 * Ficha visual de apuesta cuyo color depende de su denominación.
 */
public final class FichaCasinoPanel extends JPanel {

    public static final int TAMANO_COMPONENTE = 84;
    private static final int DIAMETRO = 76;
    private int valorApuesta;
    private Color colorPrincipal;
    private Color colorOscuro;
    private Color colorAnillo;
    private Color colorCentro;

    public FichaCasinoPanel(int valorApuesta) {
        this.valorApuesta = valorApuesta;
        setPreferredSize(new Dimension(TAMANO_COMPONENTE, TAMANO_COMPONENTE));
        setOpaque(false);
        actualizarColoresPorValor(valorApuesta);
    }

    public void setValorApuesta(int valorApuesta) {
        this.valorApuesta = valorApuesta;
        actualizarColoresPorValor(valorApuesta);
        repaint();
    }

    private void actualizarColoresPorValor(int valor) {
        if (valor <= 10) {
            colorPrincipal = new Color(215, 38, 38);
            colorOscuro = new Color(150, 20, 20);
            colorAnillo = new Color(180, 25, 25);
            colorCentro = new Color(205, 30, 30);
        } else if (valor <= 50) {
            colorPrincipal = new Color(38, 100, 215);
            colorOscuro = new Color(20, 60, 150);
            colorAnillo = new Color(25, 80, 180);
            colorCentro = new Color(30, 90, 205);
        } else if (valor <= 100) {
            colorPrincipal = new Color(38, 160, 60);
            colorOscuro = new Color(20, 110, 35);
            colorAnillo = new Color(25, 135, 45);
            colorCentro = new Color(30, 145, 50);
        } else {
            colorPrincipal = new Color(50, 50, 50);
            colorOscuro = new Color(20, 20, 20);
            colorAnillo = new Color(35, 35, 35);
            colorCentro = new Color(42, 42, 42);
        }
    }

    @Override
    protected void paintComponent(Graphics graficos) {
        super.paintComponent(graficos);
        Graphics2D g = (Graphics2D) graficos.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int x = (TAMANO_COMPONENTE - DIAMETRO) / 2;
        int y = (TAMANO_COMPONENTE - DIAMETRO) / 2;
        int diametro = DIAMETRO;
        g.setColor(colorPrincipal);
        g.fillOval(x, y, diametro, diametro);
        g.setColor(colorOscuro);
        g.drawOval(x, y, diametro, diametro);

        Stroke original = g.getStroke();
        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(8));
        int margen = 4;
        int diametroBorde = diametro - margen * 2;
        for (int angulo = 20; angulo < 360; angulo += 90) {
            g.drawArc(x + margen, y + margen, diametroBorde, diametroBorde,
                    angulo, 30);
        }
        g.setStroke(original);

        int diametroAnillo = (int) (diametro * 0.75);
        int xAnillo = x + (diametro - diametroAnillo) / 2;
        int yAnillo = y + (diametro - diametroAnillo) / 2;
        g.setColor(colorAnillo);
        g.fillOval(xAnillo, yAnillo, diametroAnillo, diametroAnillo);
        g.setColor(colorOscuro);
        g.drawOval(xAnillo, yAnillo, diametroAnillo, diametroAnillo);

        float[] puntos = {6f, 6f};
        g.setColor(new Color(240, 240, 240, 180));
        g.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 10, puntos, 0));
        int diametroPuntos = (int) (diametro * 0.62);
        int xPuntos = x + (diametro - diametroPuntos) / 2;
        int yPuntos = y + (diametro - diametroPuntos) / 2;
        g.drawOval(xPuntos, yPuntos, diametroPuntos, diametroPuntos);
        g.setStroke(original);

        int diametroCentro = (int) (diametro * 0.50);
        int xCentro = x + (diametro - diametroCentro) / 2;
        int yCentro = y + (diametro - diametroCentro) / 2;
        g.setColor(colorCentro);
        g.fillOval(xCentro, yCentro, diametroCentro, diametroCentro);
        g.setColor(colorOscuro);
        g.drawOval(xCentro, yCentro, diametroCentro, diametroCentro);

        g.setFont(new Font("Arial", Font.BOLD, 20));
        String texto = "$" + valorApuesta;
        FontMetrics metricas = g.getFontMetrics();
        int textoX = xCentro + (diametroCentro - metricas.stringWidth(texto)) / 2;
        int textoY = yCentro + ((diametroCentro - metricas.getHeight()) / 2)
                + metricas.getAscent();
        g.setColor(new Color(0, 0, 0, 120));
        g.drawString(texto, textoX + 1, textoY + 1);
        g.setColor(Color.WHITE);
        g.drawString(texto, textoX, textoY);
        g.dispose();
    }
}
