package uy.edu.utu.crea3.dao;

import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Usuario;

public interface UsuarioDAO {
    /** Devuelve el usuario si las credenciales son correctas; null si no lo son. */
    Usuario autenticar(String email, String password) throws CreaException;
}
