package uy.edu.utu.crea3.service;

import uy.edu.utu.crea3.dao.TareaDAO;
import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Tarea;
import uy.edu.utu.crea3.model.Usuario;
import uy.edu.utu.crea3.util.Validador;

import java.util.List;

public class TareaServiceImpl implements TareaService {

    private final TareaDAO dao;

    public TareaServiceImpl(TareaDAO dao) {
        this.dao = dao;
    }

    @Override
    public void crear(Usuario actor, int idMateria, String titulo, String descripcion, String fechaLimite)
            throws CreaException {
        PermisosUtil.exigirGestion(actor);
        Tarea t = new Tarea();
        t.setIdMateria(Validador.id(idMateria, "ID Materia"));
        t.setTitulo(Validador.texto(titulo, "Título", 100));
        t.setDescripcion(Validador.texto(descripcion, "Descripción", 500));
        t.setFecha(Validador.fechaHora(fechaLimite));
        if (!dao.crear(t)) {
            throw new CreaException("No se pudo crear la tarea.");
        }
    }

    @Override
    public void modificar(Usuario actor, int idTarea, String titulo, String descripcion, String fechaLimite)
            throws CreaException {
        PermisosUtil.exigirGestion(actor);
        Tarea t = new Tarea();
        t.setIdTarea(Validador.id(idTarea, "ID Tarea"));
        t.setTitulo(Validador.texto(titulo, "Título", 100));
        t.setDescripcion(Validador.texto(descripcion, "Descripción", 500));
        t.setFecha(Validador.fechaHora(fechaLimite));
        if (!dao.actualizar(t)) {
            throw new CreaException("La tarea indicada no existe.");
        }
    }

    @Override
    public void eliminar(Usuario actor, int idTarea) throws CreaException {
        PermisosUtil.exigirGestion(actor);
        if (!dao.eliminar(Validador.id(idTarea, "ID Tarea"))) {
            throw new CreaException("La tarea indicada no existe.");
        }
    }

    @Override
    public List<Tarea> buscar(Integer idMateria, String texto) throws CreaException {
        return dao.buscar(idMateria, texto);
    }
}
