package uy.edu.utu.crea3.web;

import uy.edu.utu.crea3.model.Asistencia;
import uy.edu.utu.crea3.model.Calificacion;
import uy.edu.utu.crea3.model.ResumenMateria;
import uy.edu.utu.crea3.model.Tarea;
import uy.edu.utu.crea3.model.Usuario;
import uy.edu.utu.crea3.util.Validador;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/** Presentación: genera el HTML. Todo texto que viene de la base o del usuario pasa por esc(). */
final class Vistas {

    /** Datos comunes de cada página. */
    record Ctx(Usuario usuario, String csrf, String ok, String error) {
        boolean docente() {
            return usuario.puedeGestionar();
        }
    }

    static final String CSS = """
            *{box-sizing:border-box}
            body{font-family:system-ui,Arial,sans-serif;margin:0;background:#f3f5f9;color:#1d2433}
            header{background:#1f3a68;color:#fff;padding:.6rem 1.2rem;display:flex;gap:1rem;align-items:center;flex-wrap:wrap}
            header a{color:#fff;text-decoration:none;padding:.3rem .6rem;border-radius:4px}
            header a:hover{background:#2f528f}
            header .sp{flex:1}
            main{max-width:1000px;margin:1.2rem auto;padding:0 1rem}
            section{background:#fff;border-radius:8px;padding:1rem 1.2rem;margin-bottom:1rem;box-shadow:0 1px 3px #0002}
            h1,h2{margin-top:0}
            table{border-collapse:collapse;width:100%}
            th,td{border-bottom:1px solid #dde2ec;padding:.4rem;text-align:left;vertical-align:middle}
            th{background:#eef1f7}
            input,select,button{font:inherit;padding:.35rem .5rem;margin:.15rem .2rem .15rem 0}
            input[type=number]{width:6rem}
            button{background:#1f3a68;color:#fff;border:0;border-radius:4px;cursor:pointer}
            button.peligro{background:#b3261e}
            button.link{background:none;color:#fff;padding:.3rem .6rem}
            form.en-linea{display:inline}
            .ok{background:#e3f4e6;border-left:4px solid #2e7d32;padding:.6rem;margin-bottom:1rem}
            .error{background:#fde7e7;border-left:4px solid #b3261e;padding:.6rem;margin-bottom:1rem}
            .login{max-width:380px;margin:4rem auto}
            .login input{width:100%}
            small{color:#5a6477}
            """;

    private Vistas() {
    }

    // ---------- utilidades ----------

    /** Escapa HTML para evitar XSS. */
    static String esc(Object o) {
        if (o == null) {
            return "";
        }
        String s = o.toString();
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    private static String nota(double n) {
        return String.format(Locale.ROOT, "%.1f", n);
    }

    private static String csrf(Ctx c) {
        return "<input type=\"hidden\" name=\"csrf\" value=\"" + esc(c.csrf()) + "\">";
    }

    private static String opciones(List<String> valores, String elegido, boolean conVacio) {
        StringBuilder sb = new StringBuilder();
        if (conVacio) {
            sb.append("<option value=\"\">(todos)</option>");
        }
        for (String v : valores) {
            sb.append("<option value=\"").append(esc(v)).append('"')
                    .append(v.equals(elegido) ? " selected" : "").append('>').append(esc(v)).append("</option>");
        }
        return sb.toString();
    }

    private static String pagina(String titulo, Ctx c, String cuerpo) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html><html lang=\"es\"><head><meta charset=\"UTF-8\">")
                .append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">")
                .append("<title>").append(esc(titulo)).append(" · CREA 3</title>")
                .append("<link rel=\"stylesheet\" href=\"/static/estilo.css\"></head><body>");
        if (c != null) {
            sb.append("<header><strong>CREA 3</strong>")
                    .append("<a href=\"/panel\">Panel</a>")
                    .append("<a href=\"/calificaciones\">Calificaciones</a>")
                    .append("<a href=\"/asistencias\">Asistencias</a>")
                    .append("<a href=\"/tareas\">Tareas</a>");
            if (c.docente()) {
                sb.append("<a href=\"/examen\">Registrar examen</a>");
            }
            sb.append("<span class=\"sp\"></span><span>").append(esc(c.usuario().getNombreCompleto()))
                    .append(" (").append(esc(c.usuario().getRol())).append(")</span>")
                    .append("<form class=\"en-linea\" method=\"post\" action=\"/logout\">").append(csrf(c))
                    .append("<button class=\"link\" type=\"submit\">Salir</button></form></header>");
        }
        sb.append("<main>");
        if (c != null && c.ok() != null) {
            sb.append("<div class=\"ok\">").append(esc(c.ok())).append("</div>");
        }
        if (c != null && c.error() != null) {
            sb.append("<div class=\"error\">").append(esc(c.error())).append("</div>");
        }
        sb.append(cuerpo).append("</main></body></html>");
        return sb.toString();
    }

    // ---------- páginas ----------

    static String login(String error) {
        String aviso = error == null ? "" : "<div class=\"error\">" + esc(error) + "</div>";
        return pagina("Iniciar sesión", null, """
                <section class="login"><h1>CREA 3</h1><p>Plataforma educativa</p>
                """ + aviso + """
                <form method="post" action="/login">
                <label>Correo electrónico<br><input type="email" name="email" maxlength="100" required autofocus></label><br>
                <label>Contraseña<br><input type="password" name="password" maxlength="72" required></label><br>
                <button type="submit">Ingresar</button></form></section>
                """);
    }

    static String panel(Ctx c, List<ResumenMateria> promedios, Map<String, Integer> asistencia) {
        StringBuilder sb = new StringBuilder("<section><h1>")
                .append(esc(c.usuario().getTituloPanel())).append("</h1></section>");
        sb.append("<section><h2>Promedio por materia</h2><table><tr><th>Materia</th><th>Cantidad de notas</th><th>Promedio</th></tr>");
        for (ResumenMateria r : promedios) {
            sb.append("<tr><td>").append(esc(r.getMateria())).append("</td><td>").append(r.getCantidad())
                    .append("</td><td>").append(nota(r.getPromedio())).append("</td></tr>");
        }
        if (promedios.isEmpty()) {
            sb.append("<tr><td colspan=\"3\">Sin calificaciones.</td></tr>");
        }
        sb.append("</table></section><section><h2>Asistencias por estado</h2><table><tr><th>Estado</th><th>Cantidad</th></tr>");
        for (Map.Entry<String, Integer> e : asistencia.entrySet()) {
            sb.append("<tr><td>").append(esc(e.getKey())).append("</td><td>").append(e.getValue()).append("</td></tr>");
        }
        if (asistencia.isEmpty()) {
            sb.append("<tr><td colspan=\"2\">Sin asistencias.</td></tr>");
        }
        sb.append("</table></section>");
        return pagina("Panel", c, sb.toString());
    }

    static String calificaciones(Ctx c, List<Calificacion> lista, Map<String, String> f) {
        StringBuilder sb = new StringBuilder("<section><h1>Calificaciones</h1>");
        sb.append("<form method=\"get\" action=\"/calificaciones\"><strong>Filtrar:</strong> ");
        if (c.docente()) {
            sb.append("Estudiante (ID) <input type=\"number\" name=\"idEstudiante\" min=\"1\" value=\"")
                    .append(esc(f.getOrDefault("idEstudiante", ""))).append("\"> ");
        }
        sb.append("Materia (ID) <input type=\"number\" name=\"idMateria\" min=\"1\" value=\"")
                .append(esc(f.getOrDefault("idMateria", ""))).append("\"> ")
                .append("Tipo <select name=\"tipo\">").append(opciones(Validador.TIPOS, f.get("tipo"), true)).append("</select> ")
                .append("Nota mínima <input type=\"number\" name=\"notaMinima\" min=\"1\" max=\"12\" step=\"0.5\" value=\"")
                .append(esc(f.getOrDefault("notaMinima", ""))).append("\"> ")
                .append("<button type=\"submit\">Buscar</button></form></section>");

        sb.append("<section><table><tr><th>ID</th><th>Estudiante</th><th>Materia</th><th>Nota</th><th>Fecha</th><th>Tipo</th>");
        if (c.docente()) {
            sb.append("<th>Acciones</th>");
        }
        sb.append("</tr>");
        for (Calificacion k : lista) {
            sb.append("<tr><td>").append(k.getIdCalificacion()).append("</td><td>").append(k.getIdEstudiante())
                    .append("</td><td>").append(esc(k.getNombreMateria())).append("</td>");
            if (c.docente()) {
                sb.append("<td colspan=\"3\"><form class=\"en-linea\" method=\"post\" action=\"/calificaciones/modificar\">")
                        .append(csrf(c)).append("<input type=\"hidden\" name=\"id\" value=\"").append(k.getIdCalificacion()).append("\">")
                        .append("<input type=\"number\" name=\"nota\" min=\"1\" max=\"12\" step=\"0.5\" value=\"").append(nota(k.getNota())).append("\" required> ")
                        .append(esc(k.getFecha())).append(" <select name=\"tipo\">").append(opciones(Validador.TIPOS, k.getTipo(), false))
                        .append("</select><button type=\"submit\">Guardar</button></form></td><td>")
                        .append("<form class=\"en-linea\" method=\"post\" action=\"/calificaciones/eliminar\">").append(csrf(c))
                        .append("<input type=\"hidden\" name=\"id\" value=\"").append(k.getIdCalificacion()).append("\">")
                        .append("<button class=\"peligro\" type=\"submit\">Eliminar</button></form></td>");
            } else {
                sb.append("<td>").append(nota(k.getNota())).append("</td><td>").append(esc(k.getFecha()))
                        .append("</td><td>").append(esc(k.getTipo())).append("</td>");
            }
            sb.append("</tr>");
        }
        if (lista.isEmpty()) {
            sb.append("<tr><td colspan=\"7\">Sin resultados.</td></tr>");
        }
        sb.append("</table></section>");

        if (c.docente()) {
            sb.append("<section><h2>Nueva calificación</h2><form method=\"post\" action=\"/calificaciones/crear\">").append(csrf(c))
                    .append("Estudiante (ID) <input type=\"number\" name=\"idEstudiante\" min=\"1\" required> ")
                    .append("Materia (ID) <input type=\"number\" name=\"idMateria\" min=\"1\" required> ")
                    .append("Nota <input type=\"number\" name=\"nota\" min=\"1\" max=\"12\" step=\"0.5\" required> ")
                    .append("Tipo <select name=\"tipo\">").append(opciones(Validador.TIPOS, null, false)).append("</select> ")
                    .append("<button type=\"submit\">Registrar</button></form>")
                    .append("<small>Notas de 1 a 12. La fecha se completa con el día actual.</small></section>");
        }
        return pagina("Calificaciones", c, sb.toString());
    }

    static String asistencias(Ctx c, List<Asistencia> lista, Map<String, String> f) {
        StringBuilder sb = new StringBuilder("<section><h1>Asistencias</h1>");
        sb.append("<form method=\"get\" action=\"/asistencias\"><strong>Filtrar:</strong> ");
        if (c.docente()) {
            sb.append("Estudiante (ID) <input type=\"number\" name=\"idEstudiante\" min=\"1\" value=\"")
                    .append(esc(f.getOrDefault("idEstudiante", ""))).append("\"> ");
        }
        sb.append("Materia (ID) <input type=\"number\" name=\"idMateria\" min=\"1\" value=\"")
                .append(esc(f.getOrDefault("idMateria", ""))).append("\"> ")
                .append("Estado <select name=\"estado\">").append(opciones(Validador.ESTADOS, f.get("estado"), true)).append("</select> ")
                .append("<button type=\"submit\">Buscar</button></form></section>");

        sb.append("<section><table><tr><th>ID</th><th>Estudiante</th><th>Materia</th><th>Fecha</th><th>Estado</th>");
        if (c.docente()) {
            sb.append("<th>Acciones</th>");
        }
        sb.append("</tr>");
        for (Asistencia a : lista) {
            sb.append("<tr><td>").append(a.getIdAsistencia()).append("</td><td>").append(a.getIdEstudiante())
                    .append("</td><td>").append(esc(a.getNombreMateria())).append("</td><td>").append(esc(a.getFecha())).append("</td>");
            if (c.docente()) {
                sb.append("<td><form class=\"en-linea\" method=\"post\" action=\"/asistencias/modificar\">").append(csrf(c))
                        .append("<input type=\"hidden\" name=\"id\" value=\"").append(a.getIdAsistencia()).append("\">")
                        .append("<select name=\"estado\">").append(opciones(Validador.ESTADOS, a.getEstado(), false))
                        .append("</select><button type=\"submit\">Guardar</button></form></td><td>")
                        .append("<form class=\"en-linea\" method=\"post\" action=\"/asistencias/eliminar\">").append(csrf(c))
                        .append("<input type=\"hidden\" name=\"id\" value=\"").append(a.getIdAsistencia()).append("\">")
                        .append("<button class=\"peligro\" type=\"submit\">Eliminar</button></form></td>");
            } else {
                sb.append("<td>").append(esc(a.getEstado())).append("</td>");
            }
            sb.append("</tr>");
        }
        if (lista.isEmpty()) {
            sb.append("<tr><td colspan=\"6\">Sin resultados.</td></tr>");
        }
        sb.append("</table></section>");

        if (c.docente()) {
            sb.append("<section><h2>Nueva asistencia</h2><form method=\"post\" action=\"/asistencias/crear\">").append(csrf(c))
                    .append("Estudiante (ID) <input type=\"number\" name=\"idEstudiante\" min=\"1\" required> ")
                    .append("Materia (ID) <input type=\"number\" name=\"idMateria\" min=\"1\" required> ")
                    .append("Estado <select name=\"estado\">").append(opciones(Validador.ESTADOS, null, false)).append("</select> ")
                    .append("<button type=\"submit\">Registrar</button></form></section>");
        }
        return pagina("Asistencias", c, sb.toString());
    }

    static String tareas(Ctx c, List<Tarea> lista, Map<String, String> f) {
        StringBuilder sb = new StringBuilder("<section><h1>Tareas</h1>");
        sb.append("<form method=\"get\" action=\"/tareas\"><strong>Filtrar:</strong> ")
                .append("Materia (ID) <input type=\"number\" name=\"idMateria\" min=\"1\" value=\"")
                .append(esc(f.getOrDefault("idMateria", ""))).append("\"> ")
                .append("Texto <input type=\"text\" name=\"texto\" maxlength=\"100\" value=\"")
                .append(esc(f.getOrDefault("texto", ""))).append("\"> ")
                .append("<button type=\"submit\">Buscar</button></form></section>");

        sb.append("<section><table><tr><th>ID</th><th>Materia (ID)</th><th>Título</th><th>Descripción</th><th>Fecha límite</th>");
        if (c.docente()) {
            sb.append("<th>Acciones</th>");
        }
        sb.append("</tr>");
        for (Tarea t : lista) {
            if (c.docente()) {
                String fecha = t.getFecha() == null ? "" : t.getFecha().replace(' ', 'T');
                if (fecha.length() > 16) {
                    fecha = fecha.substring(0, 16);
                }
                sb.append("<tr><td>").append(t.getIdTarea()).append("</td><td colspan=\"5\">")
                        .append("<form class=\"en-linea\" method=\"post\" action=\"/tareas/modificar\">").append(csrf(c))
                        .append("<input type=\"hidden\" name=\"id\" value=\"").append(t.getIdTarea()).append("\">")
                        .append(t.getIdMateria()).append(" <input name=\"titulo\" maxlength=\"100\" required value=\"").append(esc(t.getTitulo())).append("\"> ")
                        .append("<input name=\"descripcion\" maxlength=\"500\" required value=\"").append(esc(t.getDescripcion())).append("\"> ")
                        .append("<input type=\"datetime-local\" name=\"fechaLimite\" required value=\"").append(esc(fecha)).append("\">")
                        .append("<button type=\"submit\">Guardar</button></form> ")
                        .append("<form class=\"en-linea\" method=\"post\" action=\"/tareas/eliminar\">").append(csrf(c))
                        .append("<input type=\"hidden\" name=\"id\" value=\"").append(t.getIdTarea()).append("\">")
                        .append("<button class=\"peligro\" type=\"submit\">Eliminar</button></form></td></tr>");
            } else {
                sb.append("<tr><td>").append(t.getIdTarea()).append("</td><td>").append(t.getIdMateria()).append("</td><td>")
                        .append(esc(t.getTitulo())).append("</td><td>").append(esc(t.getDescripcion())).append("</td><td>")
                        .append(esc(t.getFecha())).append("</td></tr>");
            }
        }
        if (lista.isEmpty()) {
            sb.append("<tr><td colspan=\"6\">Sin resultados.</td></tr>");
        }
        sb.append("</table></section>");

        if (c.docente()) {
            sb.append("<section><h2>Nueva tarea</h2><form method=\"post\" action=\"/tareas/crear\">").append(csrf(c))
                    .append("Materia (ID) <input type=\"number\" name=\"idMateria\" min=\"1\" required> ")
                    .append("Título <input name=\"titulo\" maxlength=\"100\" required> ")
                    .append("Descripción <input name=\"descripcion\" maxlength=\"500\" required> ")
                    .append("Fecha límite <input type=\"datetime-local\" name=\"fechaLimite\" required> ")
                    .append("<button type=\"submit\">Crear</button></form></section>");
        }
        return pagina("Tareas", c, sb.toString());
    }

    static String examen(Ctx c) {
        return pagina("Registrar examen", c, "<section><h1>Registrar examen con asistencia</h1>"
                + "<p>Guarda la calificación del examen y la asistencia PRESENTE en una sola transacción: "
                + "si falla una de las dos, no se guarda ninguna.</p>"
                + "<form method=\"post\" action=\"/examen\">" + csrf(c)
                + "Estudiante (ID) <input type=\"number\" name=\"idEstudiante\" min=\"1\" required> "
                + "Materia (ID) <input type=\"number\" name=\"idMateria\" min=\"1\" required> "
                + "Nota <input type=\"number\" name=\"nota\" min=\"1\" max=\"12\" step=\"0.5\" required> "
                + "<button type=\"submit\">Registrar</button></form></section>");
    }

    static String error(int codigo, String mensaje) {
        return pagina("Error " + codigo, null,
                "<section><h1>Error " + codigo + "</h1><p>" + esc(mensaje) + "</p><p><a href=\"/\">Volver al inicio</a></p></section>");
    }
}
