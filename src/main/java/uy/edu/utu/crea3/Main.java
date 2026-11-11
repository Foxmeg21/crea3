package uy.edu.utu.crea3;

import uy.edu.utu.crea3.web.WebServer;

/** Punto de entrada: levanta el servidor web Javalin (puerto 8080 o la variable PORT). */
public class Main {
    public static void main(String[] args) {
        WebServer.iniciar();
    }
}
