package com.rscars.taller.rscarsfx;

import org.mindrot.jbcrypt.BCrypt;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginController {

    @FXML
    private TextField tfUsuario;

    @FXML
    private PasswordField pfContra;

    @FXML
    private Button btnIngresar;

    @FXML
    private TextField tfContraVisible;

    @FXML
    private void togglePasswordVisibility() {
        if (pfContra.isVisible()) {
            tfContraVisible.setText(pfContra.getText());
            tfContraVisible.setVisible(true);
            tfContraVisible.setManaged(true);
            pfContra.setVisible(false);
            pfContra.setManaged(false);
        } else {
            pfContra.setText(tfContraVisible.getText());
            pfContra.setVisible(true);
            pfContra.setManaged(true);
            tfContraVisible.setVisible(false);
            tfContraVisible.setManaged(false);
        }
    }

    @FXML
    protected void onLoginButtonClick() {
        String usuario = tfUsuario.getText();
        String contra = pfContra.isVisible() ? pfContra.getText() : tfContraVisible.getText();

        if (usuario.isEmpty() || contra.isEmpty()) {
            mostrarAlerta("Error de Validación", "Por favor, ingrese usuario y contraseña.");
            return;
        }

        if (validarCredenciales(usuario, contra)) {
            abrirVentanaPrincipal();
        } else {
            mostrarAlerta("Error de Autenticación", "Usuario o contraseña incorrectos.");
        }
    }

    private void abrirVentanaPrincipal() {
        try {
            // Cargar la nueva vista
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Crear un nuevo Stage (ventana)
            Stage stage = new Stage();
            stage.setTitle("RsCars Taller - Panel Principal");
            stage.setScene(scene);
            stage.show();

            // Cerrar la ventana de login actual
            Stage loginStage = (Stage) btnIngresar.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana principal.");
        }
    }

    private boolean validarCredenciales(String usuario, String contraPlana) {
        String sql = "SELECT contra FROM tbUsuarios WHERE usuario = ?";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();

        if (cnx == null) {
            mostrarAlerta("Error de Conexión", "No se pudo conectar a la base de datos.");
            return false;
        }

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, usuario);

            try (ResultSet rs = pst.executeQuery()) {
                // Si el usuario existe, rs.next() será verdadero
                if (rs.next()) {
                    String contraHasheada = rs.getString("contra");
                    // Comparamos la contraseña plana con el hash de la BD
                    return BCrypt.checkpw(contraPlana, contraHasheada);
                } else {
                    // El usuario no fue encontrado
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Consulta", "Error al verificar las credenciales.");
            return false;
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