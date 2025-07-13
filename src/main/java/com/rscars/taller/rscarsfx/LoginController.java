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
import javafx.scene.layout.VBox;
import javafx.stage.Modality;


public class LoginController {

    @FXML
    private TextField tfUsuario;

    @FXML
    private PasswordField pfContra;

    @FXML
    private Button btnIngresar;

    @FXML
    private TextField tfContraVisible;

    /**
     * Una clase simple para contener los datos del usuario después del login.
     */
    public static class UserSession {
        public final int idTipo;
        public final String nombre;

        public UserSession(int idTipo, String nombre) {
            this.idTipo = idTipo;
            this.nombre = nombre;
        }
    }

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

        // --- CAMBIO: Ahora obtenemos un objeto UserSession ---
        UserSession session = validarCredenciales(usuario, contra);
        if (session != null) { // Si no es nulo, el login es correcto
            if (session.idTipo == 3) { // idTipo 3 es Cliente
                mostrarAlerta("Acceso Denegado", "Usted no tiene acceso a la aplicación administrativa.");
            } else {
                abrirVentanaPrincipal(session); // Pasamos el objeto de sesión completo
            }
        } else {
            mostrarAlerta("Error de Autenticación", "Usuario o contraseña incorrectos.");
        }
    }

    @FXML
    private void handleForgotPassword() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("password-reset-view.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Restablecer Contraseña");
            // Modality.APPLICATION_MODAL bloquea la interacción con la ventana de login
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.showAndWait(); // Muestra la ventana y espera a que se cierre
        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la ventana de restablecimiento de contraseña.");
        }
    }

    // --- CAMBIO: El método ahora recibe el objeto UserSession ---
    private void abrirVentanaPrincipal(UserSession session) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            MainController mainController = fxmlLoader.getController();
            // --- CAMBIO: Pasamos el rol y el nombre ---
            mainController.inicializarConUsuario(session.idTipo, session.nombre);

            Stage stage = new Stage();
            stage.setTitle("RsCars Taller - Panel Principal");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();

            Stage loginStage = (Stage) btnIngresar.getScene().getWindow();
            loginStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- CAMBIO: El método ahora devuelve un objeto UserSession ---
    private UserSession validarCredenciales(String usuario, String contraPlana) {
        // Esta consulta une las tres tablas para obtener todos los datos de una vez
        String sql = "SELECT U.idTipo, U.contra, COALESCE(M.nombre, C.nombre) as nombreUsuario " +
                "FROM tbUsuarios U " +
                "LEFT JOIN tbMecanicos M ON U.idUsuario = M.idUsuario " +
                "LEFT JOIN tbContadores C ON U.idUsuario = C.idUsuario " +
                "WHERE U.usuario = ?";

        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        if (cnx == null) return null;

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, usuario);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    String contraHasheada = rs.getString("contra");
                    if (BCrypt.checkpw(contraPlana, contraHasheada)) {
                        int idTipo = rs.getInt("idTipo");
                        String nombre = rs.getString("nombreUsuario");
                        return new UserSession(idTipo, nombre); // Devolvemos el objeto con los datos
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Si algo falla, devuelve nulo
    }
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}