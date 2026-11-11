package uy.edu.utu.crea3.web;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.HttpStatus;
import uy.edu.utu.crea3.config.AppContext;
import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Usuario;
import uy.edu.utu.crea3.util.Validador;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Capa de presentación web sobre Javalin 6 (Jetty embebido). Solo conoce las interfaces de servicio
 * de AppContext. Escucha en el puerto de la variable PORT (Render y Railway la definen) o en 8080.
 */
public final class WebServer {

    private static final Logger LOG = Logger.getLogger(WebServer.class.getName());
    private static final String COOKIE = "CREA3SID";
    private static final String HTML = "text/html; charset=utf-8";

    /** Acción de una ruta privada: ya llega con la sesión validada (y el CSRF verificado en POST). */
    @FunctionalInterface
    private interface Accion {
        void ejecutar(Peticion p, Sesiones.Sesion s) throws Exception;
    }

    private WebServer() {
    }

    public static void iniciar() {
        int puerto = 8080;
        String v = System.getenv("PORT");
        if (v != null && !v.isBlank()) {
            puerto = Integer.parseInt(v.trim());
        }

        Javalin app = Javalin.create(config -> {
            config.showJavalinBanner = false;
            config.http.maxRequestSize = 64 * 1024L;
        });

        // Cabeceras de seguridad en todas las respuestas
        app.before(WebServer::cabecerasDeSeguridad);

        // R8: el usuario nunca ve el detalle interno; queda solo en el log
        app.exception(Exception.class, (e, ctx) -> {
            LOG.log(Level.SEVERE, "Error no controlado", e);
            ctx.status(500).contentType(HTML).result(Vistas.error(500, "Ocurrió un error interno. Intente nuevamente."));
        });
        app.error(404, ctx -> ctx.contentType(HTML).result(Vistas.error(404, "Página no encontrada.")));
        app.error(405, ctx -> ctx.contentType(HTML).result(Vistas.error(405, "Método no permitido.")));

        rutasPublicas(app);
        rutasPrivadas(app);

        Runtime.getRuntime().addShutdownHook(new Thread(app::stop));
        app.start("0.0.0.0", puerto);
        LOG.info("CREA 3 (Javalin) escuchando en el puerto " + puerto);
    }

    // ---------- rutas públicas ----------

    private static void rutasPublicas(Javalin app) {
        app.get("/health", WebServer::health);
        app.get("/static/estilo.css", WebServer::estilo);
        app.get("/login", ctx -> html(ctx, 200, Vistas.login(null)));
        app.post("/login", WebServer::iniciarSesion);
    }

    /** R12: 200 si la aplicación y la base funcionan, 503 si no. */
    private static void health(Context ctx) {
        boolean ok = AppContext.sistema().baseDeDatosDisponible();
        ctx.status(ok ? 200 : 503)
                .contentType("text/plain; charset=utf-8")
                .result(ok ? "OK" : "DB_UNAVAILABLE");
    }

    private static void estilo(Context ctx) {
        ctx.header("Cache-Control", "public, max-age=3600");
        ctx.contentType("text/css; charset=utf-8").result(Vistas.CSS);
    }

    private static void iniciarSesion(Context ctx) {
        Peticion p = new Peticion(ctx);
        try {
            Usuario u = AppContext.usuarios().iniciarSesion(p.p("email"), p.p("password"));
            Sesiones.Sesion s = Sesiones.crear(u);   // identificador nuevo: evita fijación de sesión
            String cookie = COOKIE + "=" + s.id + "; Path=/; HttpOnly; SameSite=Strict" + (p.esHttps() ? "; Secure" : "");
            ctx.res().addHeader("Set-Cookie", cookie);
            redirigir(ctx, "/panel");
        } catch (CreaException e) {
            html(ctx, 401, Vistas.login(e.getMessage()));
        }
    }

    // ---------- rutas privadas (exigen sesión) ----------

    private static void rutasPrivadas(Javalin app) {
        app.get("/", privada((p, s) -> redirigir(p.ctx, "/panel")));

        app.get("/logout", privada((p, s) -> redirigir(p.ctx, "/panel")));
        app.post("/logout", privada((p, s) -> {
            Sesiones.cerrar(s.id);
            p.ctx.res().addHeader("Set-Cookie", COOKIE + "=; Path=/; HttpOnly; SameSite=Strict; Max-Age=0");
            redirigir(p.ctx, "/login");
        }));

        app.get("/panel", privada((p, s) -> {
            Usuario u = s.usuario;
            html(p.ctx, 200, Vistas.panel(ctx(s),
                    AppContext.calificaciones().promediosPorMateria(u, null),
                    AppContext.asistencias().resumenPorEstado(u, null)));
        }));

        // --- calificaciones ---
        app.get("/calificaciones", privada((p, s) -> {
            Usuario u = s.usuario;
            html(p.ctx, 200, Vistas.calificaciones(ctx(s),
                    AppContext.calificaciones().buscar(u,
                            Validador.idOpcional(p.p("idEstudiante"), "Estudiante"),
                            Validador.idOpcional(p.p("idMateria"), "Materia"),
                            vacioANull(p.p("tipo")),
                            Validador.notaOpcional(p.p("notaMinima"))),
                    p.todos()));
        }));
        app.post("/calificaciones/crear", privada((p, s) -> {
            AppContext.calificaciones().registrar(s.usuario, Validador.id(p.p("idEstudiante"), "Estudiante"),
                    Validador.id(p.p("idMateria"), "Materia"), Validador.nota(p.p("nota")), p.p("tipo"));
            volver(p, s, "/calificaciones", "Calificación registrada.");
        }));
        app.post("/calificaciones/modificar", privada((p, s) -> {
            AppContext.calificaciones().modificar(s.usuario, Validador.id(p.p("id"), "ID"),
                    Validador.nota(p.p("nota")), p.p("tipo"));
            volver(p, s, "/calificaciones", "Calificación modificada.");
        }));
        app.post("/calificaciones/eliminar", privada((p, s) -> {
            AppContext.calificaciones().eliminar(s.usuario, Validador.id(p.p("id"), "ID"));
            volver(p, s, "/calificaciones", "Calificación eliminada.");
        }));

        // --- asistencias ---
        app.get("/asistencias", privada((p, s) -> {
            html(p.ctx, 200, Vistas.asistencias(ctx(s),
                    AppContext.asistencias().buscar(s.usuario,
                            Validador.idOpcional(p.p("idEstudiante"), "Estudiante"),
                            Validador.idOpcional(p.p("idMateria"), "Materia"),
                            vacioANull(p.p("estado"))),
                    p.todos()));
        }));
        app.post("/asistencias/crear", privada((p, s) -> {
            AppContext.asistencias().registrar(s.usuario, Validador.id(p.p("idEstudiante"), "Estudiante"),
                    Validador.id(p.p("idMateria"), "Materia"), p.p("estado"));
            volver(p, s, "/asistencias", "Asistencia registrada.");
        }));
        app.post("/asistencias/modificar", privada((p, s) -> {
            AppContext.asistencias().modificarEstado(s.usuario, Validador.id(p.p("id"), "ID"), p.p("estado"));
            volver(p, s, "/asistencias", "Asistencia modificada.");
        }));
        app.post("/asistencias/eliminar", privada((p, s) -> {
            AppContext.asistencias().eliminar(s.usuario, Validador.id(p.p("id"), "ID"));
            volver(p, s, "/asistencias", "Asistencia eliminada.");
        }));

        // --- tareas ---
        app.get("/tareas", privada((p, s) -> {
            html(p.ctx, 200, Vistas.tareas(ctx(s),
                    AppContext.tareas().buscar(Validador.idOpcional(p.p("idMateria"), "Materia"),
                            vacioANull(p.p("texto"))),
                    p.todos()));
        }));
        app.post("/tareas/crear", privada((p, s) -> {
            AppContext.tareas().crear(s.usuario, Validador.id(p.p("idMateria"), "Materia"), p.p("titulo"),
                    p.p("descripcion"), fechaHora(p.p("fechaLimite")));
            volver(p, s, "/tareas", "Tarea creada.");
        }));
        app.post("/tareas/modificar", privada((p, s) -> {
            AppContext.tareas().modificar(s.usuario, Validador.id(p.p("id"), "ID"), p.p("titulo"),
                    p.p("descripcion"), fechaHora(p.p("fechaLimite")));
            volver(p, s, "/tareas", "Tarea modificada.");
        }));
        app.post("/tareas/eliminar", privada((p, s) -> {
            AppContext.tareas().eliminar(s.usuario, Validador.id(p.p("id"), "ID"));
            volver(p, s, "/tareas", "Tarea eliminada.");
        }));

        // --- examen + asistencia (R7: transacción) ---
        app.get("/examen", privada((p, s) -> html(p.ctx, 200, Vistas.examen(ctx(s)))));
        app.post("/examen", privada((p, s) -> {
            AppContext.evaluaciones().registrarExamenConAsistencia(s.usuario,
                    Validador.id(p.p("idEstudiante"), "Estudiante"),
                    Validador.id(p.p("idMateria"), "Materia"), Validador.nota(p.p("nota")));
            volver(p, s, "/examen", "Examen y asistencia registrados.");
        }));
    }

    /** Envuelve una acción: exige sesión, verifica CSRF en POST y traduce CreaException a un mensaje (R8). */
    private static Handler privada(Accion accion) {
        return ctx -> {
            Peticion p = new Peticion(ctx);
            Sesiones.Sesion s = Sesiones.obtener(p.cookie(COOKIE));
            if (s == null) {
                redirigir(ctx, "/login");
                return;
            }
            boolean post = "POST".equals(p.metodo);
            if (post && !csrfValido(s, p.p("csrf"))) {
                html(ctx, 403, Vistas.error(403, "Solicitud no autorizada. Vuelva a intentarlo."));
                return;
            }
            try {
                accion.ejecutar(p, s);
            } catch (CreaException e) {
                // Mensaje apto para el usuario (R8). Se muestra en la misma sección de la que vino.
                s.mensajeError = e.getMessage();
                redirigir(ctx, post ? origen(p.ruta) : p.ruta);
            }
        };
    }

    // ---------- utilidades ----------

    private static Vistas.Ctx ctx(Sesiones.Sesion s) {
        Vistas.Ctx c = new Vistas.Ctx(s.usuario, s.csrf, s.mensajeOk, s.mensajeError);
        s.mensajeOk = null;       // los mensajes se muestran una sola vez
        s.mensajeError = null;
        return c;
    }

    private static void volver(Peticion p, Sesiones.Sesion s, String destino, String mensaje) {
        s.mensajeOk = mensaje;
        redirigir(p.ctx, destino);
    }

    /** Ruta de la pantalla a la que se vuelve si una acción POST falla: "/tareas/crear" -> "/tareas". */
    private static String origen(String ruta) {
        int i = ruta.indexOf('/', 1);
        return i < 0 ? ruta : ruta.substring(0, i);
    }

    private static String vacioANull(String v) {
        return (v == null || v.isBlank()) ? null : v.trim();
    }

    /** El campo datetime-local envía "2026-12-01T23:59"; el Validador espera "2026-12-01 23:59:00". */
    private static String fechaHora(String v) {
        if (v == null) {
            return null;
        }
        String t = v.trim().replace('T', ' ');
        return t.length() == 16 ? t + ":00" : t;
    }

    private static boolean csrfValido(Sesiones.Sesion s, String recibido) {
        return MessageDigest.isEqual(s.csrf.getBytes(StandardCharsets.UTF_8), recibido.getBytes(StandardCharsets.UTF_8));
    }

    private static void cabecerasDeSeguridad(Context ctx) {
        ctx.header("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none'; form-action 'self'");
        ctx.header("X-Content-Type-Options", "nosniff");
        ctx.header("X-Frame-Options", "DENY");
        ctx.header("Referrer-Policy", "no-referrer");
        ctx.header("Cache-Control", "no-store");
    }

    private static void html(Context ctx, int codigo, String html) {
        ctx.status(codigo).contentType(HTML).result(html);
    }

    /** Redirección 303 (después de un POST el navegador vuelve a pedir con GET). */
    private static void redirigir(Context ctx, String destino) {
        ctx.redirect(destino, HttpStatus.SEE_OTHER);
    }
}
