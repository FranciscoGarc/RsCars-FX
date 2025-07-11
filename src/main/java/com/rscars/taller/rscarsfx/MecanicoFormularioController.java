package com.rscars.taller.rscarsfx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import org.mindrot.jbcrypt.BCrypt;


public class MecanicoFormularioController implements Initializable {
    @FXML private Label lblTitulo;
    @FXML private TextField tfNombre, tfApellido, tfTelefono, tfDireccion, tfDui, tfUsuario, tfCorreo, tfContraVisible;
    @FXML private PasswordField pfContra;
    @FXML private Button btnGuardar, btnCancelar;

    private Mecanico mecanicoParaEditar;
    private MecanicosController mecanicosController;
    private boolean esNuevo = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ValidationUtil.autoFormatPhone(tfTelefono);
        ValidationUtil.autoFormatDui(tfDui);
    }

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

    public void setMecanicosController(MecanicosController controller) {
        this.mecanicosController = controller;
    }

    private int obtenerIdUsuarioPorMecanico(int idMecanico) {
        String sql = "SELECT idUsuario FROM tbMecanicos WHERE idMecanico = ?";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, idMecanico);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("idUsuario");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void setMecanicoParaEditar(Mecanico mecanico) {
        this.mecanicoParaEditar = mecanico;
        this.esNuevo = false;
        lblTitulo.setText("Editar Mecánico");
        tfNombre.setText(mecanico.getNombre());
        tfApellido.setText(mecanico.getApellido());
        tfTelefono.setText(mecanico.getTelefono());
        tfDireccion.setText(mecanico.getDireccion());
        tfDui.setText(mecanico.getDui());
        // NO cargamos la contraseña. Los campos se dejan en blanco.
        pfContra.setPromptText("Dejar en blanco para no cambiar");
        tfContraVisible.setPromptText("Dejar en blanco para no cambiar");

        // Obtener idUsuario desde la BD usando idMecanico
        int idUsuario = obtenerIdUsuarioPorMecanico(mecanico.getIdMecanico());
        if (idUsuario != 0) {
            // Solo cargamos los datos que no son sensibles
            String sql = "SELECT usuario, correo FROM tbUsuarios WHERE idUsuario = ?";
            Connection cnx = ConexionDB.obtenerInstancia().getCnx();

            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setInt(1, idUsuario);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        tfUsuario.setText(rs.getString("usuario"));
                        tfCorreo.setText(rs.getString("correo"));
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error", "No se pudieron cargar los datos del usuario.");
            }
        }
    }

    @FXML
    void guardarMecanico() {
        String nombre = tfNombre.getText().trim();
        String apellido = tfApellido.getText().trim();
        String telefono = tfTelefono.getText().trim();
        String direccion = tfDireccion.getText().trim();
        String dui = tfDui.getText().trim();
        String correo = tfCorreo.getText().trim();
        String usuario = tfUsuario.getText().trim();
        String contra = obtenerPassword();

        // --- VALIDACIONES ---
        if (!ValidationUtil.isNotEmpty(nombre) || !ValidationUtil.isNotEmpty(apellido) ||
            !ValidationUtil.isNotEmpty(telefono) || !ValidationUtil.isNotEmpty(direccion) ||
            !ValidationUtil.isNotEmpty(dui) || !ValidationUtil.isNotEmpty(correo) ||
            !ValidationUtil.isNotEmpty(usuario)) {
            mostrarAlerta("Error de Validación", "Todos los campos son obligatorios.");
            return;
        }

        if (esNuevo && !ValidationUtil.isNotEmpty(contra)) {
            mostrarAlerta("Error de Validación", "La contraseña es obligatoria para nuevos usuarios.");
            return;
        }

        if (!ValidationUtil.isTextOnly(nombre)) {
            mostrarAlerta("Error de Validación", "El nombre solo debe contener letras y espacios.");
            return;
        }
        if (!ValidationUtil.isTextOnly(apellido)) {
            mostrarAlerta("Error de Validación", "El apellido solo debe contener letras y espacios.");
            return;
        }
        if (!ValidationUtil.isPhoneValid(telefono)) {
            mostrarAlerta("Error de Validación", "El teléfono debe tener 8 dígitos.");
            return;
        }
        if (!ValidationUtil.isDuiValid(dui)) {
            mostrarAlerta("Error de Validación", "El DUI debe tener 9 dígitos.");
            return;
        }
        if (!ValidationUtil.isValidEmail(correo)) {
            mostrarAlerta("Error de Validación", "El formato del correo electrónico no es válido.");
            return;
        }
        if (esNuevo && ValidationUtil.usuarioYaExiste(usuario)) {
            mostrarAlerta("Error de Validación", "El nombre de usuario ya está en uso. Por favor, elija otro.");
            return;
        }
        if (esNuevo && !ValidationUtil.isValidPassword(contra)) {
            mostrarAlerta("Error de Validación", "La contraseña debe tener al menos 8 caracteres.");
            return;
        }
        if (!esNuevo && !contra.isEmpty() && !ValidationUtil.isValidPassword(contra)) {
            mostrarAlerta("Error de Validación", "La nueva contraseña debe tener al menos 8 caracteres.");
            return;
        }


        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try {
            cnx.setAutoCommit(false);

            String contraHasheada = BCrypt.hashpw(contra, BCrypt.gensalt());

            if (esNuevo) {
                String sqlUsuario = "INSERT INTO tbUsuarios (idTipo, usuario, contra, correo) VALUES (?, ?, ?, ?)";
                int nuevoUsuarioId = 0;

                try (PreparedStatement pstUsuario = cnx.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                    pstUsuario.setInt(1, 1); // idTipo 1 = Mecánico
                    pstUsuario.setString(2, usuario);
                    pstUsuario.setString(3, contraHasheada);
                    pstUsuario.setString(4, correo);
                    pstUsuario.executeUpdate();

                    try (ResultSet generatedKeys = pstUsuario.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            nuevoUsuarioId = generatedKeys.getInt(1);
                        } else {
                            throw new SQLException("Fallo al crear el usuario, no se obtuvo el ID.");
                        }
                    }
                }

                String sqlMecanico = "INSERT INTO tbMecanicos (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstMecanico = cnx.prepareStatement(sqlMecanico)) {
                    pstMecanico.setInt(1, nuevoUsuarioId);
                    pstMecanico.setString(2, nombre);
                    pstMecanico.setString(3, apellido);
                    pstMecanico.setString(4, telefono);
                    pstMecanico.setString(5, direccion);
                    pstMecanico.setString(6, dui);
                    pstMecanico.executeUpdate();
                }

                cnx.commit();
                mostrarAlerta("Éxito", "Mecánico y usuario creados correctamente.");
                mecanicosController.cargarMecanicos();
                cancelar();
            } else {
                // Lógica de actualización
                int idUsuario = obtenerIdUsuarioPorMecanico(mecanicoParaEditar.getIdMecanico());

                // Solo actualizamos la contraseña si el campo NO está vacío
                if (!contra.isEmpty()) {
                    String sqlUsuario = "UPDATE tbUsuarios SET usuario = ?, contra = ?, correo = ? WHERE idUsuario = ?";
                    try (PreparedStatement pstUsuario = cnx.prepareStatement(sqlUsuario)) {
                        pstUsuario.setString(1, usuario);
                        pstUsuario.setString(2, contraHasheada); // Se actualiza la contraseña
                        pstUsuario.setString(3, correo);
                        pstUsuario.setInt(4, idUsuario);
                        pstUsuario.executeUpdate();
                    }
                } else {
                    // Si el campo está vacío, no incluimos la contraseña en la consulta
                    String sqlUsuario = "UPDATE tbUsuarios SET usuario = ?, correo = ? WHERE idUsuario = ?";
                    try (PreparedStatement pstUsuario = cnx.prepareStatement(sqlUsuario)) {
                        pstUsuario.setString(1, usuario);
                        pstUsuario.setString(2, correo); // No se toca la contraseña
                        pstUsuario.setInt(3, idUsuario);
                        pstUsuario.executeUpdate();
                    }
                }

                String sqlMecanico = "UPDATE tbMecanicos SET nombre = ?, apellido = ?, telefono = ?, direccion = ?, dui = ? WHERE idMecanico = ?";
                try (PreparedStatement pstMecanico = cnx.prepareStatement(sqlMecanico)) {
                    pstMecanico.setString(1, nombre);
                    pstMecanico.setString(2, apellido);
                    pstMecanico.setString(3, telefono);
                    pstMecanico.setString(4, direccion);
                    pstMecanico.setString(5, dui);
                    pstMecanico.setInt(6, mecanicoParaEditar.getIdMecanico());
                    pstMecanico.executeUpdate();
                }

                cnx.commit();
                mostrarAlerta("Éxito", "Mecánico y usuario actualizados correctamente.");
                mecanicosController.cargarMecanicos();
                cancelar();
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Base de Datos", "No se pudo registrar al mecánico. Error: " + e.getMessage());
            try {
                if (cnx != null) {
                    System.err.println("Transacción revertida.");
                    cnx.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (cnx != null) {
                    cnx.setAutoCommit(true);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    @FXML
    void cancelar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void cargarDatosUsuario(int idUsuario) {
        String sql = "SELECT usuario, contra, correo FROM tbUsuarios WHERE idUsuario = ?";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, idUsuario);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    tfUsuario.setText(rs.getString("usuario"));
                    pfContra.setText(rs.getString("contra"));
                    tfCorreo.setText(rs.getString("correo"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudieron cargar los datos del usuario.");
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

