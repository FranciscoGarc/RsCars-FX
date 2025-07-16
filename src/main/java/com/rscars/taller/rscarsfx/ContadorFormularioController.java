package com.rscars.taller.rscarsfx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
import java.util.ResourceBundle;

public class ContadorFormularioController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private TextField tfNombre, tfApellido, tfTelefono, tfDireccion, tfDui, tfCorreo, tfUsuario;
    @FXML private Button btnGuardar, btnCancelar;
    @FXML private Button togglePasswordVisibility;
    @FXML private PasswordField pfContra;
    @FXML private TextField tfContraVisible;

    private Contador contadorParaEditar;
    private ContadoresController contadoresController;
    private boolean esNuevo = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ValidationUtil.autoFormatPhone(tfTelefono);
        ValidationUtil.autoFormatDui(tfDui);
        togglePasswordVisibility.setGraphic(createEyeIcon());
    }

    // --- SVG de icono de ojo reutilizable ---
    private StackPane createEyeIcon() {
        SVGPath eyeIcon = new SVGPath();
        eyeIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5C21.27 7.61 17 4.5 12 4.5zm0 10c-2.48 0-4.5-2.02-4.5-4.5S9.52 5.5 12 5.5s4.5 2.02 4.5 4.5-2.02 4.5-4.5 4.5zm0-7C10.62 7.5 9.5 8.62 9.5 10s1.12 2.5 2.5 2.5 2.5-1.12 2.5-2.5S13.38 7.5 12 7.5z");
        eyeIcon.setStyle("-fx-fill: #36454F;");
        StackPane iconContainer = new StackPane(eyeIcon);
        iconContainer.setPrefSize(18, 18);
        return iconContainer;
    }

    public void setContadoresController(ContadoresController controller) {
        this.contadoresController = controller;
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

    private int obtenerIdUsuarioPorContador(int idEmpleado) {
        String sql = "SELECT idUsuario FROM tbContadores WHERE idEmpleado = ?";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, idEmpleado);
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

    public void setContadorParaEditar(Contador contador) {
        this.contadorParaEditar = contador;
        this.esNuevo = false;

        lblTitulo.setText("Editar Contador");
        tfNombre.setText(contador.getNombre());
        tfApellido.setText(contador.getApellido());
        tfTelefono.setText(contador.getTelefono());
        tfDireccion.setText(contador.getDireccion());
        tfDui.setText(contador.getDui());
        // NO cargamos la contraseña. Los campos se dejan en blanco.
        pfContra.setPromptText("Dejar en blanco para no cambiar");
        tfContraVisible.setPromptText("Dejar en blanco para no cambiar");

        int idUsuario = obtenerIdUsuarioPorContador(contador.getIdEmpleado());
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
            }        }
    }

    @FXML
    void guardarContador() {
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
                    pstUsuario.setInt(1, 2); // idTipo 2 = Contador
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

                String sqlContador = "INSERT INTO tbContadores (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstContador = cnx.prepareStatement(sqlContador)) {
                    pstContador.setInt(1, nuevoUsuarioId);
                    pstContador.setString(2, nombre);
                    pstContador.setString(3, apellido);
                    pstContador.setString(4, telefono);
                    pstContador.setString(5, direccion);
                    pstContador.setString(6, dui);
                    pstContador.executeUpdate();
                }

                cnx.commit();
                mostrarAlerta("Éxito", "Contador y usuario creados correctamente.");

                contadoresController.cargarContadores();
                cancelar();
            } else {
                int idUsuario = obtenerIdUsuarioPorContador(contadorParaEditar.getIdEmpleado());

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

                String sqlContador = "UPDATE tbContadores SET nombre = ?, apellido = ?, telefono = ?, direccion = ?, dui = ? WHERE idEmpleado = ?";
                try (PreparedStatement pstContador = cnx.prepareStatement(sqlContador)) {
                    pstContador.setString(1, nombre);
                    pstContador.setString(2, apellido);
                    pstContador.setString(3, telefono);
                    pstContador.setString(4, direccion);
                    pstContador.setString(5, dui);
                    pstContador.setInt(6, contadorParaEditar.getIdEmpleado());
                    pstContador.executeUpdate();
                }

                cnx.commit();
                mostrarAlerta("Éxito", "Contador y usuario actualizados correctamente.");
                contadoresController.cargarContadores();
                cancelar();
                return;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Base de Datos", "No se pudo registrar el contador. Error: " + e.getMessage());
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
