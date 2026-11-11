package uy.edu.utu.crea3.service;

import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Calificacion;
import uy.edu.utu.crea3.model.ResumenMateria;
import uy.edu.utu.crea3.model.Usuario;

import java.util.List;

public interface CalificacionService {
    void registrar(Usuario actor, int idEstudiante, int idMateria, float nota, String tipo) throws CreaException;

    void modificar(Usuario actor, int idCalificacion, float nota, String tipo) throws CreaException;

    void eliminar(Usuario actor, int idCalificacion) throws CreaException;

    /** Un estudiante solo ve sus propias calificaciones, sin importar el filtro recibido. */
    List<Calificacion> buscar(Usuario actor, Integer idEstudiante, Integer idMateria, String tipo, Float notaMinima)
            throws CreaException;

    List<ResumenMateria> promediosPorMateria(Usuario actor, Integer idEstudiante) throws CreaException;
}
