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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ResourceBundle;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.sql.PreparedStatement;
import java.io.IOException;
import java.util.Optional;

public class VehiculosController implements Initializable {

    @FXML
    private TableView<Vehiculo> tablaVehiculos;
    @FXML
    private TableColumn<Vehiculo, Integer> colId;
    @FXML
    private TableColumn<Vehiculo, String> colMarca;
    @FXML
    private TableColumn<Vehiculo, String> colModelo;
    @FXML
    private TableColumn<Vehiculo, Integer> colAnio;
    @FXML
    private TableColumn<Vehiculo, String> colPlaca;
    @FXML
    private TableColumn<Vehiculo, Integer> colIdCliente;

    @FXML
    private Button btnNuevo;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnEliminar;

    private ObservableList<Vehiculo> listaVehiculos;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listaVehiculos = FXCollections.observableArrayList();

        this.colId.setCellValueFactory(new PropertyValueFactory<>("idVehiculo"));
        this.colMarca.setCellValueFactory(new PropertyValueFactory<>("marca"));
        this.colModelo.setCellValueFactory(new PropertyValueFactory<>("modelo"));
        this.colAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));
        this.colPlaca.setCellValueFactory(new PropertyValueFactory<>("placa"));
        this.colIdCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));

        this.colId.setVisible(false); // Ocultar la columna de ID si no es necesaria en la vista
        this.colIdCliente.setVisible(false);

        cargarVehiculos();
    }

    public void cargarVehiculos() {
        listaVehiculos.clear();
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        String sql = "SELECT idVehiculo, marca, modelo, año, placa, idCliente FROM tbVehiculos";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("idVehiculo");
                String marca = rs.getString("marca");
                String modelo = rs.getString("modelo");
                int anio = rs.getInt("año");
                String placa = rs.getString("placa");
                int idCliente = rs.getInt("idCliente");
                listaVehiculos.add(new Vehiculo(id, marca.trim(), modelo.trim(), anio, placa.trim(), idCliente));
            }
            tablaVehiculos.setItems(listaVehiculos);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar los vehículos de la base de datos.");
        }
    }

    @FXML
    private void handleNuevoVehiculo() {
        abrirFormularioVehiculo(null);
    }

    @FXML
    private void handleEditarVehiculo() {
        Vehiculo seleccionado = tablaVehiculos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un vehículo para editar.");
            return;
        }
        abrirFormularioVehiculo(seleccionado);
    }

    @FXML
    private void handleEliminarVehiculo() {
        Vehiculo seleccionado = tablaVehiculos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un vehículo para eliminar.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de que desea eliminar el vehículo?");
        alert.setContentText(seleccionado.getMarca() + " " + seleccionado.getModelo() + " (" + seleccionado.getPlaca() + ")");

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            String sql = "DELETE FROM tbVehiculos WHERE idVehiculo = ?";
            Connection cnx = ConexionDB.obtenerInstancia().getCnx();

            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setInt(1, seleccionado.getIdVehiculo());
                pst.executeUpdate();
                cargarVehiculos(); // Refrescar la tabla
                mostrarAlerta("Éxito", "Vehículo eliminado correctamente.");

            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error de BD", "No se pudo eliminar el vehículo.");
            }
        }
    }

    private void abrirFormularioVehiculo(Vehiculo vehiculo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("vehiculo-formulario-view.fxml"));
            Scene scene = new Scene(loader.load());

            VehiculoFormularioController formularioController = loader.getController();
            formularioController.setVehiculosController(this);

            if (vehiculo != null) {
                formularioController.setVehiculoParaEditar(vehiculo);
            }

            Stage stage = new Stage();
            stage.setTitle(vehiculo == null ? "Nuevo Vehículo" : "Editar Vehículo");
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