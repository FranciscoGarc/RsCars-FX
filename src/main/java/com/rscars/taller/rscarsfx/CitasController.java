package com.rscars.taller.rscarsfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;

public class CitasController implements Initializable {

    @FXML private TableView<Cita> tablaCitas;
    @FXML private TableColumn<Cita, Integer> colId;
    @FXML private TableColumn<Cita, String> colFecha;
    @FXML private TableColumn<Cita, String> colCliente;
    @FXML private TableColumn<Cita, String> colVehiculo;
    @FXML private TableColumn<Cita, String> colServicio;
    @FXML private TableColumn<Cita, String> colEstado;
    @FXML
    private void handleNuevaCita() {
        abrirFormularioCita(null);
    }
    @FXML
    private void handleEditarCita() {
        Cita seleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Error", "Debe seleccionar una cita para editar.");
            return;
        }
        abrirFormularioCita(seleccionada);
    }

    private ObservableList<Cita> listaCitas;
    private void abrirFormularioCita(Cita cita) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("cita-formulario-view.fxml"));
            Scene scene = new Scene(loader.load());

            CitaFormularioController controller = loader.getController();
            controller.setCitasController(this);

            // Si la cita no es nula (estamos editando), pasamos el objeto
            if (cita != null) {
                controller.setCitaParaEditar(cita);
            }

            Stage stage = new Stage();
            stage.setTitle(cita == null ? "Nueva Cita" : "Editar Cita");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listaCitas = FXCollections.observableArrayList();

        // Asociar columnas con el modelo Cita
        colId.setCellValueFactory(new PropertyValueFactory<>("idCita"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaHora"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colCliente.setCellValueFactory(new PropertyValueFactory<>("cliente"));
        colVehiculo.setCellValueFactory(new PropertyValueFactory<>("vehiculo"));
        colServicio.setCellValueFactory(new PropertyValueFactory<>("servicio"));

        cargarCitas();
    }

    public void cargarCitas() {
        listaCitas.clear();
        // --- MODIFICAMOS LA CONSULTA PARA OBTENER LOS IDs ---
        String sql = "SELECT c.idCita, c.fechaHora, c.estado, c.idVehiculo, c.idServicio, " +
                "cl.idCliente, cl.nombre AS nombreCliente, cl.apellido, " +
                "v.marca, v.modelo, v.placa, " +
                "s.descripcion AS descripcionServicio " +
                "FROM tbCitas c " +
                "LEFT JOIN tbVehiculos v ON c.idVehiculo = v.idVehiculo " +
                "LEFT JOIN tbClientes cl ON v.idCliente = cl.idCliente " +
                "LEFT JOIN tbServicios s ON c.idServicio = s.idServicio";

        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String clienteCompleto = rs.getString("nombreCliente") + " " + rs.getString("apellido");
                String vehiculoCompleto = rs.getString("marca") + " " + rs.getString("modelo") + " (" + rs.getString("placa") + ")";

                // --- PASAMOS LOS IDs AL CONSTRUCTOR ---
                listaCitas.add(new Cita(
                        rs.getInt("idCita"),
                        rs.getString("fechaHora"),
                        rs.getString("estado"),
                        clienteCompleto,
                        vehiculoCompleto,
                        rs.getString("descripcionServicio"),
                        rs.getInt("idCliente"),
                        rs.getInt("idVehiculo"),
                        rs.getInt("idServicio")
                ));
            }
            tablaCitas.setItems(listaCitas);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleCancelarCita() {
        Cita seleccionada = tablaCitas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Error", "Debe seleccionar una cita para cancelar.");
            return;
        }

        // Diálogo de confirmación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Cancelación");
        alert.setHeaderText("¿Está seguro de que desea cancelar esta cita?");
        alert.setContentText("La cita con ID " + seleccionada.getIdCita() + " se marcará como 'Cancelada'.");

        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {

            String sql = "UPDATE tbCitas SET estado = 'Cancelada' WHERE idCita = ?";
            Connection cnx = ConexionDB.obtenerInstancia().getCnx();

            try (PreparedStatement pst = cnx.prepareStatement(sql)) {
                pst.setInt(1, seleccionada.getIdCita());
                pst.executeUpdate();

                // Refrescar la tabla para ver el cambio de estado
                cargarCitas();

            } catch (SQLException e) {
                e.printStackTrace();
                mostrarAlerta("Error de BD", "No se pudo cancelar la cita.");
            }
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