package proyectofinalcasino.historial;

import java.util.ArrayList;
import java.util.List;

/**
 * Historial independiente de la interfaz y del repositorio JDBC.
 */
public final class HistorialPartidas {

    private final List<ResultadoPartida> resultados = new ArrayList<>();

    public void registrar(ResultadoPartida resultado) {
        resultados.add(java.util.Objects.requireNonNull(resultado,
                "El resultado no puede ser null"));
    }

    public List<ResultadoPartida> obtenerResultados() {
        return List.copyOf(resultados);
    }

    public void limpiar() {
        resultados.clear();
    }
}
