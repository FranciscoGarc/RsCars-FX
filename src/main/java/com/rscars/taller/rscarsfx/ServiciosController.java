package com.rscars.taller.rscarsfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Optional;

public class ServiciosController implements Initializable {
    @FXML private TableView<Servicio> tablaServicios;
    @FXML private TableColumn<Servicio, Integer> colId;
    @FXML private TableColumn<Servicio, String> colDescripcion;
    @FXML private TableColumn<Servicio, Double> colCosto;
    @FXML private Button btnNuevo;
    @FXML private Button btnEditar;
    @FXML private Button btnEliminar;

    private ObservableList<Servicio> listaServicios;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listaServicios = FXCollections.observableArrayList();
        this.colId.setCellValueFactory(new PropertyValueFactory<>("idServicio"));
        this.colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        this.colCosto.setCellValueFactory(new PropertyValueFactory<>("costo"));

        this.colId.setVisible(false); // Ocultar columna ID servicio

        cargarServicios();
    }

    public void cargarServicios() {
        listaServicios.clear();
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        String sql = "SELECT idServicio, descripcion, costo FROM tbServicios";
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("idServicio");
                String descripcion = rs.getString("descripcion");
                double costo = rs.getDouble("costo");
                listaServicios.add(new Servicio(id, descripcion.trim(), costo));
            }
            tablaServicios.setItems(listaServicios);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar los servicios de la base de datos.");
        }
    }

    @FXML
    private void handleNuevoServicio() {
        abrirFormularioServicio(null);
    }

    @FXML
    private void handleEditarServicio() {
        Servicio seleccionado = tablaServicios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un servicio para editar.");
            return;
        }
        abrirFormularioServicio(seleccionado);
    }

    @FXML
    private void handleEliminarServicio() {
        Servicio seleccionado = tablaServicios.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un servicio para eliminar.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de que desea eliminar el servicio?");
        alert.setContentText(seleccionado.getDescripcion());
        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            String sql = "DELETE FROM tbServicios WHERE idServicio = ?";
            Connection cnx = ConexionDB.obtenerInstancia().getCnx();
            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setInt(1, seleccionado.getIdServicio());
                pst.executeUpdate();
                cargarServicios();
                mostrarAlerta("Éxito", "Servicio eliminado correctamente.");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error de BD", "No se pudo eliminar el servicio.");
            }
        }
    }

    private void abrirFormularioServicio(Servicio servicio) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("servicio-formulario-view.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("styles.css").toExternalForm());
            ServicioFormularioController formularioController = loader.getController();
            formularioController.setServiciosController(this);
            if (servicio != null) {
                formularioController.setServicioParaEditar(servicio);
            }
            Stage stage = new Stage();
            stage.setTitle(servicio == null ? "Nuevo Servicio" : "Editar Servicio");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.setResizable(false);
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
