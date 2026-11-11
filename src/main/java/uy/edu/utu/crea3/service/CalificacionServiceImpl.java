package uy.edu.utu.crea3.service;

import uy.edu.utu.crea3.dao.CalificacionDAO;
import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Calificacion;
import uy.edu.utu.crea3.model.ResumenMateria;
import uy.edu.utu.crea3.model.Usuario;
import uy.edu.utu.crea3.util.Validador;

import java.util.List;

public class CalificacionServiceImpl implements CalificacionService {

    private final CalificacionDAO dao;

    public CalificacionServiceImpl(CalificacionDAO dao) {
        this.dao = dao;
    }

    @Override
    public void registrar(Usuario actor, int idEstudiante, int idMateria, float nota, String tipo) throws CreaException {
        PermisosUtil.exigirGestion(actor);
        Calificacion c = new Calificacion();
        c.setIdEstudiante(Validador.id(idEstudiante, "ID Estudiante"));
        c.setIdMateria(Validador.id(idMateria, "ID Materia"));
        c.setNota(Validador.nota(nota));
        c.setTipo(Validador.tipo(tipo));
        if (!dao.registrar(c)) {
            throw new CreaException("No se pudo registrar la calificación.");
        }
    }

    @Override
    public void modificar(Usuario actor, int idCalificacion, float nota, String tipo) throws CreaException {
        PermisosUtil.exigirGestion(actor);
        Calificacion c = new Calificacion();
        c.setIdCalificacion(Validador.id(idCalificacion, "ID Calificación"));
        c.setNota(Validador.nota(nota));
        c.setTipo(Validador.tipo(tipo));
        if (!dao.actualizar(c)) {
            throw new CreaException("La calificación indicada no existe.");
        }
    }

    @Override
    public void eliminar(Usuario actor, int idCalificacion) throws CreaException {
        PermisosUtil.exigirGestion(actor);
        if (!dao.eliminar(Validador.id(idCalificacion, "ID Calificación"))) {
            throw new CreaException("La calificación indicada no existe.");
        }
    }

    @Override
    public List<Calificacion> buscar(Usuario actor, Integer idEstudiante, Integer idMateria, String tipo, Float notaMinima)
            throws CreaException {
        return dao.buscar(PermisosUtil.alcance(actor, idEstudiante), idMateria, tipo, notaMinima);
    }

    @Override
    public List<ResumenMateria> promediosPorMateria(Usuario actor, Integer idEstudiante) throws CreaException {
        return dao.promediosPorMateria(PermisosUtil.alcance(actor, idEstudiante));
    }
}
