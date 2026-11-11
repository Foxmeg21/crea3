package uy.edu.utu.crea3.exception;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Excepción propia de la aplicación (R8). Su mensaje es apto para mostrarse al usuario:
 * nunca contiene detalles internos (SQL, rutas, trazas).
 */
public class CreaException extends Exception {

    private static final Logger LOG = Logger.getLogger(CreaException.class.getName());

    public CreaException(String mensaje) {
        super(mensaje);
    }

    public CreaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    /**
     * Convierte un error SQL en una excepción con mensaje seguro para el usuario.
     * El detalle técnico queda solo en el log.
     */
    public static CreaException deDatos(String contexto, SQLException e) {
        LOG.log(Level.WARNING, contexto + " [SQLState=" + e.getSQLState() + "]: " + e.getMessage());
        String estado = e.getSQLState();
        if (estado != null && estado.startsWith("23")) {
            return new CreaException("Los datos no son válidos: verifique que los IDs existan y no estén repetidos.", e);
        }
        return new CreaException("No se pudo completar la operación con la base de datos. Intente nuevamente.", e);
    }
}
