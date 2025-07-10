package com.rscars.taller.rscarsfx;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import javafx.scene.control.PasswordField;
import org.mindrot.jbcrypt.BCrypt;

public class ClienteFormularioController {

    @FXML
    private Label lblTitulo;
    @FXML
    private TextField tfNombre, tfApellido, tfTelefono, tfDireccion, tfDui, tfCorreo, tfUsuario;
    @FXML
    private Button btnGuardar, btnCancelar;
    @FXML
    private PasswordField pfContra;
    @FXML
    private TextField tfContraVisible;

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

    private Cliente clienteParaEditar;
    private ClientesController clientesController; // Referencia al controlador principal
    private boolean esNuevo = true;

    public void setClientesController(ClientesController controller) {
        this.clientesController = controller;
    }

    private int obtenerIdUsuarioPorCliente(int idCliente) {
        String sql = "SELECT idUsuario FROM tbClientes WHERE idCliente = ?";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, idCliente);
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

    public void setClienteParaEditar(Cliente cliente) {
        this.clienteParaEditar = cliente;
        this.esNuevo = false;
        lblTitulo.setText("Editar Cliente");
        tfNombre.setText(cliente.getNombre());
        tfApellido.setText(cliente.getApellido());
        tfTelefono.setText(cliente.getTelefono());
        tfDireccion.setText(cliente.getDireccion());
        tfDui.setText(cliente.getDui());
        // NO cargamos la contraseña. Los campos se dejan en blanco.
        // Para guiar al usuario, puedes cambiar el texto de ayuda (prompt text).
        pfContra.setPromptText("Dejar en blanco para no cambiar");
        tfContraVisible.setPromptText("Dejar en blanco para no cambiar");

        int idUsuario = obtenerIdUsuarioPorCliente(cliente.getIdCliente());
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
    void guardarCliente() {
        // La validación para un nuevo cliente sigue siendo la misma
        if (esNuevo && (tfNombre.getText().isEmpty() || tfDui.getText().isEmpty() || tfUsuario.getText().isEmpty() || obtenerPassword().isEmpty())) {
            mostrarAlerta("Error de Validación", "Para un nuevo cliente, todos los campos son obligatorios.");
            return;
        }
        // Validación más simple para editar
        if (!esNuevo && (tfNombre.getText().isEmpty() || tfDui.getText().isEmpty() || tfUsuario.getText().isEmpty())) {
            mostrarAlerta("Error de Validación", "Nombre, DUI y Usuario son obligatorios.");
            return;
        }

        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try {
            cnx.setAutoCommit(false);

            String usuario = tfUsuario.getText().trim();
            String contra = obtenerPassword().trim();
            String contraHasheada = BCrypt.hashpw(contra, BCrypt.gensalt());
            String correo = tfCorreo.getText().trim();

            if (esNuevo) {
                String sqlUsuario = "INSERT INTO tbUsuarios (idTipo, usuario, contra, correo) VALUES (?, ?, ?, ?)";
                int nuevoUsuarioId = 0;

                try (PreparedStatement pstUsuario = cnx.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                    pstUsuario.setInt(1, 3); // idTipo 3 = Cliente
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

                String sqlCliente = "INSERT INTO tbClientes (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES (?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstCliente = cnx.prepareStatement(sqlCliente)) {
                    pstCliente.setInt(1, nuevoUsuarioId);
                    pstCliente.setString(2, tfNombre.getText().trim());
                    pstCliente.setString(3, tfApellido.getText().trim());
                    pstCliente.setString(4, tfTelefono.getText().trim());
                    pstCliente.setString(5, tfDireccion.getText().trim());
                    pstCliente.setString(6, tfDui.getText().trim());
                    pstCliente.executeUpdate();
                }

                cnx.commit();
                mostrarAlerta("Éxito", "Cliente y usuario creados correctamente.");

                clientesController.cargarClientes();
                cancelar();
            } else {
                // Lógica de actualización
                int idUsuario = obtenerIdUsuarioPorCliente(clienteParaEditar.getIdCliente());

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

                String sqlCliente = "UPDATE tbClientes SET nombre = ?, apellido = ?, telefono = ?, direccion = ?, dui = ? WHERE idCliente = ?";
                try (PreparedStatement pstCliente = cnx.prepareStatement(sqlCliente)) {
                    pstCliente.setString(1, tfNombre.getText().trim());
                    pstCliente.setString(2, tfApellido.getText().trim());
                    pstCliente.setString(3, tfTelefono.getText().trim());
                    pstCliente.setString(4, tfDireccion.getText().trim());
                    pstCliente.setString(5, tfDui.getText().trim());
                    pstCliente.setInt(6, clienteParaEditar.getIdCliente());
                    pstCliente.executeUpdate();
                }

                cnx.commit();
                mostrarAlerta("Éxito", "Cliente y usuario actualizados correctamente.");
                clientesController.cargarClientes();
                cancelar();
                return;
            }


        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Base de Datos", "No se pudo registrar al cliente. Error: " + e.getMessage());
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