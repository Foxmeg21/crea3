package uy.edu.utu.crea3.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import uy.edu.utu.crea3.config.AppContext;
import uy.edu.utu.crea3.exception.CreaException;
import uy.edu.utu.crea3.model.Usuario;
import uy.edu.utu.crea3.util.Validador;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Capa de presentación web (reemplaza a la interfaz Swing). Usa solo el servidor HTTP del JDK.
 * Solo conoce las interfaces de servicio de AppContext. Escucha en el puerto de la variable PORT
 * (Render la define) o en 8080.
 */
public final class WebServer {

    private static final Logger LOG = Logger.getLogger(WebServer.class.getName());
    private static final String COOKIE = "CREA3SID";

    private WebServer() {
    }

    public static void iniciar() throws IOException {
        int puerto = 8080;
        String v = System.getenv("PORT");
        if (v != null && !v.isBlank()) {
            puerto = Integer.parseInt(v.trim());
        }
        HttpServer server = HttpServer.create(new InetSocketAddress(puerto), 0);
        server.createContext("/", WebServer::manejar);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        LOG.info("CREA 3 escuchando en el puerto " + puerto);
    }

    // ---------- despachador ----------

    private static void manejar(HttpExchange ex) throws IOException {
        try {
            Peticion p = new Peticion(ex);
            cabecerasDeSeguridad(ex);
            enrutar(p);
        } catch (Exception e) {
            // R8: el usuario nunca ve el detalle interno; queda solo en el log
            LOG.log(Level.SEVERE, "Error no controlado", e);
            try {
                responder(ex, 500, Vistas.error(500, "Ocurrió un error interno. Intente nuevamente."));
            } catch (IOException ignorada) {
                // la conexión ya se cerró
            }
        } finally {
            ex.close();
        }
    }

    private static void enrutar(Peticion p) throws IOException {
        String ruta = p.ruta;
        boolean post = "POST".equals(p.metodo);
        if (!post && !"GET".equals(p.metodo)) {
            responder(p.ex, 405, Vistas.error(405, "Método no permitido."));
            return;
        }

        // Rutas públicas
        switch (ruta) {
            case "/health" -> { health(p); return; }
            case "/static/estilo.css" -> { estilo(p); return; }
            case "/login" -> { if (post) iniciarSesion(p); else responder(p.ex, 200, Vistas.login(null)); return; }
            default -> { }
        }

        // Rutas privadas: exigen sesión
        Sesiones.Sesion s = Sesiones.obtener(p.cookie(COOKIE));
        if (s == null) {
            redirigir(p.ex, "/login", null);
            return;
        }
        if (post && !csrfValido(s, p.p("csrf"))) {
            responder(p.ex, 403, Vistas.error(403, "Solicitud no autorizada. Vuelva a intentarlo."));
            return;
        }
        Usuario u = s.usuario;

        try {
            switch (ruta) {
                case "/" -> redirigir(p.ex, "/panel", null);
                case "/logout" -> {
                    if (!post) { redirigir(p.ex, "/panel", null); return; }
                    Sesiones.cerrar(s.id);
                    redirigir(p.ex, "/login", "Max-Age=0");
                }
                case "/panel" -> responder(p.ex, 200, Vistas.panel(ctx(s),
                        AppContext.calificaciones().promediosPorMateria(u, null),
                        AppContext.asistencias().resumenPorEstado(u, null)));

                case "/calificaciones" -> responder(p.ex, 200, Vistas.calificaciones(ctx(s),
                        AppContext.calificaciones().buscar(u,
                                Validador.idOpcional(p.p("idEstudiante"), "Estudiante"),
                                Validador.idOpcional(p.p("idMateria"), "Materia"),
                                vacioANull(p.p("tipo")),
                                Validador.notaOpcional(p.p("notaMinima"))),
                        p.todos()));
                case "/calificaciones/crear" -> {
                    AppContext.calificaciones().registrar(u, Validador.id(p.p("idEstudiante"), "Estudiante"),
                            Validador.id(p.p("idMateria"), "Materia"), Validador.nota(p.p("nota")), p.p("tipo"));
                    volver(p, s, "/calificaciones", "Calificación registrada.");
                }
                case "/calificaciones/modificar" -> {
                    AppContext.calificaciones().modificar(u, Validador.id(p.p("id"), "ID"),
                            Validador.nota(p.p("nota")), p.p("tipo"));
                    volver(p, s, "/calificaciones", "Calificación modificada.");
                }
                case "/calificaciones/eliminar" -> {
                    AppContext.calificaciones().eliminar(u, Validador.id(p.p("id"), "ID"));
                    volver(p, s, "/calificaciones", "Calificación eliminada.");
                }

                case "/asistencias" -> responder(p.ex, 200, Vistas.asistencias(ctx(s),
                        AppContext.asistencias().buscar(u,
                                Validador.idOpcional(p.p("idEstudiante"), "Estudiante"),
                                Validador.idOpcional(p.p("idMateria"), "Materia"),
                                vacioANull(p.p("estado"))),
                        p.todos()));
                case "/asistencias/crear" -> {
                    AppContext.asistencias().registrar(u, Validador.id(p.p("idEstudiante"), "Estudiante"),
                            Validador.id(p.p("idMateria"), "Materia"), p.p("estado"));
                    volver(p, s, "/asistencias", "Asistencia registrada.");
                }
                case "/asistencias/modificar" -> {
                    AppContext.asistencias().modificarEstado(u, Validador.id(p.p("id"), "ID"), p.p("estado"));
                    volver(p, s, "/asistencias", "Asistencia modificada.");
                }
                case "/asistencias/eliminar" -> {
                    AppContext.asistencias().eliminar(u, Validador.id(p.p("id"), "ID"));
                    volver(p, s, "/asistencias", "Asistencia eliminada.");
                }

                case "/tareas" -> responder(p.ex, 200, Vistas.tareas(ctx(s),
                        AppContext.tareas().buscar(Validador.idOpcional(p.p("idMateria"), "Materia"),
                                vacioANull(p.p("texto"))),
                        p.todos()));
                case "/tareas/crear" -> {
                    AppContext.tareas().crear(u, Validador.id(p.p("idMateria"), "Materia"), p.p("titulo"),
                            p.p("descripcion"), fechaHora(p.p("fechaLimite")));
                    volver(p, s, "/tareas", "Tarea creada.");
                }
                case "/tareas/modificar" -> {
                    AppContext.tareas().modificar(u, Validador.id(p.p("id"), "ID"), p.p("titulo"),
                            p.p("descripcion"), fechaHora(p.p("fechaLimite")));
                    volver(p, s, "/tareas", "Tarea modificada.");
                }
                case "/tareas/eliminar" -> {
                    AppContext.tareas().eliminar(u, Validador.id(p.p("id"), "ID"));
                    volver(p, s, "/tareas", "Tarea eliminada.");
                }

                case "/examen" -> {
                    if (!post) {
                        responder(p.ex, 200, Vistas.examen(ctx(s)));
                    } else {
                        AppContext.evaluaciones().registrarExamenConAsistencia(u,
                                Validador.id(p.p("idEstudiante"), "Estudiante"),
                                Validador.id(p.p("idMateria"), "Materia"), Validador.nota(p.p("nota")));
                        volver(p, s, "/examen", "Examen y asistencia registrados.");
                    }
                }
                default -> responder(p.ex, 404, Vistas.error(404, "Página no encontrada."));
            }
        } catch (CreaException e) {
            // Mensaje apto para el usuario (R8). Se muestra en la misma sección de la que vino.
            String destino = post ? origen(ruta) : ruta;
            s.mensajeError = e.getMessage();
            redirigir(p.ex, destino, null);
        }
    }

    // ---------- rutas públicas ----------

    /** R12: 200 si la aplicación y la base funcionan, 503 si no. */
    private static void health(Peticion p) throws IOException {
        boolean ok = AppContext.sistema().baseDeDatosDisponible();
        byte[] cuerpo = (ok ? "OK" : "DB_UNAVAILABLE").getBytes(StandardCharsets.UTF_8);
        p.ex.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        enviar(p.ex, ok ? 200 : 503, cuerpo);
    }

    private static void estilo(Peticion p) throws IOException {
        p.ex.getResponseHeaders().set("Content-Type", "text/css; charset=utf-8");
        p.ex.getResponseHeaders().set("Cache-Control", "public, max-age=3600");
        enviar(p.ex, 200, Vistas.CSS.getBytes(StandardCharsets.UTF_8));
    }

    private static void iniciarSesion(Peticion p) throws IOException {
        try {
            Usuario u = AppContext.usuarios().iniciarSesion(p.p("email"), p.p("password"));
            Sesiones.Sesion s = Sesiones.crear(u);   // identificador nuevo: evita fijación de sesión
            String cookie = COOKIE + "=" + s.id + "; Path=/; HttpOnly; SameSite=Strict" + (p.esHttps() ? "; Secure" : "");
            p.ex.getResponseHeaders().add("Set-Cookie", cookie);
            redirigir(p.ex, "/panel", null);
        } catch (CreaException e) {
            responder(p.ex, 401, Vistas.login(e.getMessage()));
        }
    }

    // ---------- utilidades ----------

    private static Vistas.Ctx ctx(Sesiones.Sesion s) {
        Vistas.Ctx c = new Vistas.Ctx(s.usuario, s.csrf, s.mensajeOk, s.mensajeError);
        s.mensajeOk = null;       // los mensajes se muestran una sola vez
        s.mensajeError = null;
        return c;
    }

    private static void volver(Peticion p, Sesiones.Sesion s, String destino, String mensaje) throws IOException {
        s.mensajeOk = mensaje;
        redirigir(p.ex, destino, null);
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

    private static void cabecerasDeSeguridad(HttpExchange ex) {
        var h = ex.getResponseHeaders();
        h.set("Content-Security-Policy", "default-src 'self'; frame-ancestors 'none'; form-action 'self'");
        h.set("X-Content-Type-Options", "nosniff");
        h.set("X-Frame-Options", "DENY");
        h.set("Referrer-Policy", "no-referrer");
        h.set("Cache-Control", "no-store");
    }

    private static void responder(HttpExchange ex, int codigo, String html) throws IOException {
        ex.getResponseHeaders().set("Content-Type", "text/html; charset=utf-8");
        enviar(ex, codigo, html.getBytes(StandardCharsets.UTF_8));
    }

    private static void enviar(HttpExchange ex, int codigo, byte[] cuerpo) throws IOException {
        ex.sendResponseHeaders(codigo, cuerpo.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(cuerpo);
        }
    }

    /** Redirección 303; si cookieExtra no es null se usa para expirar la cookie de sesión. */
    private static void redirigir(HttpExchange ex, String destino, String cookieExtra) throws IOException {
        if (cookieExtra != null) {
            ex.getResponseHeaders().add("Set-Cookie", COOKIE + "=; Path=/; HttpOnly; SameSite=Strict; " + cookieExtra);
        }
        ex.getResponseHeaders().set("Location", destino);
        ex.sendResponseHeaders(303, -1);
    }
}
