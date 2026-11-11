package uy.edu.utu.crea3.util;

import uy.edu.utu.crea3.exception.CreaException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/** Validaciones de entrada (R8). Todas lanzan CreaException con un mensaje claro. */
public final class Validador {

    public static final List<String> TIPOS = List.of("Parcial", "Examen", "Tarea");
    public static final List<String> ESTADOS = List.of("PRESENTE", "AUSENTE", "JUSTIFICADO");
    public static final float NOTA_MIN = 1f;
    public static final float NOTA_MAX = 12f;
    private static final DateTimeFormatter FECHA_HORA = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private Validador() {
    }

    /** Entero positivo obligatorio (IDs). */
    public static int id(String texto, String campo) throws CreaException {
        if (texto == null || texto.isBlank()) {
            throw new CreaException("El campo \"" + campo + "\" es obligatorio.");
        }
        try {
            return id(Integer.parseInt(texto.trim()), campo);
        } catch (NumberFormatException e) {
            throw new CreaException("El campo \"" + campo + "\" debe ser un número entero.");
        }
    }

    public static int id(int valor, String campo) throws CreaException {
        if (valor <= 0) {
            throw new CreaException("El campo \"" + campo + "\" debe ser un número positivo.");
        }
        return valor;
    }

    /** Entero positivo opcional: vacío significa "sin filtro" (null). */
    public static Integer idOpcional(String texto, String campo) throws CreaException {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return id(texto, campo);
    }

    public static float nota(String texto) throws CreaException {
        if (texto == null || texto.isBlank()) {
            throw new CreaException("La nota es obligatoria.");
        }
        try {
            return nota(Float.parseFloat(texto.trim().replace(',', '.')));
        } catch (NumberFormatException e) {
            throw new CreaException("La nota debe ser un número.");
        }
    }

    public static float nota(float valor) throws CreaException {
        if (Float.isNaN(valor) || valor < NOTA_MIN || valor > NOTA_MAX) {
            throw new CreaException("La nota debe estar entre " + (int) NOTA_MIN + " y " + (int) NOTA_MAX + ".");
        }
        return valor;
    }

    public static Float notaOpcional(String texto) throws CreaException {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        return nota(texto);
    }

    public static String texto(String valor, String campo, int maximo) throws CreaException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new CreaException("El campo \"" + campo + "\" es obligatorio.");
        }
        String limpio = valor.trim();
        if (limpio.length() > maximo) {
            throw new CreaException("El campo \"" + campo + "\" admite hasta " + maximo + " caracteres.");
        }
        return limpio;
    }

    public static String tipo(String valor) throws CreaException {
        if (valor == null || !TIPOS.contains(valor)) {
            throw new CreaException("Tipo de calificación inválido.");
        }
        return valor;
    }

    public static String estado(String valor) throws CreaException {
        if (valor == null || !ESTADOS.contains(valor)) {
            throw new CreaException("Estado de asistencia inválido.");
        }
        return valor;
    }

    /** Fecha y hora con formato yyyy-MM-dd HH:mm:ss. */
    public static String fechaHora(String valor) throws CreaException {
        if (valor == null || valor.isBlank()) {
            throw new CreaException("La fecha límite es obligatoria.");
        }
        try {
            return LocalDateTime.parse(valor.trim(), FECHA_HORA).format(FECHA_HORA);
        } catch (DateTimeParseException e) {
            throw new CreaException("La fecha debe tener el formato AAAA-MM-DD HH:MM:SS (ej.: 2026-12-01 23:59:00).");
        }
    }
}
