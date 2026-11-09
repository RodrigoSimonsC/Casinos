package proyectofinalcasino.funciones;

/**
 * Recibe eventos del motor visual sin mezclarlo con la persistencia.
 */
public interface PartidaEventoListener {

    void cartaRepartida(int jugador, int mano, String carta, boolean dealer);

    void turnoIniciado(int jugador, int mano, boolean dealer);

    void decisionTomada(int jugador, int mano, String decision,
            double valorAdicional);

    void partidaFinalizada(String[] resultados, int[] puntuaciones);
}
