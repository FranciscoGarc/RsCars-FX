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

public class ServicioFormularioController implements Initializable {
    @FXML private Label lblTitulo;
    @FXML private TextField tfDescripcion;
    @FXML private TextField tfCosto;
    @FXML private Button btnGuardar, btnCancelar;

    private Servicio servicioParaEditar;
    private ServiciosController serviciosController;
    private boolean esNuevo = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // No se requiere cargar datos externos
    }

    public void setServiciosController(ServiciosController controller) {
        this.serviciosController = controller;
    }

    public void setServicioParaEditar(Servicio servicio) {
        this.servicioParaEditar = servicio;
        this.esNuevo = false;
        lblTitulo.setText("Editar Servicio");
        tfDescripcion.setText(servicio.getDescripcion());
        tfCosto.setText(String.valueOf(servicio.getCosto()));
    }

    @FXML
    void guardarServicio() {
        if (tfDescripcion.getText().isEmpty() || tfCosto.getText().isEmpty()) {
            mostrarAlerta("Error de Validación", "La descripción y el costo son obligatorios.");
            return;
        }
        double costo;
        try {
            costo = Double.parseDouble(tfCosto.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Validación", "El costo debe ser un número válido.");
            return;
        }
        String sql;
        if (esNuevo) {
            sql = "INSERT INTO tbServicios (descripcion, costo) VALUES (?, ?)";
        } else {
            sql = "UPDATE tbServicios SET descripcion = ?, costo = ? WHERE idServicio = ?";
        }
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, tfDescripcion.getText().trim());
            pst.setDouble(2, costo);
            if (!esNuevo) {
                pst.setInt(3, servicioParaEditar.getIdServicio());
            }
            pst.executeUpdate();
            mostrarAlerta("Éxito", "Servicio guardado correctamente.");
            serviciosController.cargarServicios();
            cancelar();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo guardar el servicio. Verifique los datos.");
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

