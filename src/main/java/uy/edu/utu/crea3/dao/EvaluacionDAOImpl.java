package uy.edu.utu.crea3.dao;

import uy.edu.utu.crea3.config.DatabaseConnection;
import uy.edu.utu.crea3.exception.CreaException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EvaluacionDAOImpl implements EvaluacionDAO {

    private static final String SQL_CALIFICACION =
            "INSERT INTO calificacion (id_estudiante, id_materia, nota, fecha, tipo) VALUES (?, ?, ?, CURDATE(), 'Examen')";
    private static final String SQL_ASISTENCIA =
            "INSERT INTO asistencia (id_estudiante, id_materia, fecha, estado) VALUES (?, ?, CURDATE(), 'PRESENTE')";

    @Override
    public void registrarExamenConAsistencia(int idEstudiante, int idMateria, float nota) throws CreaException {
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // inicio de la transacción
            try {
                try (PreparedStatement ps = conn.prepareStatement(SQL_CALIFICACION)) {
                    ps.setInt(1, idEstudiante);
                    ps.setInt(2, idMateria);
                    ps.setFloat(3, nota);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = conn.prepareStatement(SQL_ASISTENCIA)) {
                    ps.setInt(1, idEstudiante);
                    ps.setInt(2, idMateria);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback(); // si algo falla, no queda ninguna de las dos filas
                throw e;
            }
        } catch (SQLException e) {
            throw CreaException.deDatos("Registrar examen con asistencia", e);
        }
    }
}
