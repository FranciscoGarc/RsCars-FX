package com.rscars.taller.rscarsfx;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

public class MainController {

    @FXML
    private MenuItem menuCerrarSesion;

    @FXML
    private MenuItem menuGestionClientes;

    @FXML
    private MenuItem menuGestionVehiculos;

    @FXML
    private MenuItem menuGestionServicios;

    @FXML
    private MenuItem menuAgendarCita;

    // Aquí agregaremos los métodos para manejar los clics del menú

    @FXML
    void onGestionClientesClick() {
        System.out.println("Cargando gestión de clientes...");
        // Futuro: Cargar clientes-view.fxml en el centro del BorderPane
    }

    @FXML
    void onCerrarSesionClick() {
        System.out.println("Cerrando sesión...");
        // Futuro: Lógica para cerrar esta ventana y mostrar el login
    }
}