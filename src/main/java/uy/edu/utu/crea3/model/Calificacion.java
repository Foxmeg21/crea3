package uy.edu.utu.crea3.model;

import java.sql.Date;

public class Calificacion {
    private int idCalificacion;
    private int idEstudiante;
    private int idMateria;
    private String nombreMateria;
    private float nota;
    private Date fecha;
    private String tipo;

    public Calificacion() {}

    public int getIdCalificacion() { return idCalificacion; }
    public void setIdCalificacion(int idCalificacion) { this.idCalificacion = idCalificacion; }

    public int getIdEstudiante() { return idEstudiante; }
    public void setIdEstudiante(int idEstudiante) { this.idEstudiante = idEstudiante; }

    public int getIdMateria() { return idMateria; }
    public void setIdMateria(int idMateria) { this.idMateria = idMateria; }

    public String getNombreMateria() { return nombreMateria; }
    public void setNombreMateria(String nombreMateria) { this.nombreMateria = nombreMateria; }

    public float getNota() { return nota; }
    public void setNota(float nota) { this.nota = nota; }

    public Date getFecha() { return fecha; }
    public void setFecha(Date fecha) { this.fecha = fecha; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
}
