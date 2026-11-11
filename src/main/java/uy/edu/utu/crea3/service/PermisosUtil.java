package uy.edu.utu.crea3.service;

import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Usuario;

/** Control de permisos común a los servicios (R3). */
final class PermisosUtil {

    private PermisosUtil() {
    }

    static void exigirGestion(Usuario actor) throws CreaException {
        if (actor == null || !actor.puedeGestionar()) {
            throw new CreaException("No tiene permisos para realizar esta operación.");
        }
    }

    /** Estudiante: siempre su propio ID. Docente: el filtro que haya pedido (puede ser null). */
    static Integer alcance(Usuario actor, Integer idPedido) throws CreaException {
        if (actor == null) {
            throw new CreaException("Debe iniciar sesión.");
        }
        return actor.puedeGestionar() ? idPedido : Integer.valueOf(actor.getIdUsuario());
    }
}
