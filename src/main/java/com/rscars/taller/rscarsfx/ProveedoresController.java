package com.rscars.taller.rscarsfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProveedoresController implements Initializable {
    @FXML private TableView<Proveedor> tablaProveedores;
    @FXML private TableColumn<Proveedor, Integer> colIdProveedor;
    @FXML private TableColumn<Proveedor, String> colNombre;
    @FXML private TableColumn<Proveedor, String> colTelefono;
    @FXML private TableColumn<Proveedor, String> colCorreo;
    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;

    private ObservableList<Proveedor> listaProveedores;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listaProveedores = FXCollections.observableArrayList();
        this.colIdProveedor.setCellValueFactory(new PropertyValueFactory<>("idProveedor"));
        this.colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        this.colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        this.colCorreo.setCellValueFactory(new PropertyValueFactory<>("correo"));

        this.colIdProveedor.setVisible(false); // Ocultar columna ID proveedor

        cargarProveedores();
    }

    public void cargarProveedores() {
        listaProveedores.clear();
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        String sql = "SELECT idProveedor, nombre, telefono, correo FROM tbProveedores";
        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("idProveedor");
                String nombre = rs.getString("nombre");
                String telefono = rs.getString("telefono");
                String correo = rs.getString("correo");
                listaProveedores.add(new Proveedor(id, nombre.trim(), telefono.trim(), correo.trim()));
            }
            tablaProveedores.setItems(listaProveedores);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar los proveedores de la base de datos.");
        }
    }

    @FXML
    private void handleNuevoProveedor() {
        abrirFormularioProveedor(null);
    }

    @FXML
    private void handleEditarProveedor() {
        Proveedor seleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un proveedor para editar.");
            return;
        }
        abrirFormularioProveedor(seleccionado);
    }

    @FXML
    private void handleEliminarProveedor() {
        Proveedor seleccionado = tablaProveedores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un proveedor para eliminar.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de que desea eliminar el proveedor?");
        alert.setContentText(seleccionado.getNombre());
        Optional<javafx.scene.control.ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == javafx.scene.control.ButtonType.OK) {
            String sql = "DELETE FROM tbProveedores WHERE idProveedor = ?";
            Connection cnx = ConexionDB.obtenerInstancia().getCnx();
            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setInt(1, seleccionado.getIdProveedor());
                pst.executeUpdate();
                cargarProveedores();
                mostrarAlerta("Éxito", "Proveedor eliminado correctamente.");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error de BD", "No se pudo eliminar el proveedor.");
            }
        }
    }

    private void abrirFormularioProveedor(Proveedor proveedor) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("proveedor-formulario-view.fxml"));
            Scene scene = new Scene(loader.load());
            ProveedorFormularioController formularioController = loader.getController();
            formularioController.setProveedoresController(this);
            if (proveedor != null) {
                formularioController.setProveedorParaEditar(proveedor);
            }
            Stage stage = new Stage();
            stage.setTitle(proveedor == null ? "Nuevo Proveedor" : "Editar Proveedor");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
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
