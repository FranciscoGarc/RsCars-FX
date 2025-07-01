package com.rscars.taller.rscarsfx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
    protected void onLoginButtonClick() {
        String usuario = tfUsuario.getText();
        String contra = pfContra.getText();

        if (usuario.isEmpty() || contra.isEmpty()) {
            mostrarAlerta("Error", "Por favor, ingrese usuario y contraseña.");
            return;
        }

        if (validarCredenciales(usuario, contra)) {
            mostrarAlerta("Éxito", "¡Inicio de sesión exitoso!");
            // Aquí irá el código para abrir la ventana principal
        } else {
            mostrarAlerta("Error", "Usuario o contraseña incorrectos.");
        }
    }

    private boolean validarCredenciales(String usuario, String contra) {
        // Usamos PreparedStatement para evitar inyección SQL
        String sql = "SELECT * FROM tbUsuarios WHERE usuario = ? AND contra = ?";
        try (Connection cnx = ConexionDB.obtenerInstancia().getCnx();
             PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setString(1, usuario);
            pst.setString(2, contra);

            try (ResultSet rs = pst.executeQuery()) {
                // Si rs.next() es true, significa que encontró al menos un resultado
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Conexión", "No se pudo conectar a la base de datos.");
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