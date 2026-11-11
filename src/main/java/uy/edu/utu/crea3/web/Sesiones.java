package uy.edu.utu.crea3.web;

import uy.edu.utu.crea3.model.Usuario;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Sesiones en memoria (una sola réplica). El identificador es aleatorio de 256 bits. */
final class Sesiones {

    private static final long INACTIVIDAD_MS = 30 * 60 * 1000L;
    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final Map<String, Sesion> MAPA = new ConcurrentHashMap<>();

    static final class Sesion {
        final String id;
        final Usuario usuario;
        final String csrf;
        String mensajeOk;
        String mensajeError;
        volatile long ultimoUso = System.currentTimeMillis();

        Sesion(String id, Usuario usuario, String csrf) {
            this.id = id;
            this.usuario = usuario;
            this.csrf = csrf;
        }
    }

    private Sesiones() {
    }

    /** Crea una sesión nueva (siempre con identificador nuevo al iniciar sesión). */
    static Sesion crear(Usuario usuario) {
        Sesion s = new Sesion(token(), usuario, token());
        MAPA.put(s.id, s);
        return s;
    }

    static Sesion obtener(String id) {
        if (id == null) {
            return null;
        }
        Sesion s = MAPA.get(id);
        if (s == null) {
            return null;
        }
        long ahora = System.currentTimeMillis();
        if (ahora - s.ultimoUso > INACTIVIDAD_MS) {
            MAPA.remove(id);
            return null;
        }
        s.ultimoUso = ahora;
        return s;
    }

    static void cerrar(String id) {
        if (id != null) {
            MAPA.remove(id);
        }
    }

    private static String token() {
        byte[] b = new byte[32];
        ALEATORIO.nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }
}
