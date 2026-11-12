package proyectofinalcasino.conexiones;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import proyectofinalcasino.modelo.ConfiguracionMesa;
import proyectofinalcasino.modelo.Jugador;

/**
 * Repositorio JDBC exclusivo para el historial operativo de las partidas.
 */
public final class PartidaRepositoryJdbc implements PartidaRepository {

    public PartidaRepositoryJdbc() {
        asegurarLongitudCarta();
    }

    @Override
    public int crearPartida(ConfiguracionMesa configuracion,
            List<Jugador> jugadores) {
        Connection conexion = JdbcConexion.obtenerConexion();
        if (conexion == null) {
            return 0;
        }
        String configuracionSql = "SELECT id_configuracion FROM configuracion_mesa "
                + "ORDER BY id_configuracion DESC LIMIT 1";
        String partidaSql = "INSERT INTO partida "
                + "(id_configuracion, estado_partida) VALUES (?, 'En Curso')";
        try (Connection connection = conexion;
                PreparedStatement configuracionStatement
                = connection.prepareStatement(configuracionSql);
                ResultSet configuraciones = configuracionStatement.executeQuery()) {
            int configuracionId;
            if (configuraciones.next()) {
                configuracionId = configuraciones.getInt(1);
            } else {
                configuracionId = crearConfiguracion(connection, configuracion);
            }
            try (PreparedStatement partida = connection.prepareStatement(
                    partidaSql, Statement.RETURN_GENERATED_KEYS)) {
                partida.setInt(1, configuracionId);
                partida.executeUpdate();
                try (ResultSet claves = partida.getGeneratedKeys()) {
                    if (!claves.next()) {
                        return 0;
                    }
                    int partidaId = claves.getInt(1);
                    registrarJugadoresPartida(connection, partidaId, jugadores);
                    return partidaId;
                }
            }
        } catch (SQLException exception) {
            informarError("No se pudo crear la partida.", exception);
            return 0;
        }
    }

    private int crearConfiguracion(Connection connection,
            ConfiguracionMesa configuracion) throws SQLException {
        String sql = "INSERT INTO configuracion_mesa "
                + "(cantidad_mazos, maximo_jugadores, multiples_apuesta, "
                + "variante_reglas, efectos_sonido, recarga_automatica) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(
                sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, configuracion.getCantidadBarajas());
            statement.setInt(2, configuracion.getMaximoJugadores());
            statement.setInt(3, configuracion.getMultiplesApuesta());
            statement.setString(4, configuracion.getVarianteReglas());
            statement.setBoolean(5, configuracion.isEfectosSonido());
            statement.setBoolean(6, configuracion.isRecargaAutomatica());
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                if (!claves.next()) {
                    throw new SQLException(
                            "La base de datos no devolvió el id de la configuración.");
                }
                return claves.getInt(1);
            }
        }
    }

    private void registrarJugadoresPartida(Connection connection, int partidaId,
            List<Jugador> jugadores) throws SQLException {
        String sql = "INSERT INTO partidaJugada "
                + "(id_Partida, id_Usuario, monto_ingreso, saldo_actual_mesa) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Jugador jugador : jugadores) {
                int usuarioId = obtenerUsuarioId(connection,
                        jugador.getNombre());
                if (usuarioId == 0) {
                    continue;
                }
                statement.setInt(1, partidaId);
                statement.setInt(2, usuarioId);
                statement.setBigDecimal(3, dinero(jugador.getSaldo()));
                statement.setBigDecimal(4, dinero(jugador.getSaldo()));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    @Override
    public int crearRonda(int partidaId, int numeroRonda,
            List<Jugador> jugadores, int[] apuestas) {
        Connection conexion = JdbcConexion.obtenerConexion();
        if (conexion == null || jugadores.isEmpty()) {
            return 0;
        }
        String partidaJugador = "SELECT id_partidaJugada FROM partidaJugada "
                + "WHERE id_Partida = ? ORDER BY id_partidaJugada LIMIT 1";
        String rondaSql = "INSERT INTO rondas "
                + "(id_PartidaJugada, ronda) VALUES (?, ?)";
        try (Connection connection = conexion;
                PreparedStatement jugador = connection.prepareStatement(partidaJugador)) {
            jugador.setInt(1, partidaId);
            try (ResultSet resultado = jugador.executeQuery()) {
                if (!resultado.next()) {
                    return 0;
                }
                try (PreparedStatement ronda = connection.prepareStatement(
                        rondaSql, Statement.RETURN_GENERATED_KEYS)) {
                    ronda.setInt(1, resultado.getInt(1));
                    ronda.setInt(2, numeroRonda);
                    ronda.executeUpdate();
                    try (ResultSet claves = ronda.getGeneratedKeys()) {
                        if (!claves.next()) {
                            return 0;
                        }
                        int rondaId = claves.getInt(1);
                        registrarApuestas(connection, rondaId, jugadores, apuestas);
                        return rondaId;
                    }
                }
            }
        } catch (SQLException exception) {
            informarError("No se pudo crear la ronda.", exception);
            return 0;
        }
    }

    private void registrarApuestas(Connection connection, int rondaId,
            List<Jugador> jugadores, int[] apuestas) throws SQLException {
        String sql = "INSERT INTO Apuesta (idUsuario, id_ronda, apostado) "
                + "VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int indice = 0; indice < jugadores.size(); indice++) {
                int usuarioId = obtenerEstadoUsuarioId(connection,
                        jugadores.get(indice).getNombre());
                if (usuarioId == 0) {
                    continue;
                }
                statement.setInt(1, usuarioId);
                statement.setInt(2, rondaId);
                statement.setBigDecimal(3, dinero(apuestas[indice]));
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    @Override
    public int crearMano(int rondaId, int jugador, int numeroMano,
            double apuesta) {
        Connection conexion = JdbcConexion.obtenerConexion();
        if (conexion == null) {
            return 0;
        }
        String sql = "INSERT INTO manos_jugador "
                + "(id_ronda, idUsuario, numero_mano, apuesta_mano) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection connection = conexion;
                PreparedStatement statement = connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, rondaId);
            statement.setInt(2, jugador);
            statement.setInt(3, numeroMano);
            statement.setBigDecimal(4, dinero(apuesta));
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                return claves.next() ? claves.getInt(1) : 0;
            }
        } catch (SQLException exception) {
            informarError("No se pudo registrar la mano.", exception);
            return 0;
        }
    }

    @Override
    public int obtenerEstadoUsuarioId(String nombre) {
        Connection conexion = JdbcConexion.obtenerConexion();
        if (conexion == null) {
            return 0;
        }
        try (Connection connection = conexion) {
            return obtenerEstadoUsuarioId(connection, nombre);
        } catch (SQLException exception) {
            informarError("No se pudo consultar el usuario de la partida.", exception);
            return 0;
        }
    }

    @Override
    public int obtenerUsuarioId(String nombre) {
        Connection conexion = JdbcConexion.obtenerConexion();
        if (conexion == null) {
            return 0;
        }
        String sql = "SELECT u.id_Usuarios FROM Usuarios u "
                + "WHERE u.Nombre = ? ORDER BY u.id_Usuarios DESC LIMIT 1";
        try (Connection connection = conexion;
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() ? resultado.getInt(1) : 0;
            }
        } catch (SQLException exception) {
            informarError("No se pudo consultar el usuario de la partida.", exception);
            return 0;
        }
    }

    private int obtenerUsuarioId(Connection connection, String nombre)
            throws SQLException {
        String sql = "SELECT u.id_Usuarios FROM Usuarios u "
                + "WHERE u.Nombre = ? ORDER BY u.id_Usuarios DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() ? resultado.getInt(1) : 0;
            }
        }
    }

    @Override
    public boolean registrarCarta(int rondaId, int manoId, int jugador,
            String carta, boolean dealer) {
        if (carta == null || carta.isBlank()) {
            return false;
        }
        return ejecutar("INSERT INTO Repartido "
                + "(id_Rondas, id_mano, id_Jugador, CartaActual, es_crupier) "
                + "VALUES (?, ?, ?, ?, ?)", statement -> {
            statement.setInt(1, rondaId);
            if (manoId > 0) {
                statement.setInt(2, manoId);
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            if (jugador > 0) {
                statement.setInt(3, jugador);
            } else {
                statement.setNull(3, java.sql.Types.INTEGER);
            }
            statement.setString(4, carta);
            statement.setBoolean(5, dealer);
        });
    }

    @Override
    public int crearTurno(int rondaId, int jugador, boolean dealer, int orden) {
        Connection conexion = JdbcConexion.obtenerConexion();
        if (conexion == null) {
            return 0;
        }
        String sql = "INSERT INTO turnos "
                + "(id_ronda, idUsuario, es_dealer, orden_turno, estado_turno) "
                + "VALUES (?, ?, ?, ?, 'En Juego')";
        try (Connection connection = conexion;
                PreparedStatement statement = connection.prepareStatement(
                        sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, rondaId);
            if (jugador > 0) {
                statement.setInt(2, jugador);
            } else {
                statement.setNull(2, java.sql.Types.INTEGER);
            }
            statement.setBoolean(3, dealer);
            statement.setInt(4, orden);
            statement.executeUpdate();
            try (ResultSet claves = statement.getGeneratedKeys()) {
                return claves.next() ? claves.getInt(1) : 0;
            }
        } catch (SQLException exception) {
            informarError("No se pudo registrar el turno.", exception);
            return 0;
        }
    }

    @Override
    public boolean registrarDecision(int turnoId, int manoId, String decision,
            double valorAdicional) {
        return ejecutar("INSERT INTO decisiones_turno "
                + "(id_turno, id_mano, tipo_decision, valor_adicional) "
                + "VALUES (?, ?, ?, ?)", statement -> {
            statement.setInt(1, turnoId);
            statement.setInt(2, manoId);
            statement.setString(3, decision);
            statement.setBigDecimal(4, dinero(valorAdicional));
        });
    }

    @Override
    public boolean registrarResultado(int rondaId, int jugador, int puntuacion,
            String resultado) {
        return ejecutar("INSERT INTO resultados_ronda "
                + "(id_ronda, idUsuario, puntuacion_final, resultado) "
                + "VALUES (?, ?, ?, ?)", statement -> {
            statement.setInt(1, rondaId);
            statement.setInt(2, jugador);
            statement.setInt(3, puntuacion);
            statement.setString(4, resultado);
        });
    }

    @Override
    public boolean registrarPago(int rondaId, int jugador, double monto) {
        return ejecutar("INSERT INTO Pagos (id_ronda, idUsuario, monto_pagado) "
                + "VALUES (?, ?, ?)", statement -> {
            statement.setInt(1, rondaId);
            statement.setInt(2, jugador);
            statement.setBigDecimal(3, dinero(monto));
        });
    }

    @Override
    public boolean actualizarSaldoEnPartida(int partidaId, String jugador,
            double saldo) {
        return ejecutar("UPDATE partidaJugada pj INNER JOIN Usuarios u "
                + "ON u.id_Usuarios = pj.id_Usuario SET pj.saldo_actual_mesa = ? "
                + "WHERE pj.id_Partida = ? AND u.Nombre = ?", statement -> {
            statement.setBigDecimal(1, dinero(saldo));
            statement.setInt(2, partidaId);
            statement.setString(3, jugador);
        });
    }

    @Override
    public boolean actualizarApuestaMano(int manoId, double apuesta) {
        return ejecutar("UPDATE manos_jugador SET apuesta_mano = ? "
                + "WHERE id_mano = ?", statement -> {
            statement.setBigDecimal(1, dinero(apuesta));
            statement.setInt(2, manoId);
        });
    }

    @Override
    public boolean cerrarPartida(int partidaId, String estado) {
        return ejecutar("UPDATE partida SET estado_partida = ? "
                + "WHERE id_partida = ?", statement -> {
            statement.setString(1, estado);
            statement.setInt(2, partidaId);
        });
    }

    private int obtenerEstadoUsuarioId(Connection connection, String nombre)
            throws SQLException {
        String sql = "SELECT eu.id_estadoUsuario FROM estadoUsuario eu "
                + "INNER JOIN Usuarios u ON u.id_Usuarios = eu.id_Usuario "
                + "WHERE u.Nombre = ? ORDER BY eu.id_estadoUsuario DESC LIMIT 1";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nombre);
            try (ResultSet resultado = statement.executeQuery()) {
                return resultado.next() ? resultado.getInt(1) : 0;
            }
        }
    }

    private BigDecimal dinero(double valor) {
        return BigDecimal.valueOf(valor);
    }

    private boolean ejecutar(String sql, Preparador preparador) {
        Connection conexion = JdbcConexion.obtenerConexion();
        if (conexion == null) {
            return false;
        }
        try (Connection connection = conexion;
                PreparedStatement statement = connection.prepareStatement(sql)) {
            preparador.preparar(statement);
            return statement.executeUpdate() > 0;
        } catch (SQLException exception) {
            informarError("No se pudo guardar un evento de la partida.", exception);
            return false;
        }
    }

    private void asegurarLongitudCarta() {
        Connection conexion = JdbcConexion.obtenerConexion();
        if (conexion == null) {
            return;
        }
        String sql = "ALTER TABLE Repartido "
                + "MODIFY CartaActual VARCHAR(255) NOT NULL";
        try (Connection connection = conexion;
                Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException exception) {
            informarError("No se pudo ampliar la columna CartaActual. "
                    + "Ejecuta la migración SQL manualmente.", exception);
        }
    }

    private void informarError(String mensaje, SQLException exception) {
        System.err.println(mensaje + " " + exception.getMessage());
    }

    @FunctionalInterface
    private interface Preparador {

        void preparar(PreparedStatement statement) throws SQLException;
    }
}
