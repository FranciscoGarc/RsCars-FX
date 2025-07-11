package com.rscars.taller.rscarsfx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ProveedorFormularioController implements Initializable {
    @FXML private Label lblTitulo;
    @FXML private TextField tfNombre, tfTelefono, tfCorreo;
    @FXML private Button btnGuardar, btnCancelar;

    private Proveedor proveedorParaEditar;
    private ProveedoresController proveedoresController;
    private boolean esNuevo = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Nada especial al iniciar
        ValidationUtil.autoFormatPhone(tfTelefono);
    }

    public void setProveedoresController(ProveedoresController controller) {
        this.proveedoresController = controller;
    }

    public void setProveedorParaEditar(Proveedor proveedor) {
        this.proveedorParaEditar = proveedor;
        this.esNuevo = false;
        lblTitulo.setText("Editar Proveedor");
        tfNombre.setText(proveedor.getNombre());
        tfTelefono.setText(proveedor.getTelefono());
        tfCorreo.setText(proveedor.getCorreo());
    }

    @FXML
    void guardarProveedor() {
        String nombre = tfNombre.getText().trim();
        String telefono = tfTelefono.getText().trim();
        String correo = tfCorreo.getText().trim();

        if (!ValidationUtil.isNotEmpty(nombre) || !ValidationUtil.isNotEmpty(telefono) || !ValidationUtil.isNotEmpty(correo)) {
            mostrarAlerta("Error de Validación", "Todos los campos son obligatorios.");
            return;
        }

        if (!ValidationUtil.isPhoneValid(telefono)) {
            mostrarAlerta("Error de Validación", "El teléfono debe tener 8 dígitos.");
            return;
        }

        if (!ValidationUtil.isValidEmail(correo)) {
            mostrarAlerta("Error de Validación", "El formato del correo electrónico no es válido.");
            return;
        }

        String sql;
        if (esNuevo) {
            sql = "INSERT INTO tbProveedores (nombre, telefono, correo) VALUES (?, ?, ?)";
        } else {
            sql = "UPDATE tbProveedores SET nombre = ?, telefono = ?, correo = ? WHERE idProveedor = ?";
        }
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, nombre);
            pst.setString(2, telefono);
            pst.setString(3, correo);
            if (!esNuevo) {
                pst.setInt(4, proveedorParaEditar.getIdProveedor());
            }
            pst.executeUpdate();
            mostrarAlerta("Éxito", "Proveedor guardado correctamente.");
            proveedoresController.cargarProveedores();
            cancelar();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo guardar el proveedor. Verifique los datos.");
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

