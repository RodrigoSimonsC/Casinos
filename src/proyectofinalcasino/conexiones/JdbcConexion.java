package proyectofinalcasino.conexiones;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Proporciona conexiones a MySQL ejecutándose localmente con XAMPP.
 */
public final class JdbcConexion {

    private static final String URL_POR_DEFECTO =
            "jdbc:mysql://localhost:3306/casino"
            + "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USUARIO_POR_DEFECTO = "root";
    private static final String CONTRASENA_POR_DEFECTO = "";

    private JdbcConexion() {
    }

    public static Connection obtenerConexion() {
        String url = System.getProperty("db.url", URL_POR_DEFECTO);
        String usuario = System.getProperty("db.user", USUARIO_POR_DEFECTO);
        String contrasena = System.getProperty("db.password", CONTRASENA_POR_DEFECTO);
        try {
            return DriverManager.getConnection(url, usuario, contrasena);
        } catch (SQLException exception) {
            System.err.println("No se pudo conectar con la base de datos: "
                    + exception.getMessage());
            return null;
        }
    }
}
