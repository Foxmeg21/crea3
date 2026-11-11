package uy.edu.utu.crea3.service;

import uy.edu.utu.crea3.config.DatabaseConnection;

public class SistemaServiceImpl implements SistemaService {

    @Override
    public boolean baseDeDatosDisponible() {
        return DatabaseConnection.verificar();
    }
}
