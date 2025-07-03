package com.rscars.taller.rscarsfx;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class ContadorFormularioController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private TextField tfIdUsuario, tfNombre, tfApellido, tfTelefono, tfDireccion, tfDui;
    @FXML private Button btnGuardar, btnCancelar;

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

    public void setContadorParaEditar(Contador contador) {
        this.contadorParaEditar = contador;
        this.esNuevo = false;

        lblTitulo.setText("Editar Contador");
        tfIdUsuario.setText(String.valueOf(contador.getIdUsuario()));
        tfNombre.setText(contador.getNombre());
        tfApellido.setText(contador.getApellido());
        tfTelefono.setText(contador.getTelefono());
        tfDireccion.setText(contador.getDireccion());
        tfDui.setText(contador.getDui());
    }

    @FXML
    void guardarContador() {
        if (tfNombre.getText().isEmpty() || tfApellido.getText().isEmpty()) {
            mostrarAlerta("Error de Validación", "Nombre y Apellido son obligatorios.");
            return;
        }

        String sql;
        if (esNuevo) {
            sql = "INSERT INTO tbContadores (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE tbContadores SET idUsuario = ?, nombre = ?, apellido = ?, telefono = ?, direccion = ?, dui = ? WHERE idEmpleado = ?";
        }

        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, Integer.parseInt(tfIdUsuario.getText().trim()));
            pst.setString(2, tfNombre.getText().trim());
            pst.setString(3, tfApellido.getText().trim());
            pst.setString(4, tfTelefono.getText().trim());
            pst.setString(5, tfDireccion.getText().trim());
            pst.setString(6, tfDui.getText().trim());

            if (!esNuevo) {
                pst.setInt(7, contadorParaEditar.getIdEmpleado());
            }

            pst.executeUpdate();
            mostrarAlerta("Éxito", "Contador guardado correctamente.");

            contadoresController.cargarContadores();
            cancelar();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo guardar el contador. Verifique los datos.");
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

