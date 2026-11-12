package proyectofinalcasino.conexiones;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import proyectofinalcasino.modelo.ConfiguracionMesa;
import proyectofinalcasino.modelo.Jugador;

/**
 * Repositorio JDBC para las tablas Usuarios, estadoUsuario y
 * configuracion_mesa del esquema casino.
 */
public final class MesaRepositoryJdbc implements MesaRepository {

    private final List<Jugador> jugadoresActivos = new ArrayList<>();
    private final boolean tieneMaximoJugadores;
    private final boolean tieneMultiplesApuesta;
    private final boolean tieneVarianteReglas;
    private final boolean tieneEfectosSonido;
    private final boolean tieneRecargaAutomatica;

    public MesaRepositoryJdbc() {
        this.tieneMaximoJugadores = detectarColumna("configuracion_mesa", "maximo_jugadores");
        this.tieneMultiplesApuesta = detectarColumna("configuracion_mesa", "multiples_apuesta");
        this.tieneVarianteReglas = detectarColumna("configuracion_mesa", "variante_reglas");
        this.tieneEfectosSonido = detectarColumna("configuracion_mesa", "efectos_sonido");
        this.tieneRecargaAutomatica = detectarColumna("configuracion_mesa", "recarga_automatica");
    }

    @Override
    public List<Jugador> consultarJugadores() {
        // El saldo pertenece a la sesión actual, no se recupera desde MySQL.
        return new ArrayList<>(jugadoresActivos);
    }

    @Override
    public void guardarJugadores(List<Jugador> jugadores) {
        String insertarUsuario = "INSERT INTO Usuarios (Nombre) VALUES (?)";
        String insertarEstado = "INSERT INTO estadoUsuario (id_Usuario, saldo_global) VALUES (?, 0)";
        String buscarJugador = "SELECT u.id_Usuarios FROM Usuarios u "
                + "WHERE u.Nombre = ? LIMIT 1";
        Connection conexion = JdbcConexion.obtenerConexion();
        if (conexion == null) {
            jugadoresActivos.clear();
            jugadoresActivos.addAll(jugadores);
            return;
        }

        try (Connection connection = conexion) {
            connection.setAutoCommit(false);
            try (PreparedStatement usuario = connection.prepareStatement(insertarUsuario,
                            Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement estado = connection.prepareStatement(insertarEstado);
                    PreparedStatement existente = connection.prepareStatement(buscarJugador)) {
                for (Jugador jugador : jugadores) {
                    existente.setString(1, jugador.getNombre());
                    try (ResultSet resultados = existente.executeQuery()) {
                        if (resultados.next()) {
                            continue;
                        }
                    }

                    usuario.setString(1, jugador.getNombre());
                    usuario.executeUpdate();
                    try (ResultSet claves = usuario.getGeneratedKeys()) {
                        if (!claves.next()) {
                            informarError("La base de datos no devolvió el id del usuario.",
                                    null);
                            continue;
                        }
                        estado.setLong(1, claves.getLong(1));
                    }
                    estado.executeUpdate();
                }
                connection.commit();
            } catch (SQLException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    informarError("No se pudo deshacer el guardado.", rollbackException);
                }
                informarError("No se pudieron guardar los jugadores.", exception);
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException exception) {
                    informarError("No se pudo restaurar la conexión.", exception);
                }
            }
            jugadoresActivos.clear();
            jugadoresActivos.addAll(jugadores);
        } catch (SQLException exception) {
            informarError("No se pudieron guardar los jugadores.", exception);
        }
    }

    @Override
    public boolean actualizarSaldo(String nombre, double saldo) {
        // Las apuestas solo modifican el saldo que vive en esta ejecución.
        double saldoNormalizado = Math.max(0, saldo);
        for (int indice = 0; indice < jugadoresActivos.size(); indice++) {
            Jugador jugador = jugadoresActivos.get(indice);
            if (jugador.getNombre().equals(nombre)) {
                jugadoresActivos.set(indice,
                        new Jugador(nombre, saldoNormalizado));
                return true;
            }
        }
        return false;
    }

    @Override
    public ConfiguracionMesa consultarConfiguracion() {
        StringBuilder columnas = new StringBuilder("cantidad_mazos");
        agregarColumna(columnas, tieneMaximoJugadores, "maximo_jugadores");
        agregarColumna(columnas, tieneMultiplesApuesta, "multiples_apuesta");
        agregarColumna(columnas, tieneVarianteReglas, "variante_reglas");
        agregarColumna(columnas, tieneEfectosSonido, "efectos_sonido");
        agregarColumna(columnas, tieneRecargaAutomatica, "recarga_automatica");
        String sql = "SELECT " + columnas + " FROM configuracion_mesa "
                + "ORDER BY id_configuracion LIMIT 1";
        Connection conexion = JdbcConexion.obtenerConexion();
        if (conexion == null) {
            return new ConfiguracionMesa();
        }
        try (Connection connection = conexion;
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultados = statement.executeQuery()) {
            ConfiguracionMesa configuracion = new ConfiguracionMesa();
            if (resultados.next()) {
                configuracion.setCantidadBarajas(resultados.getInt("cantidad_mazos"));
                if (tieneMaximoJugadores) {
                    configuracion.setMaximoJugadores(resultados.getInt("maximo_jugadores"));
                }
                if (tieneMultiplesApuesta) {
                    configuracion.setMultiplesApuesta(resultados.getInt("multiples_apuesta"));
                }
                if (tieneVarianteReglas) {
                    configuracion.setVarianteReglas(resultados.getString("variante_reglas"));
                }
                if (tieneEfectosSonido) {
                    configuracion.setEfectosSonido(resultados.getBoolean("efectos_sonido"));
                }
                if (tieneRecargaAutomatica) {
                    configuracion.setRecargaAutomatica(resultados.getBoolean("recarga_automatica"));
                }
            }
            return configuracion;
        } catch (SQLException exception) {
            informarError("No se pudo consultar la configuración.", exception);
            return new ConfiguracionMesa();
        }
    }

    @Override
    public void guardarConfiguracion(ConfiguracionMesa configuracion) {
        Connection conexion = JdbcConexion.obtenerConexion();
        if (conexion == null) {
            return;
        }
        try (Connection connection = conexion) {
            connection.setAutoCommit(false);
            try {
                String borrar = "DELETE FROM configuracion_mesa";
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(borrar);
                }
                StringBuilder columnas = new StringBuilder("cantidad_mazos");
                StringBuilder valores = new StringBuilder("?");
                agregarColumnaYValor(columnas, valores, tieneMaximoJugadores,
                        "maximo_jugadores");
                agregarColumnaYValor(columnas, valores, tieneMultiplesApuesta,
                        "multiples_apuesta");
                agregarColumnaYValor(columnas, valores, tieneVarianteReglas,
                        "variante_reglas");
                agregarColumnaYValor(columnas, valores, tieneEfectosSonido,
                        "efectos_sonido");
                agregarColumnaYValor(columnas, valores, tieneRecargaAutomatica,
                        "recarga_automatica");
                String insertar = "INSERT INTO configuracion_mesa (" + columnas
                        + ") VALUES (" + valores + ")";
                try (PreparedStatement statement = connection.prepareStatement(insertar)) {
                    int parametro = 1;
                    statement.setInt(parametro++, configuracion.getCantidadBarajas());
                    if (tieneMaximoJugadores) {
                        statement.setInt(parametro++, configuracion.getMaximoJugadores());
                    }
                    if (tieneMultiplesApuesta) {
                        statement.setInt(parametro++, configuracion.getMultiplesApuesta());
                    }
                    if (tieneVarianteReglas) {
                        statement.setString(parametro++, configuracion.getVarianteReglas());
                    }
                    if (tieneEfectosSonido) {
                        statement.setBoolean(parametro++, configuracion.isEfectosSonido());
                    }
                    if (tieneRecargaAutomatica) {
                        statement.setBoolean(parametro, configuracion.isRecargaAutomatica());
                    }
                    statement.executeUpdate();
                }

                connection.commit();
            } catch (SQLException exception) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    informarError("No se pudo deshacer la configuración.", rollbackException);
                }
                informarError("No se pudo guardar la configuración.", exception);
            } finally {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException exception) {
                    informarError("No se pudo restaurar la conexión.", exception);
                }
            }
        } catch (SQLException exception) {
            informarError("No se pudo guardar la configuración.", exception);
        }
    }

    private void agregarColumna(StringBuilder columnas, boolean existe, String columna) {
        if (existe) {
            columnas.append(", ").append(columna);
        }
    }

    private void agregarColumnaYValor(StringBuilder columnas, StringBuilder valores,
            boolean existe, String columna) {
        if (existe) {
            columnas.append(", ").append(columna);
            valores.append(", ?");
        }
    }

    private boolean detectarColumna(String tabla, String columna) {
        Connection conexion = JdbcConexion.obtenerConexion();
        if (conexion == null) {
            return false;
        }
        try (Connection connection = conexion) {
            DatabaseMetaData metadata = connection.getMetaData();
            try (ResultSet columnas = metadata.getColumns(connection.getCatalog(), null,
                    tabla, columna)) {
                return columnas.next();
            }
        } catch (SQLException exception) {
            informarError("No se pudo verificar la estructura de la tabla " + tabla + ".",
                    exception);
            return false;
        }
    }

    private void informarError(String mensaje, SQLException exception) {
        String detalle = exception == null ? "" : " " + exception.getMessage();
        System.err.println(mensaje + detalle);
    }
}
