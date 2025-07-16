package com.rscars.taller.rscarsfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        // Verificar si existen usuarios
        boolean existenUsuarios = ConexionDB.obtenerInstancia().hayUsuarios();

        FXMLLoader fxmlLoader;
        if (!existenUsuarios) {
            // No hay usuarios, cargar la pantalla de primer uso
            System.out.println("No se encontraron usuarios. Iniciando pantalla de primer uso.");
            fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("primer-uso-view.fxml"));
            stage.setTitle("RsCars Taller - Configuración Inicial");
        } else {
            // Ya hay usuarios, cargar el login normal
            fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            stage.setTitle("RsCars Taller - Inicio de Sesión");
        }

        Scene scene = new Scene(fxmlLoader.load(), 450, 500); // Ajusta el tamaño si es necesario
        scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    @Override
    public void stop() {
        System.out.println("Cerrando la aplicación...");
        ConexionDB.obtenerInstancia().cerrarConexion();
    }

    public static void main(String[] args) {
        launch();
    }
}