package uy.edu.utu.crea3.service;

import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Asistencia;
import uy.edu.utu.crea3.model.Usuario;

import java.util.List;
import java.util.Map;

public interface AsistenciaService {
    void registrar(Usuario actor, int idEstudiante, int idMateria, String estado) throws CreaException;

    void modificarEstado(Usuario actor, int idAsistencia, String estado) throws CreaException;

    void eliminar(Usuario actor, int idAsistencia) throws CreaException;

    /** Un estudiante solo ve sus propias asistencias, sin importar el filtro recibido. */
    List<Asistencia> buscar(Usuario actor, Integer idEstudiante, Integer idMateria, String estado) throws CreaException;

    Map<String, Integer> resumenPorEstado(Usuario actor, Integer idEstudiante) throws CreaException;
}
