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

        int idTipoUsuario = validarCredenciales(usuario, contra);

        if (idTipoUsuario > 0) { // Si es mayor a 0, el login es correcto
            if (idTipoUsuario == 3) { // idTipo 3 es Cliente
                mostrarAlerta("Acceso Denegado", "Usted no tiene acceso a la aplicación administrativa.");
                // No se hace nada más, el usuario se queda en el login.
            } else {
                // Si es Mecánico (1) o Contador (2), abrimos la ventana principal
                abrirVentanaPrincipal(idTipoUsuario);
            }
        } else {
            mostrarAlerta("Error de Autenticación", "Usuario o contraseña incorrectos.");
        }
    }


    private void abrirVentanaPrincipal(int idTipoUsuario) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // --- OBTENEMOS EL CONTROLADOR Y PASAMOS EL ROL ---
            MainController mainController = fxmlLoader.getController();
            mainController.inicializarConUsuario(idTipoUsuario);

            Stage stage = new Stage();
            stage.setTitle("RsCars Taller - Panel Principal");
            stage.setScene(scene);
            stage.show();

            Stage loginStage = (Stage) btnIngresar.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana principal.");
        }
    }

    private int validarCredenciales(String usuario, String contraPlana) {
        String sql = "SELECT idTipo, contra FROM tbUsuarios WHERE usuario = ?";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();

        if (cnx == null) {
            mostrarAlerta("Error de Conexión", "No se pudo conectar a la base de datos.");
            return 0; // 0 indica error
        }

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, usuario);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String contraHasheada = rs.getString("contra");
                    if (BCrypt.checkpw(contraPlana, contraHasheada)) {
                        // Si la contraseña es correcta, devolvemos el tipo de usuario
                        return rs.getInt("idTipo");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Consulta", "Error al verificar las credenciales.");
        }
        return 0; // Si algo falla, devuelve 0
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}