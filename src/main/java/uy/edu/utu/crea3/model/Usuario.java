package uy.edu.utu.crea3.model;

/**
 * Raíz de la jerarquía de usuarios (R5). Las subclases Docente y Estudiante
 * redefinen el comportamiento según el rol (polimorfismo).
 * La contraseña (hash) nunca se guarda en el objeto.
 */
public abstract class Usuario {
    protected int idUsuario;
    protected String nombre;
    protected String apellido;
    protected String email;

    protected Usuario() {
    }

    protected Usuario(int idUsuario, String nombre, String apellido, String email) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
    }

    /** Rol tal como se guarda en la base: DOCENTE o ESTUDIANTE. */
    public abstract String getRol();

    /** Si el usuario puede crear, modificar y eliminar registros académicos. */
    public abstract boolean puedeGestionar();

    /** Texto para el título de la ventana principal. */
    public abstract String getTituloPanel();

    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
