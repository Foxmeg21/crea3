package uy.edu.utu.crea3.dao;

import uy.edu.utu.crea3.exception.CreaException;

public interface EvaluacionDAO {
    /**
     * Registra, en una única transacción (R7), la calificación de un examen y la
     * asistencia PRESENTE del estudiante ese día. Si falla alguna de las dos
     * escrituras se hace rollback y no queda nada guardado.
     */
    void registrarExamenConAsistencia(int idEstudiante, int idMateria, float nota) throws CreaException;
}
