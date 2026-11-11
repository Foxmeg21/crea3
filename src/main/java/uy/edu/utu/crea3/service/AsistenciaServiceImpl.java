package uy.edu.utu.crea3.service;

import uy.edu.utu.crea3.dao.AsistenciaDAO;
import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Asistencia;
import uy.edu.utu.crea3.model.Usuario;
import uy.edu.utu.crea3.util.Validador;

import java.util.List;
import java.util.Map;

public class AsistenciaServiceImpl implements AsistenciaService {

    private final AsistenciaDAO dao;

    public AsistenciaServiceImpl(AsistenciaDAO dao) {
        this.dao = dao;
    }

    @Override
    public void registrar(Usuario actor, int idEstudiante, int idMateria, String estado) throws CreaException {
        PermisosUtil.exigirGestion(actor);
        Asistencia a = new Asistencia();
        a.setIdEstudiante(Validador.id(idEstudiante, "ID Estudiante"));
        a.setIdMateria(Validador.id(idMateria, "ID Materia"));
        a.setEstado(Validador.estado(estado));
        if (!dao.registrar(a)) {
            throw new CreaException("No se pudo registrar la asistencia.");
        }
    }

    @Override
    public void modificarEstado(Usuario actor, int idAsistencia, String estado) throws CreaException {
        PermisosUtil.exigirGestion(actor);
        if (!dao.actualizarEstado(Validador.id(idAsistencia, "ID Asistencia"), Validador.estado(estado))) {
            throw new CreaException("La asistencia indicada no existe.");
        }
    }

    @Override
    public void eliminar(Usuario actor, int idAsistencia) throws CreaException {
        PermisosUtil.exigirGestion(actor);
        if (!dao.eliminar(Validador.id(idAsistencia, "ID Asistencia"))) {
            throw new CreaException("La asistencia indicada no existe.");
        }
    }

    @Override
    public List<Asistencia> buscar(Usuario actor, Integer idEstudiante, Integer idMateria, String estado)
            throws CreaException {
        return dao.buscar(PermisosUtil.alcance(actor, idEstudiante), idMateria, estado);
    }

    @Override
    public Map<String, Integer> resumenPorEstado(Usuario actor, Integer idEstudiante) throws CreaException {
        return dao.contarPorEstado(PermisosUtil.alcance(actor, idEstudiante));
    }
}
