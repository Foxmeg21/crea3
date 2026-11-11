package uy.edu.utu.crea3.model;

public class Docente extends Usuario {

    public Docente() {
    }

    public Docente(int idUsuario, String nombre, String apellido, String email) {
        super(idUsuario, nombre, apellido, email);
    }

    @Override
    public String getRol() {
        return "DOCENTE";
    }

    @Override
    public boolean puedeGestionar() {
        return true;
    }

    @Override
    public String getTituloPanel() {
        return "CREA 3 - Panel Docente (" + getNombreCompleto() + ")";
    }
}
