package com.rscars.taller.rscarsfx;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class MainController {

    @FXML
    private BorderPane mainPanel;

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
        cargarVista("clientes-view.fxml");
    }
    @FXML
    void onGestionVehiculosClick() {
        System.out.println("Cargando gestión de vehículos...");
        cargarVista("vehiculos-view.fxml");
    }
    @FXML
    void onGestionServiciosClick() {
        System.out.println("Cargando gestión de servicios...");
        cargarVista("servicios-view.fxml");
    }
    @FXML
    void onGestionMecanicosClick() {
        System.out.println("Cargando gestión de mecánicos...");
        cargarVista("mecanicos-view.fxml");
    }
    @FXML
    void onGestionContadoresClick() {
        System.out.println("Cargando gestión de contadores...");
        cargarVista("contadores-view.fxml");
    }
    @FXML
    void onGestionProveedoresClick() {
        System.out.println("Cargando gestión de proveedores...");
        cargarVista("proveedores-view.fxml");
    }
    @FXML
    void onGestionRepuestosClick() {
        System.out.println("Cargando gestión de repuestos...");
        cargarVista("repuestos-view.fxml");
    }

    @FXML
    void onAgendarCitaClick() {
        System.out.println("Cargando gestión de citas...");
        cargarVista("citas-view.fxml");
    }

    @FXML
    void onCerrarSesionClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Crear una nueva ventana (Stage) para la pantalla de login
            Stage loginStage = new Stage();
            loginStage.setTitle("RsCars Taller - Inicio de Sesión");
            loginStage.setScene(scene);
            loginStage.show();

            // Obtener la ventana actual (el panel principal) a través de cualquier
            // componente que esté en ella, como el 'mainPanel'
            Stage mainStage = (Stage) mainPanel.getScene().getWindow();
            mainStage.close();

        } catch (IOException e) {
            // Imprimir el error en caso de que no se pueda cargar el FXML
            e.printStackTrace();
        }
    }
    private void cargarVista(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
            Pane view = loader.load();
            mainPanel.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}