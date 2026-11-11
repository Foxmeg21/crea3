package uy.edu.utu.crea3.web;

import io.javalin.http.Context;

import java.util.HashMap;
import java.util.Map;

/** Envuelve el Context de Javalin: método, ruta, parámetros (query + formulario) y cookies. */
final class Peticion {

    final Context ctx;
    final String metodo;
    final String ruta;
    private final Map<String, String> params = new HashMap<>();

    Peticion(Context ctx) {
        this.ctx = ctx;
        this.metodo = ctx.method().name();
        this.ruta = ctx.path();
        ctx.queryParamMap().forEach((k, v) -> {
            if (!v.isEmpty()) {
                params.put(k, v.get(0));
            }
        });
        if ("POST".equals(metodo)) {
            ctx.formParamMap().forEach((k, v) -> {
                if (!v.isEmpty()) {
                    params.put(k, v.get(0));
                }
            });
        }
    }

    /** Valor del parámetro o "" si no vino. */
    String p(String nombre) {
        return params.getOrDefault(nombre, "");
    }

    Map<String, String> todos() {
        return params;
    }

    String cookie(String nombre) {
        return ctx.cookie(nombre);
    }

    /** True si la petición llegó por HTTPS (Render y Railway terminan el TLS y avisan con X-Forwarded-Proto). */
    boolean esHttps() {
        return "https".equalsIgnoreCase(ctx.header("X-Forwarded-Proto"));
    }
}
