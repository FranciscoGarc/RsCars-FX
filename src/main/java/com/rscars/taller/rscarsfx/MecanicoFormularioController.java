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
        // No se requiere lógica especial al inicializar
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

        // Obtener idUsuario desde la BD usando idMecanico
        int idUsuario = obtenerIdUsuarioPorMecanico(mecanico.getIdMecanico());
        if (idUsuario != 0) {
            cargarDatosUsuario(idUsuario);
        }
    }

    @FXML
    void guardarMecanico() {
        if (tfNombre.getText().isEmpty() || tfDui.getText().isEmpty() || tfUsuario.getText().isEmpty() || obtenerPassword().isEmpty()) {
            mostrarAlerta("Error de Validación", "Nombre, DUI, Usuario y Contraseña son campos obligatorios.");
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
                    pstUsuario.setInt(1, 1); // idTipo 1 = Mecánico
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

                String sqlMecanico = "INSERT INTO tbMecanicos (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstMecanico = cnx.prepareStatement(sqlMecanico)) {
                    pstMecanico.setInt(1, nuevoUsuarioId);
                    pstMecanico.setString(2, tfNombre.getText().trim());
                    pstMecanico.setString(3, tfApellido.getText().trim());
                    pstMecanico.setString(4, tfTelefono.getText().trim());
                    pstMecanico.setString(5, tfDireccion.getText().trim());
                    pstMecanico.setString(6, tfDui.getText().trim());
                    pstMecanico.executeUpdate();
                }

                cnx.commit();
                mostrarAlerta("Éxito", "Mecánico y usuario creados correctamente.");
                mecanicosController.cargarMecanicos();
                cancelar();
            } else {
                // Lógica de actualización
                int idUsuario = obtenerIdUsuarioPorMecanico(mecanicoParaEditar.getIdMecanico());

                String sqlUsuario = "UPDATE tbUsuarios SET usuario = ?, contra = ?, correo = ? WHERE idUsuario = ?";
                try (PreparedStatement pstUsuario = cnx.prepareStatement(sqlUsuario)) {
                    pstUsuario.setString(1, usuario);
                    pstUsuario.setString(2, contra);
                    pstUsuario.setString(3, correo);
                    pstUsuario.setInt(4, idUsuario);
                    pstUsuario.executeUpdate();
                }

                String sqlMecanico = "UPDATE tbMecanicos SET nombre = ?, apellido = ?, telefono = ?, direccion = ?, dui = ? WHERE idMecanico = ?";
                try (PreparedStatement pstMecanico = cnx.prepareStatement(sqlMecanico)) {
                    pstMecanico.setString(1, tfNombre.getText().trim());
                    pstMecanico.setString(2, tfApellido.getText().trim());
                    pstMecanico.setString(3, tfTelefono.getText().trim());
                    pstMecanico.setString(4, tfDireccion.getText().trim());
                    pstMecanico.setString(5, tfDui.getText().trim());
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

