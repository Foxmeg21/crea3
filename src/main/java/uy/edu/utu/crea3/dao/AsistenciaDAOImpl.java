package uy.edu.utu.crea3.dao;

import uy.edu.utu.crea3.config.DatabaseConnection;
import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Asistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AsistenciaDAOImpl implements AsistenciaDAO {

    @Override
    public boolean registrar(Asistencia a) throws CreaException {
        String sql = "INSERT INTO asistencia (id_estudiante, id_materia, fecha, estado) VALUES (?, ?, CURDATE(), ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, a.getIdEstudiante());
            stmt.setInt(2, a.getIdMateria());
            stmt.setString(3, a.getEstado());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw CreaException.deDatos("Registrar asistencia", e);
        }
    }

    @Override
    public boolean actualizarEstado(int idAsistencia, String estado) throws CreaException {
        String sql = "UPDATE asistencia SET estado = ? WHERE id_asistencia = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, estado);
            stmt.setInt(2, idAsistencia);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw CreaException.deDatos("Actualizar asistencia", e);
        }
    }

    @Override
    public boolean eliminar(int idAsistencia) throws CreaException {
        String sql = "DELETE FROM asistencia WHERE id_asistencia = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idAsistencia);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw CreaException.deDatos("Eliminar asistencia", e);
        }
    }

    @Override
    public List<Asistencia> buscar(Integer idEstudiante, Integer idMateria, String estado) throws CreaException {
        StringBuilder sql = new StringBuilder(
                "SELECT a.id_asistencia, a.id_estudiante, a.id_materia, a.fecha, a.estado, m.nombre AS materia " +
                "FROM asistencia a JOIN materia m ON a.id_materia = m.id_materia WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (idEstudiante != null) {
            sql.append(" AND a.id_estudiante = ?");
            params.add(idEstudiante);
        }
        if (idMateria != null) {
            sql.append(" AND a.id_materia = ?");
            params.add(idMateria);
        }
        if (estado != null && !estado.isBlank()) {
            sql.append(" AND a.estado = ?");
            params.add(estado);
        }
        sql.append(" ORDER BY a.fecha DESC, a.id_asistencia DESC");

        List<Asistencia> lista = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Asistencia asis = new Asistencia();
                    asis.setIdAsistencia(rs.getInt("id_asistencia"));
                    asis.setIdEstudiante(rs.getInt("id_estudiante"));
                    asis.setIdMateria(rs.getInt("id_materia"));
                    asis.setFecha(rs.getDate("fecha"));
                    asis.setEstado(rs.getString("estado"));
                    asis.setNombreMateria(rs.getString("materia"));
                    lista.add(asis);
                }
            }
        } catch (SQLException e) {
            throw CreaException.deDatos("Buscar asistencias", e);
        }
        return lista;
    }

    @Override
    public Map<String, Integer> contarPorEstado(Integer idEstudiante) throws CreaException {
        StringBuilder sql = new StringBuilder("SELECT estado, COUNT(*) AS cantidad FROM asistencia");
        if (idEstudiante != null) {
            sql.append(" WHERE id_estudiante = ?");
        }
        sql.append(" GROUP BY estado ORDER BY estado");

        Map<String, Integer> resumen = new LinkedHashMap<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            if (idEstudiante != null) {
                stmt.setInt(1, idEstudiante);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resumen.put(rs.getString("estado"), rs.getInt("cantidad"));
                }
            }
        } catch (SQLException e) {
            throw CreaException.deDatos("Reporte de asistencias", e);
        }
        return resumen;
    }
}
