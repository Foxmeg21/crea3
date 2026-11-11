package uy.edu.utu.crea3.dao;

import uy.edu.utu.crea3.config.DatabaseConnection;
import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Docente;
import uy.edu.utu.crea3.model.Estudiante;
import uy.edu.utu.crea3.model.Usuario;
import uy.edu.utu.crea3.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAOImpl implements UsuarioDAO {

    private static final String SQL =
            "SELECT id_usuario, nombre, apellido, email, contrasena, rol FROM usuario WHERE LOWER(email) = LOWER(?)";

    @Override
    public Usuario autenticar(String email, String password) throws CreaException {
        if (email == null || password == null) {
            return null;
        }
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(SQL)) {

            stmt.setString(1, email.trim());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                String hash = rs.getString("contrasena");
                if (hash == null || !PasswordUtil.checkPassword(password.trim(), hash.trim())) {
                    return null;
                }
                return crearUsuario(rs);
            }
        } catch (SQLException e) {
            throw CreaException.deDatos("Autenticación", e);
        }
    }

    /** Fábrica: devuelve la subclase que corresponde al rol guardado. */
    private Usuario crearUsuario(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_usuario");
        String nombre = rs.getString("nombre");
        String apellido = rs.getString("apellido");
        String email = rs.getString("email");
        String rol = rs.getString("rol");
        if ("DOCENTE".equalsIgnoreCase(rol)) {
            return new Docente(id, nombre, apellido, email);
        }
        if ("ESTUDIANTE".equalsIgnoreCase(rol)) {
            return new Estudiante(id, nombre, apellido, email);
        }
        return null; // rol desconocido: se trata como credenciales inválidas
    }
}
