package uy.edu.utu.crea3.service;

import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Tarea;
import uy.edu.utu.crea3.model.Usuario;

import java.util.List;

public interface TareaService {
    void crear(Usuario actor, int idMateria, String titulo, String descripcion, String fechaLimite) throws CreaException;

    void modificar(Usuario actor, int idTarea, String titulo, String descripcion, String fechaLimite) throws CreaException;

    void eliminar(Usuario actor, int idTarea) throws CreaException;

    List<Tarea> buscar(Integer idMateria, String texto) throws CreaException;
}
