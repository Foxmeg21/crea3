package uy.edu.utu.crea3.dao;

import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Asistencia;

import java.util.List;
import java.util.Map;

public interface AsistenciaDAO {
    boolean registrar(Asistencia asistencia) throws CreaException;

    boolean actualizarEstado(int idAsistencia, String estado) throws CreaException;

    boolean eliminar(int idAsistencia) throws CreaException;

    /** Búsqueda con filtros opcionales (null = sin filtro), con JOIN a materia. */
    List<Asistencia> buscar(Integer idEstudiante, Integer idMateria, String estado) throws CreaException;

    /** Reporte: cantidad de registros por estado (COUNT, GROUP BY). */
    Map<String, Integer> contarPorEstado(Integer idEstudiante) throws CreaException;
}
