package com.rscars.taller.rscarsfx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.*;

import java.util.ResourceBundle;

public class ContadorFormularioController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private TextField tfNombre, tfApellido, tfTelefono, tfDireccion, tfDui, tfCorreo, tfUsuario;
    @FXML private Button btnGuardar, btnCancelar;
    @FXML private PasswordField pfContra;
    @FXML private TextField tfContraVisible;

    private Contador contadorParaEditar;
    private ContadoresController contadoresController;
    private boolean esNuevo = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // No se requiere lógica especial al inicializar
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

        int idUsuario = obtenerIdUsuarioPorContador(contador.getIdEmpleado());
        if (idUsuario != 0) {
            cargarDatosUsuario(idUsuario);
        }
    }

    @FXML
    void guardarContador() {
        if (tfNombre.getText().isEmpty() || tfUsuario.getText().isEmpty() || obtenerPassword().isEmpty()) {
            mostrarAlerta("Error de Validación", "Nombre, Usuario y Contraseña son campos obligatorios.");
            return;
        }

        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try {
            cnx.setAutoCommit(false);

            String usuario = tfUsuario.getText().trim();
            String contra = obtenerPassword().trim();
            String correo = tfCorreo.getText().trim();

            if (esNuevo) {
                String sqlUsuario = "INSERT INTO tbUsuarios (idTipo, usuario, contra, correo) VALUES (?, ?, ?, ?)";
                int nuevoUsuarioId = 0;

                try (PreparedStatement pstUsuario = cnx.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                    pstUsuario.setInt(1, 2); // idTipo 2 = Contador
                    pstUsuario.setString(2, usuario);
                    pstUsuario.setString(3, contra);
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
                    pstContador.setString(2, tfNombre.getText().trim());
                    pstContador.setString(3, tfApellido.getText().trim());
                    pstContador.setString(4, tfTelefono.getText().trim());
                    pstContador.setString(5, tfDireccion.getText().trim());
                    pstContador.setString(6, tfDui.getText().trim());
                    pstContador.executeUpdate();
                }

                cnx.commit();
                mostrarAlerta("Éxito", "Contador y usuario creados correctamente.");

                contadoresController.cargarContadores();
                cancelar();
            } else {
                int idUsuario = obtenerIdUsuarioPorContador(contadorParaEditar.getIdEmpleado());

                String sqlUsuario = "UPDATE tbUsuarios SET usuario = ?, contra = ?, correo = ? WHERE idUsuario = ?";
                try (PreparedStatement pstUsuario = cnx.prepareStatement(sqlUsuario)) {
                    pstUsuario.setString(1, usuario);
                    pstUsuario.setString(2, contra);
                    pstUsuario.setString(3, correo);
                    pstUsuario.setInt(4, idUsuario);
                    pstUsuario.executeUpdate();
                }

                String sqlContador = "UPDATE tbContadores SET nombre = ?, apellido = ?, telefono = ?, direccion = ?, dui = ? WHERE idEmpleado = ?";
                try (PreparedStatement pstContador = cnx.prepareStatement(sqlContador)) {
                    pstContador.setString(1, tfNombre.getText().trim());
                    pstContador.setString(2, tfApellido.getText().trim());
                    pstContador.setString(3, tfTelefono.getText().trim());
                    pstContador.setString(4, tfDireccion.getText().trim());
                    pstContador.setString(5, tfDui.getText().trim());
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
