package com.rscars.taller.rscarsfx;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PrimerUsoController {

    // --- CAMPOS ORIGINALES ---
    @FXML private TextField tfNombre, tfApellido, tfTelefono, tfDireccion, tfDui, tfCorreo, tfUsuario;
    @FXML private PasswordField pfContra;
    @FXML private TextField tfContraVisible; // Re-añadido
    @FXML private Button btnGuardar;

    // --- ELEMENTOS DEL PANEL DE VERIFICACIÓN ---
    @FXML private VBox panelVerificacion;
    @FXML private TextField tfCodigo;
    @FXML private Button btnVerificar;

    // --- VARIABLES PARA GUARDAR DATOS TEMPORALMENTE ---
    private String codigoEnviado;
    private String nombre, apellido, telefono, direccion, dui, correo, usuario, contra;

    /**
     * Se activa con el primer botón. Valida los campos, envía el correo y muestra el panel de verificación.
     */
    @FXML
    void iniciarVerificacion() {
        correo = tfCorreo.getText().trim();
        // Validaciones iniciales
        if (tfNombre.getText().isEmpty() || tfUsuario.getText().isEmpty() || obtenerPassword().isEmpty() || correo.isEmpty()) {
            mostrarAlerta("Error de Validación", "Nombre, Usuario, Contraseña y Correo son obligatorios.");
            return;
        }

        System.out.println("Iniciando proceso de verificación para: " + correo);
        guardarDatosTemporalmente();

        this.codigoEnviado = EmailService.enviarCodigoVerificacion(correo);

        if (codigoEnviado != null) {
            mostrarPanelVerificacion(true);
            mostrarAlerta("Verificación Requerida", "Hemos enviado un código a " + correo + ". Por favor, ingrésalo para continuar.");
        } else {
            mostrarAlerta("Error de Envío", "No pudimos enviar el código de verificación. Revisa la configuración en config.properties o la dirección de correo ingresada.");
        }
    }

    /**
     * Se activa con el segundo botón. Compara códigos y guarda en la BD si son correctos.
     */
    @FXML
    void verificarYGuardar() {
        String codigoIngresado = tfCodigo.getText().trim();
        if (codigoIngresado.equals(this.codigoEnviado)) {
            System.out.println("Código correcto. Guardando usuario en la base de datos.");
            guardarUsuarioEnBD();
        } else {
            mostrarAlerta("Error", "El código de verificación es incorrecto.");
        }
    }

    private void guardarDatosTemporalmente() {
        this.nombre = tfNombre.getText().trim();
        this.apellido = tfApellido.getText().trim();
        this.telefono = tfTelefono.getText().trim();
        this.direccion = tfDireccion.getText().trim();
        this.dui = tfDui.getText().trim();
        this.usuario = tfUsuario.getText().trim();
        // Corregido para usar el método que obtiene la contraseña del campo correcto
        this.contra = obtenerPassword();
    }

    // --- MÉTODO RE-AÑADIDO ---
    /**
     * Alterna la visibilidad entre el campo de contraseña y el campo de texto visible.
     */
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

    // --- MÉTODO RE-AÑADIDO ---
    /**
     * Obtiene la contraseña del campo que esté visible actualmente.
     * @return La contraseña como una cadena de texto.
     */
    private String obtenerPassword() {
        return pfContra.isVisible() ? pfContra.getText() : tfContraVisible.getText();
    }


    private void mostrarPanelVerificacion(boolean mostrar) {
        // Ocultamos los campos originales y el primer botón
        tfNombre.setManaged(!mostrar); tfNombre.setVisible(!mostrar);
        tfApellido.setManaged(!mostrar); tfApellido.setVisible(!mostrar);
        tfTelefono.setManaged(!mostrar); tfTelefono.setVisible(!mostrar);
        tfDireccion.setManaged(!mostrar); tfDireccion.setVisible(!mostrar);
        tfDui.setManaged(!mostrar); tfDui.setVisible(!mostrar);
        tfCorreo.setManaged(!mostrar); tfCorreo.setVisible(!mostrar);
        tfUsuario.setManaged(!mostrar); tfUsuario.setVisible(!mostrar);
        pfContra.setManaged(!mostrar); pfContra.setVisible(!mostrar);
        tfContraVisible.setManaged(!mostrar); tfContraVisible.setVisible(!mostrar); // Corregido: también se oculta
        btnGuardar.setManaged(!mostrar); btnGuardar.setVisible(!mostrar);

        // Mostramos el panel de verificación
        panelVerificacion.setVisible(mostrar);
        panelVerificacion.setManaged(mostrar);
    }

    private void guardarUsuarioEnBD() {
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try {
            cnx.setAutoCommit(false);
            String contraHasheada = BCrypt.hashpw(this.contra, BCrypt.gensalt());

            // 1. Crear el usuario
            String sqlUsuario = "INSERT INTO tbUsuarios (idTipo, usuario, contra, correo) VALUES (?, ?, ?, ?)";
            int nuevoUsuarioId = 0;
            try (PreparedStatement pstUsuario = cnx.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                pstUsuario.setInt(1, 1); // idTipo 1 para Mecánico
                pstUsuario.setString(2, this.usuario);
                pstUsuario.setString(3, contraHasheada);
                pstUsuario.setString(4, this.correo);
                pstUsuario.executeUpdate();

                try (ResultSet generatedKeys = pstUsuario.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        nuevoUsuarioId = generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Fallo al crear el usuario, no se obtuvo el ID.");
                    }
                }
            }

            // 2. Crear el registro del mecánico
            String sqlMecanico = "INSERT INTO tbMecanicos (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstMecanico = cnx.prepareStatement(sqlMecanico)) {
                pstMecanico.setInt(1, nuevoUsuarioId);
                pstMecanico.setString(2, this.nombre);
                pstMecanico.setString(3, this.apellido);
                pstMecanico.setString(4, this.telefono);
                pstMecanico.setString(5, this.direccion);
                pstMecanico.setString(6, this.dui);
                pstMecanico.executeUpdate();
            }

            cnx.commit();
            mostrarAlerta("¡Éxito!", "Primer usuario administrador creado correctamente.");
            abrirVentanaPrincipal();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Base de Datos", "No se pudo registrar el primer usuario.");
            try { if (cnx != null) cnx.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        } finally {
            try { if (cnx != null) cnx.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void abrirVentanaPrincipal() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("main-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            MainController mainController = fxmlLoader.getController();
            mainController.inicializarConUsuario(1);

            Stage stage = new Stage();
            stage.setTitle("RsCars Taller - Panel Principal");
            stage.setScene(scene);
            stage.show();

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