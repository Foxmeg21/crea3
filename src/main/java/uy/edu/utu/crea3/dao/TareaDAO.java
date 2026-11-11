package uy.edu.utu.crea3.dao;

import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Tarea;

import java.util.List;

public interface TareaDAO {
    boolean crear(Tarea tarea) throws CreaException;

    boolean actualizar(Tarea tarea) throws CreaException;

    boolean eliminar(int idTarea) throws CreaException;

    /** Búsqueda con filtros opcionales (null = sin filtro). */
    List<Tarea> buscar(Integer idMateria, String texto) throws CreaException;
}
