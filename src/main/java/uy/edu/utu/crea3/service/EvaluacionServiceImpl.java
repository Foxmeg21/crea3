package uy.edu.utu.crea3.service;

import uy.edu.utu.crea3.dao.EvaluacionDAO;
import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Usuario;
import uy.edu.utu.crea3.util.Validador;

public class EvaluacionServiceImpl implements EvaluacionService {

    private final EvaluacionDAO dao;

    public EvaluacionServiceImpl(EvaluacionDAO dao) {
        this.dao = dao;
    }

    @Override
    public void registrarExamenConAsistencia(Usuario actor, int idEstudiante, int idMateria, float nota)
            throws CreaException {
        PermisosUtil.exigirGestion(actor);
        dao.registrarExamenConAsistencia(
                Validador.id(idEstudiante, "ID Estudiante"),
                Validador.id(idMateria, "ID Materia"),
                Validador.nota(nota));
    }
}
