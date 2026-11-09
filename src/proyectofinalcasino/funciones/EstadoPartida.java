package proyectofinalcasino.funciones;

/**
 * Estados visibles del ciclo de una partida.
 */
public enum EstadoPartida {
    ESPERANDO_APUESTAS,
    REPARTIENDO,
    TURNO_JUGADOR,
    TURNO_DEALER,
    RESULTADO
}
