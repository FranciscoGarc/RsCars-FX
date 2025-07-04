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

public class ClienteFormularioController {

    @FXML private Label lblTitulo;
    @FXML private TextField tfNombre, tfApellido, tfTelefono, tfDireccion, tfDui, tfCorreo, tfUsuario;
    @FXML private PasswordField pfContra;
    @FXML private Button btnGuardar, btnCancelar;

    private Cliente clienteParaEditar;
    private ClientesController clientesController; // Referencia al controlador principal
    private boolean esNuevo = true;

    public void setClientesController(ClientesController controller) {
        this.clientesController = controller;
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

    }

    @FXML
    void guardarCliente() {
        // Validación (DUI y Correo son importantes para el usuario)
        if (tfNombre.getText().isEmpty() || tfDui.getText().isEmpty() || tfUsuario.getText().isEmpty() || pfContra.getText().isEmpty()) {
            mostrarAlerta("Error de Validación", "Nombre, DUI, Usuario y Contraseña son campos obligatorios.");
            return;
        }

        // Solo manejaremos la creación de nuevos clientes con esta lógica.
        // La edición de un cliente existente no debería cambiar su usuario asociado.
        if (!esNuevo) {
            // Aquí iría la lógica de UPDATE para tbClientes que ya tenías.
            // Por ahora, nos centramos en la creación.
            mostrarAlerta("Información", "La edición de usuarios asociados no está implementada aún.");
            return;
        }

        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try {
            cnx.setAutoCommit(false);

            // --- INICIO DE LA MODIFICACIÓN ---
            // Usamos los datos de los nuevos campos
            String usuario = tfUsuario.getText().trim();
            String contra = pfContra.getText().trim();
            String correo = tfCorreo.getText().trim(); // Puedes añadir un campo para el correo si lo deseas

            String sqlUsuario = "INSERT INTO tbUsuarios (idTipo, usuario, contra, correo) VALUES (?, ?, ?, ?)";
            int nuevoUsuarioId = 0;
            // --- FIN DE LA MODIFICACIÓN ---

            try (PreparedStatement pstUsuario = cnx.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                pstUsuario.setInt(1, 3); // idTipo 3 = Cliente
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

            // 3. Insertar en tbClientes con el nuevo ID de usuario
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

            // 4. Si todo fue bien, confirmar la transacción
            cnx.commit();
            mostrarAlerta("Éxito", "Cliente y usuario creados correctamente.");

            clientesController.cargarClientes();
            cancelar();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de Base de Datos", "No se pudo registrar al cliente. Error: " + e.getMessage());
            try {
                // 5. En caso de error, revertir la transacción
                if (cnx != null) {
                    System.err.println("Transacción revertida.");
                    cnx.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                // 6. Restaurar el auto-commit en cualquier caso
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

                    // Si tienes un campo para el correo, lo llenas aquí
                    // tfCorreo.setText(rs.getString("correo"));
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