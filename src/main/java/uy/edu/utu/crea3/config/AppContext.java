package uy.edu.utu.crea3.config;

import uy.edu.utu.crea3.dao.*;
import uy.edu.utu.crea3.service.*;

/**
 * Ensamblado manual de las capas: presentación -> servicio (interfaz) -> DAO (interfaz).
 * La GUI solo conoce las interfaces de servicio.
 */
public final class AppContext {

    private static final UsuarioService USUARIOS = new UsuarioServiceImpl(new UsuarioDAOImpl());
    private static final CalificacionService CALIFICACIONES = new CalificacionServiceImpl(new CalificacionDAOImpl());
    private static final AsistenciaService ASISTENCIAS = new AsistenciaServiceImpl(new AsistenciaDAOImpl());
    private static final TareaService TAREAS = new TareaServiceImpl(new TareaDAOImpl());
    private static final EvaluacionService EVALUACIONES = new EvaluacionServiceImpl(new EvaluacionDAOImpl());
    private static final SistemaService SISTEMA = new SistemaServiceImpl();

    private AppContext() {
    }

    public static UsuarioService usuarios() { return USUARIOS; }
    public static CalificacionService calificaciones() { return CALIFICACIONES; }
    public static AsistenciaService asistencias() { return ASISTENCIAS; }
    public static TareaService tareas() { return TAREAS; }
    public static EvaluacionService evaluaciones() { return EVALUACIONES; }
    public static SistemaService sistema() { return SISTEMA; }
}
