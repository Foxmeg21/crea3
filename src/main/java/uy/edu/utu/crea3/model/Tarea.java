package uy.edu.utu.crea3.model;

public class Tarea {
    private int idTarea;
    private int idMateria;
    private String titulo;
    private String descripcion;
    private String fecha;

    public Tarea() {}

    public Tarea(int idTarea, int idMateria, String titulo, String descripcion, String fecha) {
        this.idTarea = idTarea;
        this.idMateria = idMateria;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public int getIdTarea() { return idTarea; }
    public void setIdTarea(int idTarea) { this.idTarea = idTarea; }

    public int getIdMateria() { return idMateria; }
    public void setIdMateria(int idMateria) { this.idMateria = idMateria; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}
