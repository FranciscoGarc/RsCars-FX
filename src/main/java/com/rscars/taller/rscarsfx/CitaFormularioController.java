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
import java.time.format.DateTimeFormatter;

public class CitaFormularioController implements Initializable {

    @FXML private Label lblTitulo;
    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private ComboBox<Vehiculo> cbVehiculo;
    @FXML private ComboBox<Servicio> cbServicio;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cbEstado;
    @FXML private Button btnGuardar;

    private CitasController citasController;
    private Cita citaParaEditar; // Variable para guardar la cita a editar
    private boolean esNuevo = true;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarClientes();
        cargarServicios();
        // Poblar el ComboBox de estados
        ObservableList<String> estados = FXCollections.observableArrayList("Pendiente", "En Proceso", "Completada", "Cancelada");
        cbEstado.setItems(estados);
        // Opcional: seleccionar un valor por defecto
        cbEstado.setValue("Pendiente");
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

    public void setCitaParaEditar(Cita cita) {
        this.citaParaEditar = cita;
        this.esNuevo = false;
        lblTitulo.setText("Editar Cita");
        cbEstado.setValue(cita.getEstado().trim());

        // Convertir y establecer la fecha
        if (cita.getFechaHora() != null && !cita.getFechaHora().isEmpty()) {
            String fechaParte = cita.getFechaHora().split(" ")[0];
            dpFecha.setValue(LocalDate.parse(fechaParte));
        }

        // --- LÓGICA ROBUSTA PARA SELECCIONAR COMBOBOXES ---

        // 1. Seleccionar Cliente
        for (Cliente cliente : cbCliente.getItems()) {
            if (cliente.getIdCliente() == cita.getIdCliente()) {
                cbCliente.getSelectionModel().select(cliente);
                break;
            }
        }

        // 2. Seleccionar Servicio
        for (Servicio servicio : cbServicio.getItems()) {
            // Asegúrate de que tu modelo Servicio tenga el método getIdServicio()
            if (servicio.getIdServicio() == cita.getIdServicio()) {
                cbServicio.getSelectionModel().select(servicio);
                break;
            }
        }

        // 3. Cargar vehículos del cliente y LUEGO seleccionar el correcto
        // Esto es crucial porque la lista de vehículos depende del cliente.
        if (cbCliente.getValue() != null) {
            // Cargamos la lista de vehículos para el cliente seleccionado
            cargarVehiculosDeCliente(cbCliente.getValue().getIdCliente());

            // Ahora que la lista está llena, buscamos y seleccionamos el vehículo
            for (Vehiculo vehiculo : cbVehiculo.getItems()) {
                // Asegúrate de que tu modelo Vehiculo tenga el método getIdVehiculo()
                if (vehiculo.getIdVehiculo() == cita.getIdVehiculo()) {
                    cbVehiculo.getSelectionModel().select(vehiculo);
                    break;
                }
            }
        }
    }

    @FXML
    void guardarCita() {
        if (cbCliente.getValue() == null || cbVehiculo.getValue() == null || cbServicio.getValue() == null || dpFecha.getValue() == null) {
            mostrarAlerta("Error", "Todos los campos son obligatorios.");
            return;
        }

        String sql;
        if (esNuevo) {
            sql = "INSERT INTO tbCitas (fechaHora, idVehiculo, idServicio, estado) VALUES (?, ?, ?, ?)";
        } else {
            sql = "UPDATE tbCitas SET fechaHora = ?, idVehiculo = ?, idServicio = ?, estado = ? WHERE idCita = ?";
        }

        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {

            pst.setDate(1, Date.valueOf(dpFecha.getValue()));
            pst.setInt(2, cbVehiculo.getValue().getIdVehiculo());
            pst.setInt(3, cbServicio.getValue().getIdServicio());
            pst.setString(4, cbEstado.getValue());

            if (!esNuevo) {
                pst.setInt(5, citaParaEditar.getIdCita()); // Añadimos el ID para el WHERE
            }

            pst.executeUpdate();

            mostrarAlerta("Éxito", "Cita guardada correctamente.");
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