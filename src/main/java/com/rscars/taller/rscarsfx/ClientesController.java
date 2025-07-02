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

public class ClientesController implements Initializable {

    @FXML
    private TableView<Cliente> tablaClientes;
    @FXML
    private TableColumn<Cliente, Integer> colId;
    @FXML
    private TableColumn<Cliente, String> colNombre;
    @FXML
    private TableColumn<Cliente, String> colApellido;
    @FXML
    private TableColumn<Cliente, String> colTelefono;
    @FXML
    private TableColumn<Cliente, String> colDireccion;
    @FXML
    private TableColumn<Cliente, String> colDui;

    @FXML
    private Button btnNuevo;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnEliminar;

    private ObservableList<Cliente> listaClientes;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listaClientes = FXCollections.observableArrayList();

        // Asociar las columnas de la tabla con las propiedades del modelo Cliente
        this.colId.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        this.colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        this.colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        this.colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        this.colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        this.colDui.setCellValueFactory(new PropertyValueFactory<>("dui"));

        cargarClientes();
    }

    public void cargarClientes() {
        listaClientes.clear(); // Limpiar la lista antes de cargar para evitar duplicados
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        String sql = "SELECT idCliente, nombre, apellido, telefono, direccion, dui FROM tbClientes";

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // ... (la lógica para crear y añadir clientes que ya tenías)
                int id = rs.getInt("idCliente");
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                String telefono = rs.getString("telefono");
                String direccion = rs.getString("direccion");
                String dui = rs.getString("dui");
                listaClientes.add(new Cliente(id, nombre.trim(), apellido.trim(), telefono.trim(), direccion.trim(), dui.trim()));
            }
            tablaClientes.setItems(listaClientes);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Error al cargar los clientes de la base de datos.");
        }
    }

    // --- NUEVOS MÉTODOS PARA LOS BOTONES ---

    @FXML
    private void handleNuevoCliente() {
        abrirFormularioCliente(null);
    }

    @FXML
    private void handleEditarCliente() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un cliente para editar.");
            return;
        }
        abrirFormularioCliente(seleccionado);
    }

    @FXML
    private void handleEliminarCliente() {
        Cliente seleccionado = tablaClientes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un cliente para eliminar.");
            return;
        }
        // 1. Diálogo de confirmación que solicitas
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de que desea eliminar al cliente?");
        alert.setContentText(seleccionado.getNombre() + " " + seleccionado.getApellido());

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            // 2. Corrección de la conexión
            String sql = "DELETE FROM tbClientes WHERE idCliente = ?";
            Connection cnx = ConexionDB.obtenerInstancia().getCnx();

            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setInt(1, seleccionado.getIdCliente());
                pst.executeUpdate();
                cargarClientes();
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Eliminación exitosa");
                info.setHeaderText(null);
                info.setContentText("Cliente eliminado exitosamente");
                info.showAndWait();

            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error de BD", "No se pudo eliminar el cliente. Es posible que tenga registros asociados (vehículos, etc.).");
            }
        }
    }

    private void abrirFormularioCliente(Cliente cliente) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("cliente-formulario-view.fxml"));
            Scene scene = new Scene(loader.load());

            // Pasar la referencia de este controlador al del formulario
            ClienteFormularioController formularioController = loader.getController();
            formularioController.setClientesController(this);

            // Si es para editar, pasar los datos
            if (cliente != null) {
                formularioController.setClienteParaEditar(cliente);
            }

            Stage stage = new Stage();
            stage.setTitle(cliente == null ? "Nuevo Cliente" : "Editar Cliente");
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana principal
            stage.setScene(scene);
            stage.showAndWait(); // Muestra y espera a que se cierre

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