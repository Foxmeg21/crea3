package uy.edu.utu.crea3.dao;

import uy.edu.utu.crea3.config.DatabaseConnection;
import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Tarea;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TareaDAOImpl implements TareaDAO {

    @Override
    public boolean crear(Tarea t) throws CreaException {
        String sql = "INSERT INTO tarea (id_materia, titulo, descripcion, fecha_limite) VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, t.getIdMateria());
            ps.setString(2, t.getTitulo());
            ps.setString(3, t.getDescripcion());
            ps.setString(4, t.getFecha());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw CreaException.deDatos("Crear tarea", e);
        }
    }

    @Override
    public boolean actualizar(Tarea t) throws CreaException {
        String sql = "UPDATE tarea SET titulo = ?, descripcion = ?, fecha_limite = ? WHERE id_tarea = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, t.getTitulo());
            ps.setString(2, t.getDescripcion());
            ps.setString(3, t.getFecha());
            ps.setInt(4, t.getIdTarea());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw CreaException.deDatos("Actualizar tarea", e);
        }
    }

    @Override
    public boolean eliminar(int idTarea) throws CreaException {
        String sql = "DELETE FROM tarea WHERE id_tarea = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTarea);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw CreaException.deDatos("Eliminar tarea", e);
        }
    }

    @Override
    public List<Tarea> buscar(Integer idMateria, String texto) throws CreaException {
        StringBuilder sql = new StringBuilder(
                "SELECT id_tarea, id_materia, titulo, descripcion, fecha_limite FROM tarea WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (idMateria != null) {
            sql.append(" AND id_materia = ?");
            params.add(idMateria);
        }
        if (texto != null && !texto.isBlank()) {
            sql.append(" AND (titulo LIKE ? OR descripcion LIKE ?)");
            String patron = "%" + texto.trim() + "%";
            params.add(patron);
            params.add(patron);
        }
        sql.append(" ORDER BY fecha_limite, id_tarea");

        List<Tarea> lista = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Tarea t = new Tarea();
                    t.setIdTarea(rs.getInt("id_tarea"));
                    t.setIdMateria(rs.getInt("id_materia"));
                    t.setTitulo(rs.getString("titulo"));
                    t.setDescripcion(rs.getString("descripcion"));
                    t.setFecha(rs.getString("fecha_limite"));
                    lista.add(t);
                }
            }
        } catch (SQLException e) {
            throw CreaException.deDatos("Buscar tareas", e);
        }
        return lista;
    }
}
