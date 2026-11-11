package uy.edu.utu.crea3.model;

public class Estudiante extends Usuario {

    public Estudiante() {
    }

    public Estudiante(int idUsuario, String nombre, String apellido, String email) {
        super(idUsuario, nombre, apellido, email);
    }

    @Override
    public String getRol() {
        return "ESTUDIANTE";
    }

    @Override
    public boolean puedeGestionar() {
        return false;
    }

    @Override
    public String getTituloPanel() {
        return "CREA 3 - Panel Estudiante (" + getNombreCompleto() + ")";
    }
}
