package com.rscars.taller.rscarsfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

// 1. Implementa Initializable para usar el método initialize
public class VehiculoFormularioController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private TextField tfMarca, tfModelo, tfAnio, tfPlaca;

    // 2. Cambia el TextField por un ComboBox
    @FXML private ComboBox<Cliente> cbCliente;

    @FXML private Button btnGuardar, btnCancelar;

    private Vehiculo vehiculoParaEditar;
    private VehiculosController vehiculosController;
    private boolean esNuevo = true;

    // 3. Este método se ejecuta automáticamente al cargar la vista
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarClientesEnComboBox();
    }

    public void setVehiculosController(VehiculosController controller) {
        this.vehiculosController = controller;
    }

    // 4. Método para llenar el ComboBox con datos de la BD
    private void cargarClientesEnComboBox() {
        ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
        String sql = "SELECT idCliente, nombre, apellido, telefono, direccion, dui FROM tbClientes";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                listaClientes.add(new Cliente(
                        rs.getInt("idCliente"), rs.getString("nombre"), rs.getString("apellido"),
                        rs.getString("telefono"), rs.getString("direccion"), rs.getString("dui")
                ));
            }
            cbCliente.setItems(listaClientes);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudieron cargar los clientes.");
        }
    }

    // 5. Adaptamos el método para funcionar con el ComboBox
    public void setVehiculoParaEditar(Vehiculo vehiculo) {
        this.vehiculoParaEditar = vehiculo;
        this.esNuevo = false;

        lblTitulo.setText("Editar Vehículo");
        tfMarca.setText(vehiculo.getMarca());
        tfModelo.setText(vehiculo.getModelo());
        tfAnio.setText(String.valueOf(vehiculo.getAnio()));
        tfPlaca.setText(vehiculo.getPlaca());

        // Lógica para seleccionar el cliente correcto en el ComboBox
        for (Cliente cliente : cbCliente.getItems()) {
            if (cliente.getIdCliente() == vehiculo.getIdCliente()) {
                cbCliente.getSelectionModel().select(cliente);
                break;
            }
        }
    }

    @FXML
    void guardarVehiculo() {
        // 6. Adaptamos la validación y la obtención de datos
        if (tfMarca.getText().isEmpty() || cbCliente.getSelectionModel().getSelectedItem() == null) {
            mostrarAlerta("Error de Validación", "La Marca y el Cliente son campos obligatorios.");
            return;
        }

        // Obtenemos el ID del cliente desde el objeto seleccionado en el ComboBox
        int idClienteSeleccionado = cbCliente.getSelectionModel().getSelectedItem().getIdCliente();

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
            pst.setInt(5, idClienteSeleccionado); // Usamos el ID del ComboBox

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