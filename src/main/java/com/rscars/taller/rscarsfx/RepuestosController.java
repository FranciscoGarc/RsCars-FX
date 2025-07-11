package com.rscars.taller.rscarsfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class RepuestosController implements Initializable {
    @FXML private TableView<Repuesto> tablaRepuestos;
    @FXML private TableColumn<Repuesto, Integer> colId;
    @FXML private TableColumn<Repuesto, String> colDescripcion;
    @FXML private TableColumn<Repuesto, Integer> colPrecio;
    @FXML private TableColumn<Repuesto, Integer> colStock;
    @FXML private TableColumn<Repuesto, Integer> colIdProveedor;
    @FXML private Button btnNuevo, btnEditar, btnEliminar;

    private ObservableList<Repuesto> listaRepuestos;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listaRepuestos = FXCollections.observableArrayList();
        colId.setCellValueFactory(new PropertyValueFactory<>("idRepuesto"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colIdProveedor.setCellValueFactory(new PropertyValueFactory<>("idProveedor"));

        this.colId.setVisible(false); // Ocultar la columna de ID si no es necesaria en la vista
        this.colIdProveedor.setVisible(false);
        cargarRepuestos();
    }

    public void cargarRepuestos() {
        listaRepuestos.clear();
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        String sql = "SELECT idRepuesto, descripcion, precio, stock, idProveedor FROM tbRepuestos";
        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("idRepuesto");
                String descripcion = rs.getString("descripcion");
                int precio = rs.getInt("precio");
                int stock = rs.getInt("stock");
                int idProveedor = rs.getInt("idProveedor");
                listaRepuestos.add(new Repuesto(id, descripcion.trim(), precio, stock, idProveedor));
            }
            tablaRepuestos.setItems(listaRepuestos);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar los repuestos de la base de datos.");
        }
    }

    @FXML
    private void handleNuevoRepuesto() {
        abrirFormularioRepuesto(null);
    }

    @FXML
    private void handleEditarRepuesto() {
        Repuesto seleccionado = tablaRepuestos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un repuesto para editar.");
            return;
        }
        abrirFormularioRepuesto(seleccionado);
    }

    @FXML
    private void handleEliminarRepuesto() {
        Repuesto seleccionado = tablaRepuestos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un repuesto para eliminar.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de que desea eliminar el repuesto?");
        alert.setContentText(seleccionado.getDescripcion());
        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            String sql = "DELETE FROM tbRepuestos WHERE idRepuesto = ?";
            Connection cnx = ConexionDB.obtenerInstancia().getCnx();
            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setInt(1, seleccionado.getIdRepuesto());
                pst.executeUpdate();
                cargarRepuestos();
                mostrarAlerta("Éxito", "Repuesto eliminado correctamente.");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error de BD", "No se pudo eliminar el repuesto.");
            }
        }
    }

    private void abrirFormularioRepuesto(Repuesto repuesto) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("repuesto-formulario-view.fxml"));
            Scene scene = new Scene(loader.load());
            RepuestoFormularioController formularioController = loader.getController();
            formularioController.setRepuestosController(this);
            if (repuesto != null) {
                formularioController.setRepuestoParaEditar(repuesto);
            }
            Stage stage = new Stage();
            stage.setTitle(repuesto == null ? "Nuevo Repuesto" : "Editar Repuesto");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
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

