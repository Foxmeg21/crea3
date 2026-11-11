package uy.edu.utu.crea3.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/** Envuelve la petición HTTP: método, ruta, parámetros (query + formulario) y cookies. */
final class Peticion {

    private static final int MAX_CUERPO = 64 * 1024;

    final HttpExchange ex;
    final String metodo;
    final String ruta;
    private final Map<String, String> params = new HashMap<>();

    Peticion(HttpExchange ex) throws IOException {
        this.ex = ex;
        this.metodo = ex.getRequestMethod();
        this.ruta = ex.getRequestURI().getPath();
        leer(ex.getRequestURI().getRawQuery());
        if ("POST".equals(metodo)) {
            byte[] cuerpo = ex.getRequestBody().readNBytes(MAX_CUERPO);
            leer(new String(cuerpo, StandardCharsets.UTF_8));
        }
    }

    private void leer(String texto) {
        if (texto == null || texto.isEmpty()) {
            return;
        }
        for (String par : texto.split("&")) {
            int i = par.indexOf('=');
            String clave = i < 0 ? par : par.substring(0, i);
            String valor = i < 0 ? "" : par.substring(i + 1);
            params.put(URLDecoder.decode(clave, StandardCharsets.UTF_8),
                    URLDecoder.decode(valor, StandardCharsets.UTF_8));
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
        var cabeceras = ex.getRequestHeaders().get("Cookie");
        if (cabeceras == null) {
            return null;
        }
        for (String linea : cabeceras) {
            for (String c : linea.split(";")) {
                String t = c.trim();
                if (t.startsWith(nombre + "=")) {
                    return t.substring(nombre.length() + 1);
                }
            }
        }
        return null;
    }

    /** True si la petición llegó por HTTPS (Render termina el TLS y avisa con X-Forwarded-Proto). */
    boolean esHttps() {
        return "https".equalsIgnoreCase(ex.getRequestHeaders().getFirst("X-Forwarded-Proto"));
    }
}
