package uy.edu.utu.crea3.dao;

import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Calificacion;
import uy.edu.utu.crea3.model.ResumenMateria;

import java.util.List;

public interface CalificacionDAO {
    boolean registrar(Calificacion calificacion) throws CreaException;

    boolean actualizar(Calificacion calificacion) throws CreaException;

    boolean eliminar(int idCalificacion) throws CreaException;

    /** Búsqueda con filtros opcionales (null = sin filtro), con JOIN a materia. */
    List<Calificacion> buscar(Integer idEstudiante, Integer idMateria, String tipo, Float notaMinima) throws CreaException;

    /** Reporte: cantidad y promedio de notas por materia (COUNT, AVG, GROUP BY). */
    List<ResumenMateria> promediosPorMateria(Integer idEstudiante) throws CreaException;
}
