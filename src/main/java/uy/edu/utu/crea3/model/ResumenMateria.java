package uy.edu.utu.crea3.model;

/** Fila del reporte de promedios: cantidad de notas y promedio por materia. */
public class ResumenMateria {
    private final String materia;
    private final int cantidad;
    private final double promedio;

    public ResumenMateria(String materia, int cantidad, double promedio) {
        this.materia = materia;
        this.cantidad = cantidad;
        this.promedio = promedio;
    }

    public String getMateria() { return materia; }
    public int getCantidad() { return cantidad; }
    public double getPromedio() { return promedio; }
}
