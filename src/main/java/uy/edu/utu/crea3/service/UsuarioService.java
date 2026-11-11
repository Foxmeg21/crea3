package uy.edu.utu.crea3.service;

import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Usuario;

public interface UsuarioService {
    /** Valida los datos y autentica. Lanza CreaException si son inválidos o las credenciales no coinciden. */
    Usuario iniciarSesion(String email, String password) throws CreaException;
}
