package com.rscars.taller.rscarsfx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable; // ¡Importante!
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane; // ¡Importante!
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath; // ¡Importante!
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.net.URL; // ¡Importante!
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle; // ¡Importante!

public class PasswordResetController implements Initializable{

    @FXML private VBox panelEmail, panelVerificacion, panelReset;
    @FXML private TextField tfCorreo, tfCodigo;
    @FXML private PasswordField pfNuevaContra, pfConfirmarContra;
    @FXML private TextField tfNuevaContraVisible, tfConfirmarContraVisible;
    @FXML private Button btnEnviarCodigo, btnVerificarCodigo, btnResetear;

    // --- Añade los fx:id para los botones de ojo ---
    @FXML private Button btnToggleNewPassword;
    @FXML private Button btnToggleConfirmPassword;

    private String codigoEnviado;
    private int idUsuarioParaResetear;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Asignamos el mismo ícono a ambos botones
        btnToggleNewPassword.setGraphic(createEyeIcon());
        btnToggleConfirmPassword.setGraphic(createEyeIcon());
    }

    private StackPane createEyeIcon() {
        SVGPath eyeIcon = new SVGPath();
        eyeIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5C21.27 7.61 17 4.5 12 4.5zm0 10c-2.48 0-4.5-2.02-4.5-4.5S9.52 5.5 12 5.5s4.5 2.02 4.5 4.5-2.02 4.5-4.5 4.5zm0-7C10.62 7.5 9.5 8.62 9.5 10s1.12 2.5 2.5 2.5 2.5-1.12 2.5-2.5S13.38 7.5 12 7.5z");
        eyeIcon.setStyle("-fx-fill: #36454F;");

        StackPane iconContainer = new StackPane(eyeIcon);
        iconContainer.setPrefSize(18, 18);

        return iconContainer;
    }

    @FXML
    void handleSendCode() {
        String correo = tfCorreo.getText().trim();
        if (correo.isEmpty()) {
            mostrarAlerta("Error", "Por favor, ingresa una dirección de correo.");
            return;
        }

        // 1. Verificar si el correo existe en la base de datos
        if (!verificarExistenciaCorreo(correo)) {
            mostrarAlerta("Información", "Si el correo está registrado, recibirás un código en breve.");
            return; // No le decimos al usuario si el correo existe o no por seguridad.
        }

        // 2. Enviar el código
        this.codigoEnviado = EmailService.enviarCodigoRestablecimiento(correo);
        if (this.codigoEnviado != null) {
            // 3. Cambiar al panel de verificación
            mostrarPanelVerificacion(true);
            mostrarAlerta("Código Enviado", "Hemos enviado un código a " + correo + ". Revisa tu bandeja de entrada.");
        } else {
            mostrarAlerta("Error", "No se pudo enviar el correo. Intenta de nuevo más tarde.");
        }
    }

    @FXML
    void handleVerifyCode() {
        String codigoIngresado = tfCodigo.getText().trim();

        // Validar el código ingresado
        if (codigoIngresado.isEmpty()) {
            mostrarAlerta("Error", "Por favor, ingresa el código de verificación.");
            return;
        }

        if (!codigoIngresado.equals(this.codigoEnviado)) {
            mostrarAlerta("Error", "El código de verificación es incorrecto.");
            return;
        }

        // Si el código es correcto, mostrar el panel para establecer la nueva contraseña
        mostrarPanelContraseña(true);
    }

    @FXML
    void handleResetPassword() {
        String nuevaContra = pfNuevaContra.isVisible() ? pfNuevaContra.getText() : tfNuevaContraVisible.getText();
        String confirmarContra = pfConfirmarContra.isVisible() ? pfConfirmarContra.getText() : tfConfirmarContraVisible.getText();

        // Validaciones
        if (nuevaContra.isEmpty() || !nuevaContra.equals(confirmarContra)) {
            mostrarAlerta("Error", "Las contraseñas no coinciden o están vacías.");
            return;
        }

        // Hashear la nueva contraseña
        String contraHasheada = BCrypt.hashpw(nuevaContra, BCrypt.gensalt());

        // Actualizar en la base de datos
        String sql = "UPDATE tbUsuarios SET contra = ? WHERE idUsuario = ?";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, contraHasheada);
            pst.setInt(2, this.idUsuarioParaResetear);

            int filasAfectadas = pst.executeUpdate();
            if (filasAfectadas > 0) {
                mostrarAlerta("Éxito", "Tu contraseña ha sido actualizada. Ya puedes iniciar sesión.");
                // Cerrar la ventana
                Stage stage = (Stage) btnResetear.getScene().getWindow();
                stage.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo actualizar la contraseña.");
        }
    }

    private boolean verificarExistenciaCorreo(String correo) {
        String sql = "SELECT idUsuario FROM tbUsuarios WHERE correo = ?";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, correo);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    this.idUsuarioParaResetear = rs.getInt("idUsuario");
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void mostrarPanelVerificacion(boolean mostrar) {
        panelEmail.setVisible(!mostrar);
        panelEmail.setManaged(!mostrar);
        panelVerificacion.setVisible(mostrar);
        panelVerificacion.setManaged(mostrar);
    }

    private void mostrarPanelContraseña(boolean mostrar) {
        panelVerificacion.setVisible(!mostrar);
        panelVerificacion.setManaged(!mostrar);
        panelReset.setVisible(mostrar);
        panelReset.setManaged(mostrar);
    }

    @FXML
    private void toggleNewPasswordVisibility() {
        if (pfNuevaContra.isVisible()) {
            tfNuevaContraVisible.setText(pfNuevaContra.getText());
            tfNuevaContraVisible.setVisible(true);
            tfNuevaContraVisible.setManaged(true);
            pfNuevaContra.setVisible(false);
            pfNuevaContra.setManaged(false);
        } else {
            pfNuevaContra.setText(tfNuevaContraVisible.getText());
            pfNuevaContra.setVisible(true);
            pfNuevaContra.setManaged(true);
            tfNuevaContraVisible.setVisible(false);
            tfNuevaContraVisible.setManaged(false);
        }
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        if (pfConfirmarContra.isVisible()) {
            tfConfirmarContraVisible.setText(pfConfirmarContra.getText());
            tfConfirmarContraVisible.setVisible(true);
            tfConfirmarContraVisible.setManaged(true);
            pfConfirmarContra.setVisible(false);
            pfConfirmarContra.setManaged(false);
        } else {
            pfConfirmarContra.setText(tfConfirmarContraVisible.getText());
            pfConfirmarContra.setVisible(true);
            pfConfirmarContra.setManaged(true);
            tfConfirmarContraVisible.setVisible(false);
            tfConfirmarContraVisible.setManaged(false);
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