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

public class VehiculoFormularioController {

    @FXML private Label lblTitulo;
    @FXML private TextField tfMarca, tfModelo, tfAnio, tfPlaca, tfIdCliente;
    @FXML private Button btnGuardar, btnCancelar;

    private Vehiculo vehiculoParaEditar;
    private VehiculosController vehiculosController;
    private boolean esNuevo = true;

    public void setVehiculosController(VehiculosController controller) {
        this.vehiculosController = controller;
    }

    public void setVehiculoParaEditar(Vehiculo vehiculo) {
        this.vehiculoParaEditar = vehiculo;
        this.esNuevo = false;
        lblTitulo.setText("Editar Vehículo");
        tfMarca.setText(vehiculo.getMarca());
        tfModelo.setText(vehiculo.getModelo());
        tfAnio.setText(String.valueOf(vehiculo.getAnio()));
        tfPlaca.setText(vehiculo.getPlaca());
        tfIdCliente.setText(String.valueOf(vehiculo.getIdCliente()));
    }

    @FXML
    void guardarVehiculo() {
        if (tfMarca.getText().isEmpty() || tfModelo.getText().isEmpty() || tfPlaca.getText().isEmpty()) {
            mostrarAlerta("Error de Validación", "Marca, Modelo y Placa son campos obligatorios.");
            return;
        }

        String sql;
        if (esNuevo) {
            sql = "INSERT INTO tbVehiculos (marca, modelo, año, placa, idCliente) VALUES (?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE tbVehiculos SET marca = ?, modelo = ?, año = ?, placa = ?, idCliente = ? WHERE idVehiculo = ?";
        }

        Connection cnx = ConexionDB.obtenerInstancia().getCnx();

        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setString(1, tfMarca.getText().trim());
            pst.setString(2, tfModelo.getText().trim());
            pst.setInt(3, Integer.parseInt(tfAnio.getText().trim()));
            pst.setString(4, tfPlaca.getText().trim());
            pst.setInt(5, Integer.parseInt(tfIdCliente.getText().trim()));
            if (!esNuevo) {
                pst.setInt(6, vehiculoParaEditar.getIdVehiculo());
            }

            pst.executeUpdate();
            mostrarAlerta("Éxito", "Vehículo guardado correctamente.");

            vehiculosController.cargarVehiculos();
            cancelar();

        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo guardar el vehículo. Verifique los datos.");
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