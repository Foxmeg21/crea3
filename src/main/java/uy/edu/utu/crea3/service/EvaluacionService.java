package uy.edu.utu.crea3.service;

import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Usuario;

public interface EvaluacionService {
    /** Calificación de examen + asistencia PRESENTE en una sola transacción. */
    void registrarExamenConAsistencia(Usuario actor, int idEstudiante, int idMateria, float nota) throws CreaException;
}
