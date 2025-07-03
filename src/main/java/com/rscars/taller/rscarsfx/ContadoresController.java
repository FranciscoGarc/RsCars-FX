package com.rscars.taller.rscarsfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.*;
import java.util.Optional;
import java.util.ResourceBundle;
import java.io.IOException;

public class ContadoresController implements Initializable {

    @FXML
    private TableView<Contador> tablaContadores;
    @FXML
    private TableColumn<Contador, Integer> colIdEmpleado;
    @FXML
    private TableColumn<Contador, Integer> colIdUsuario;
    @FXML
    private TableColumn<Contador, String> colNombre;
    @FXML
    private TableColumn<Contador, String> colApellido;
    @FXML
    private TableColumn<Contador, String> colTelefono;
    @FXML
    private TableColumn<Contador, String> colDireccion;
    @FXML
    private TableColumn<Contador, String> colDui;

    @FXML
    private Button btnNuevo;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnEliminar;

    private ObservableList<Contador> listaContadores;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listaContadores = FXCollections.observableArrayList();

        colIdEmpleado.setCellValueFactory(new PropertyValueFactory<>("idEmpleado"));
        colIdUsuario.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colDui.setCellValueFactory(new PropertyValueFactory<>("dui"));

        cargarContadores();
    }

    public void cargarContadores() {
        listaContadores.clear();
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        String sql = "SELECT idEmpleado, idUsuario, nombre, apellido, telefono, direccion, dui FROM tbContadores";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                listaContadores.add(new Contador(
                        rs.getInt("idEmpleado"),
                        rs.getInt("idUsuario"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("dui")
                ));
            }
            tablaContadores.setItems(listaContadores);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar los contadores de la base de datos.");
        }
    }

    @FXML
    private void handleNuevoContador() {
        abrirFormularioContador(null);
    }

    @FXML
    private void handleEditarContador() {
        Contador seleccionado = tablaContadores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un contador para editar.");
            return;
        }
        abrirFormularioContador(seleccionado);
    }

    @FXML
    private void handleEliminarContador() {
        Contador seleccionado = tablaContadores.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un contador para eliminar.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de que desea eliminar el contador?");
        alert.setContentText(seleccionado.getNombre() + " " + seleccionado.getApellido());

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            String sql = "DELETE FROM tbContadores WHERE idEmpleado = ?";
            Connection cnx = ConexionDB.obtenerInstancia().getCnx();

            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setInt(1, seleccionado.getIdEmpleado());
                pst.executeUpdate();
                cargarContadores();
                mostrarAlerta("Éxito", "Contador eliminado correctamente.");
            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error de BD", "No se pudo eliminar el contador.");
            }
        }
    }

    private void abrirFormularioContador(Contador contador) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("contador-formulario-view.fxml"));
            Scene scene = new Scene(loader.load());

            ContadorFormularioController formularioController = loader.getController();
            formularioController.setContadoresController(this);

            if (contador != null) {
                formularioController.setContadorParaEditar(contador);
            }

            Stage stage = new Stage();
            stage.setTitle(contador == null ? "Nuevo Contador" : "Editar Contador");
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

