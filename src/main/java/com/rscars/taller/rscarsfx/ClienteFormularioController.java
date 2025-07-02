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

public class ClienteFormularioController {

    @FXML private Label lblTitulo;
    @FXML private TextField tfNombre, tfApellido, tfTelefono, tfDireccion, tfDui;
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
        // Validación simple
        if (tfNombre.getText().isEmpty() || tfApellido.getText().isEmpty()) {
            mostrarAlerta("Error de Validación", "Nombre y Apellido son campos obligatorios.");
            return;
        }

        String sql;
        if (esNuevo) {
            sql = "INSERT INTO tbClientes (nombre, apellido, telefono, direccion, dui) VALUES (?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE tbClientes SET nombre = ?, apellido = ?, telefono = ?, direccion = ?, dui = ? WHERE idCliente = ?";
        }

        // --- INICIO DE LA CORRECCIÓN ---
        // Obtenemos la conexión ANTES del try
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();

        // El try-with-resources ahora solo gestiona el PreparedStatement
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setString(1, tfNombre.getText().trim());
            pst.setString(2, tfApellido.getText().trim());
            pst.setString(3, tfTelefono.getText().trim());
            pst.setString(4, tfDireccion.getText().trim());
            pst.setString(5, tfDui.getText().trim());
            if (!esNuevo) {
                pst.setInt(6, clienteParaEditar.getIdCliente());
            }

            pst.executeUpdate();
            mostrarAlerta("Éxito", "Cliente guardado correctamente.");

            // Refrescar la tabla en la vista principal
            clientesController.cargarClientes();

            // Cerrar la ventana del formulario
            cancelar();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo guardar el cliente.");
        }
    }

    @FXML
    void cancelar() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}