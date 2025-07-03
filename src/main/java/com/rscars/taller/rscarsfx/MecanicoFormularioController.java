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

public class MecanicoFormularioController implements Initializable {
    @FXML private Label lblTitulo;
    @FXML private TextField tfNombre, tfApellido, tfTelefono, tfDireccion, tfDui, tfIdUsuario;
    @FXML private Button btnGuardar, btnCancelar;

    private Mecanico mecanicoParaEditar;
    private MecanicosController mecanicosController;
    private boolean esNuevo = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // No se requiere lógica especial al inicializar
    }

    public void setMecanicosController(MecanicosController controller) {
        this.mecanicosController = controller;
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
        tfIdUsuario.setText(String.valueOf(mecanico.getIdUsuario()));
    }

    @FXML
    void guardarMecanico() {
        if (tfNombre.getText().isEmpty() || tfIdUsuario.getText().isEmpty()) {
            mostrarAlerta("Error de Validación", "El Nombre y el ID Usuario son obligatorios.");
            return;
        }
        int idUsuario;
        try {
            idUsuario = Integer.parseInt(tfIdUsuario.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAlerta("Error de Validación", "ID Usuario debe ser un número.");
            return;
        }
        String sql;
        if (esNuevo) {
            sql = "INSERT INTO tbMecanicos (idUsuario, nombre, apellido, telefono, direccion, dui) VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE tbMecanicos SET idUsuario = ?, nombre = ?, apellido = ?, telefono = ?, direccion = ?, dui = ? WHERE idMecanico = ?";
        }
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, idUsuario);
            pst.setString(2, tfNombre.getText().trim());
            pst.setString(3, tfApellido.getText().trim());
            pst.setString(4, tfTelefono.getText().trim());
            pst.setString(5, tfDireccion.getText().trim());
            pst.setString(6, tfDui.getText().trim());
            if (!esNuevo) {
                pst.setInt(7, mecanicoParaEditar.getIdMecanico());
            }
            pst.executeUpdate();
            mostrarAlerta("Éxito", "Mecánico guardado correctamente.");
            mecanicosController.cargarMecanicos();
            cancelar();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo guardar el mecánico. Verifique los datos.");
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

