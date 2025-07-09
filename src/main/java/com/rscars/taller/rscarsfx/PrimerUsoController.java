package com.rscars.taller.rscarsfx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PrimerUsoController {

    @FXML private TextField tfNombre, tfApellido, tfTelefono, tfDireccion, tfDui, tfCorreo, tfUsuario, tfContraVisible;
    @FXML private PasswordField pfContra;
    @FXML private Button btnGuardar;

    @FXML
    private void togglePasswordVisibility() {
        if (pfContra.isVisible()) {
            tfContraVisible.setText(pfContra.getText());
            tfContraVisible.setVisible(true);
            pfContra.setVisible(false);
        } else {
            pfContra.setText(tfContraVisible.getText());
            pfContra.setVisible(true);
            tfContraVisible.setVisible(false);
        }
    }

    private String obtenerPassword() {
        return pfContra.isVisible() ? pfContra.getText() : tfContraVisible.getText();
    }

    @FXML
    void guardarPrimerUsuario() {
        if (tfNombre.getText().isEmpty() || tfUsuario.getText().isEmpty() || obtenerPassword().isEmpty() || tfCorreo.getText().isEmpty()) {
            mostrarAlerta("Error de Validación", "Nombre, Usuario, Contraseña y Correo son obligatorios.");
            return;
        }

        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try {
            cnx.setAutoCommit(false); // Iniciar transacción

            // 1. Crear el usuario de tipo "Mecanico" (idTipo = 1)
            String sqlUsuario = "INSERT INTO tbUsuarios (idTipo, usuario, contra, correo) VALUES (?, ?, ?, ?)";
            int nuevoUsuarioId = 0;
            try (PreparedStatement pstUsuario = cnx.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                pstUsuario.setInt(1, 1); // idTipo 1 para Mecánico
                pstUsuario.setString(2, tfUsuario.getText().trim());
                pstUsuario.setString(3, obtenerPassword().trim());
                pstUsuario.setString(4, tfCorreo.getText().trim());
                pstUsuario.executeUpdate();

                try (ResultSet generatedKeys = pstUsuario.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        nuevoUsuarioId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Fallo al crear el usuario, no se obtuvo el ID.");
                    }
                }
            }

            // 2. Crear el registro del contador asociado
            String sqlContador = "INSERT INTO tbMecanicos (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstContador = cnx.prepareStatement(sqlContador)) {
                pstContador.setInt(1, nuevoUsuarioId);
                pstContador.setString(2, tfNombre.getText().trim());
                pstContador.setString(3, tfApellido.getText().trim());
                pstContador.setString(4, tfTelefono.getText().trim());
                pstContador.setString(5, tfDireccion.getText().trim());
                pstContador.setString(6, tfDui.getText().trim());
                pstContador.executeUpdate();
            }

            cnx.commit(); // Confirmar transacción
            mostrarAlerta("¡Éxito!", "Primer usuario administrador creado correctamente.");

            // Abrir la ventana principal de la aplicación
            abrirVentanaPrincipal();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Base de Datos", "No se pudo registrar el primer usuario.");
            try {
                if (cnx != null) cnx.rollback(); // Revertir en caso de error
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (cnx != null) cnx.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void abrirVentanaPrincipal() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle("RsCars Taller - Panel Principal");
            stage.setScene(scene);
            stage.show();

            // Cerrar la ventana actual
            Stage currentStage = (Stage) btnGuardar.getScene().getWindow();
            currentStage.close();
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana principal.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}