package com.rscars.taller.rscarsfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class CitaFormularioController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private ComboBox<Vehiculo> cbVehiculo;
    @FXML private ComboBox<Servicio> cbServicio;
    @FXML private DatePicker dpFecha;
    @FXML private TextField tfEstado;
    @FXML private Button btnGuardar;

    private CitasController citasController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarClientes();
        cargarServicios();
    }

    public void setCitasController(CitasController controller) {
        this.citasController = controller;
    }

    private void cargarClientes() {
        ObservableList<Cliente> listaClientes = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tbClientes";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                listaClientes.add(new Cliente(rs.getInt("idCliente"), rs.getString("nombre"), rs.getString("apellido"), rs.getString("telefono"), rs.getString("direccion"), rs.getString("dui")));
            }
            cbCliente.setItems(listaClientes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cargarServicios() {
        ObservableList<Servicio> listaServicios = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tbServicios";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                listaServicios.add(new Servicio(rs.getInt("idServicio"), rs.getString("descripcion"), rs.getDouble("costo")));
            }
            cbServicio.setItems(listaServicios);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void onClienteSeleccionado() {
        Cliente clienteSeleccionado = cbCliente.getSelectionModel().getSelectedItem();
        if (clienteSeleccionado != null) {
            cargarVehiculosDeCliente(clienteSeleccionado.getIdCliente());
        }
    }

    private void cargarVehiculosDeCliente(int idCliente) {
        ObservableList<Vehiculo> listaVehiculos = FXCollections.observableArrayList();
        String sql = "SELECT * FROM tbVehiculos WHERE idCliente = ?";

        // --- CORRECCIÓN AQUÍ ---
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setInt(1, idCliente);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    listaVehiculos.add(new Vehiculo(rs.getInt("idVehiculo"), rs.getString("marca"), rs.getString("modelo"), rs.getInt("año"), rs.getString("placa"), rs.getInt("idCliente")));
                }
                cbVehiculo.setItems(listaVehiculos);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void guardarCita() {
        if (cbCliente.getValue() == null || cbVehiculo.getValue() == null || cbServicio.getValue() == null || dpFecha.getValue() == null) {
            mostrarAlerta("Error", "Todos los campos son obligatorios.");
            return;
        }

        String sql = "INSERT INTO tbCitas (fechaHora, idVehiculo, idServicio, estado) VALUES (?, ?, ?, ?)";

        // --- CORRECCIÓN AQUÍ ---
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setDate(1, Date.valueOf(dpFecha.getValue()));
            pst.setInt(2, cbVehiculo.getValue().getIdVehiculo());
            pst.setInt(3, cbServicio.getValue().getIdServicio());
            pst.setString(4, tfEstado.getText());

            pst.executeUpdate();

            citasController.cargarCitas();
            cancelar();

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo guardar la cita.");
        }
    }

    @FXML
    void cancelar() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
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