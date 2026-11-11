package uy.edu.utu.crea3.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Conexión a la base de datos. Toda la configuración sale de variables de entorno (R10):
 *   DB_URL      (opcional) URL JDBC completa; si está, se ignoran HOST/PORT/NAME/SSL_MODE
 *   DB_HOST     (por defecto localhost)
 *   DB_PORT     (por defecto 3306)
 *   DB_NAME     (por defecto crea3_db)
 *   DB_SSL_MODE (por defecto PREFERRED; usar REQUIRED con Aiven)
 *   DB_USER     (obligatoria)
 *   DB_PASS     (obligatoria; puede ser vacía en desarrollo local)
 * No hay usuario ni contraseña por defecto en el código.
 */
public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url(), requerida("DB_USER"), requerida("DB_PASS"));
    }

    /** Verifica que la base responda (R12). */
    public static boolean verificar() {
        try (Connection c = getConnection(); Statement s = c.createStatement()) {
            s.execute("SELECT 1");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    private static String url() {
        String url = System.getenv("DB_URL");
        if (url != null && !url.isBlank()) {
            return url;
        }
        String host = opcional("DB_HOST", "localhost");
        String puerto = opcional("DB_PORT", "3306");
        String base = opcional("DB_NAME", "crea3_db");
        String ssl = opcional("DB_SSL_MODE", "PREFERRED");
        return "jdbc:mysql://" + host + ":" + puerto + "/" + base + "?sslMode=" + ssl;
    }

    private static String opcional(String nombre, String porDefecto) {
        String v = System.getenv(nombre);
        return (v == null || v.isBlank()) ? porDefecto : v;
    }

    private static String requerida(String nombre) throws SQLException {
        String v = System.getenv(nombre);
        if (v == null) {
            throw new SQLException("Falta la variable de entorno " + nombre);
        }
        return v;
    }
}
