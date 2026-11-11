package uy.edu.utu.crea3.dao;

import uy.edu.utu.crea3.config.DatabaseConnection;
import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Calificacion;
import uy.edu.utu.crea3.model.ResumenMateria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CalificacionDAOImpl implements CalificacionDAO {

    @Override
    public boolean registrar(Calificacion c) throws CreaException {
        String sql = "INSERT INTO calificacion (id_estudiante, id_materia, nota, fecha, tipo) VALUES (?, ?, ?, CURDATE(), ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, c.getIdEstudiante());
            stmt.setInt(2, c.getIdMateria());
            stmt.setFloat(3, c.getNota());
            stmt.setString(4, c.getTipo());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw CreaException.deDatos("Registrar calificación", e);
        }
    }

    @Override
    public boolean actualizar(Calificacion c) throws CreaException {
        String sql = "UPDATE calificacion SET nota = ?, tipo = ? WHERE id_calificacion = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setFloat(1, c.getNota());
            stmt.setString(2, c.getTipo());
            stmt.setInt(3, c.getIdCalificacion());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw CreaException.deDatos("Actualizar calificación", e);
        }
    }

    @Override
    public boolean eliminar(int idCalificacion) throws CreaException {
        String sql = "DELETE FROM calificacion WHERE id_calificacion = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCalificacion);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw CreaException.deDatos("Eliminar calificación", e);
        }
    }

    @Override
    public List<Calificacion> buscar(Integer idEstudiante, Integer idMateria, String tipo, Float notaMinima)
            throws CreaException {
        StringBuilder sql = new StringBuilder(
                "SELECT c.id_calificacion, c.id_estudiante, c.id_materia, c.nota, c.fecha, c.tipo, m.nombre AS materia " +
                "FROM calificacion c JOIN materia m ON c.id_materia = m.id_materia WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (idEstudiante != null) {
            sql.append(" AND c.id_estudiante = ?");
            params.add(idEstudiante);
        }
        if (idMateria != null) {
            sql.append(" AND c.id_materia = ?");
            params.add(idMateria);
        }
        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND c.tipo = ?");
            params.add(tipo);
        }
        if (notaMinima != null) {
            sql.append(" AND c.nota >= ?");
            params.add(notaMinima);
        }
        sql.append(" ORDER BY c.fecha DESC, c.id_calificacion DESC");

        List<Calificacion> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Calificacion cal = new Calificacion();
                    cal.setIdCalificacion(rs.getInt("id_calificacion"));
                    cal.setIdEstudiante(rs.getInt("id_estudiante"));
                    cal.setIdMateria(rs.getInt("id_materia"));
                    cal.setNota(rs.getFloat("nota"));
                    cal.setFecha(rs.getDate("fecha"));
                    cal.setTipo(rs.getString("tipo"));
                    cal.setNombreMateria(rs.getString("materia"));
                    lista.add(cal);
                }
            }
        } catch (SQLException e) {
            throw CreaException.deDatos("Buscar calificaciones", e);
        }
        return lista;
    }

    @Override
    public List<ResumenMateria> promediosPorMateria(Integer idEstudiante) throws CreaException {
        StringBuilder sql = new StringBuilder(
                "SELECT m.nombre AS materia, COUNT(*) AS cantidad, AVG(c.nota) AS promedio " +
                "FROM calificacion c JOIN materia m ON c.id_materia = m.id_materia");
        if (idEstudiante != null) {
            sql.append(" WHERE c.id_estudiante = ?");
        }
        sql.append(" GROUP BY m.id_materia, m.nombre ORDER BY m.nombre");

        List<ResumenMateria> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            if (idEstudiante != null) {
                stmt.setInt(1, idEstudiante);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new ResumenMateria(rs.getString("materia"), rs.getInt("cantidad"), rs.getDouble("promedio")));
                }
            }
        } catch (SQLException e) {
            throw CreaException.deDatos("Reporte de promedios", e);
        }
        return lista;
    }
}
