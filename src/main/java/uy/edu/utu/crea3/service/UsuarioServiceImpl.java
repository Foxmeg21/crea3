package uy.edu.utu.crea3.service;

import uy.edu.utu.crea3.dao.UsuarioDAO;
import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Usuario;
import uy.edu.utu.crea3.util.Validador;

public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioDAO dao;

    public UsuarioServiceImpl(UsuarioDAO dao) {
        this.dao = dao;
    }

    @Override
    public Usuario iniciarSesion(String email, String password) throws CreaException {
        String e = Validador.texto(email, "Correo electrónico", 100);
        if (!e.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new CreaException("Ingrese un correo electrónico válido.");
        }
        String p = Validador.texto(password, "Contraseña", 72);
        Usuario usuario = dao.autenticar(e, p);
        if (usuario == null) {
            throw new CreaException("Credenciales incorrectas o usuario inexistente.");
        }
        return usuario;
    }
}
