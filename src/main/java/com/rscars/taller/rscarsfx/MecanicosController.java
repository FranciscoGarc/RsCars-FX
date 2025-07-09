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

public class MecanicosController implements Initializable {
    @FXML
    private TableView<Mecanico> tablaMecanicos;
    @FXML
    private TableColumn<Mecanico, Integer> colIdMecanico;
    @FXML
    private TableColumn<Mecanico, Integer> colIdUsuario;
    @FXML
    private TableColumn<Mecanico, String> colNombre;
    @FXML
    private TableColumn<Mecanico, String> colApellido;
    @FXML
    private TableColumn<Mecanico, String> colTelefono;
    @FXML
    private TableColumn<Mecanico, String> colDireccion;
    @FXML
    private TableColumn<Mecanico, String> colDui;
    @FXML
    private Button btnNuevo, btnEditar, btnEliminar;

    private ObservableList<Mecanico> listaMecanicos;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listaMecanicos = FXCollections.observableArrayList();
        colIdMecanico.setCellValueFactory(new PropertyValueFactory<>("idMecanico"));
        colIdUsuario.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colApellido.setCellValueFactory(new PropertyValueFactory<>("apellido"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colDireccion.setCellValueFactory(new PropertyValueFactory<>("direccion"));
        colDui.setCellValueFactory(new PropertyValueFactory<>("dui"));
        cargarMecanicos();
    }

    public void cargarMecanicos() {
        listaMecanicos.clear();
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        String sql = "SELECT * FROM tbMecanicos";
        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                listaMecanicos.add(new Mecanico(
                        rs.getInt("idMecanico"),
                        rs.getInt("idUsuario"),
                        rs.getString("nombre"),
                        rs.getString("apellido"),
                        rs.getString("telefono"),
                        rs.getString("direccion"),
                        rs.getString("dui")
                ));
            }
            tablaMecanicos.setItems(listaMecanicos);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "Error al cargar los mecánicos de la base de datos.");
        }
    }

    @FXML
    private void handleNuevoMecanico() {
        abrirFormularioMecanico(null);
    }

    @FXML
    private void handleEditarMecanico() {
        Mecanico seleccionado = tablaMecanicos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un mecánico para editar.");
            return;
        }
        abrirFormularioMecanico(seleccionado);
    }

    @FXML
    private void handleEliminarMecanico() {
        Mecanico seleccionado = tablaMecanicos.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta("Error", "Debe seleccionar un mecánico para eliminar.");
            return;
        }
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmar Eliminación");
        alert.setHeaderText("¿Está seguro de que desea eliminar el mecánico?");
        alert.setContentText(seleccionado.getNombre() + " " + seleccionado.getApellido());
        Optional<ButtonType> resultado = alert.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            borrarMecanicoYUsuario(seleccionado);
        }
    }

    private void borrarMecanicoYUsuario(Mecanico mecanico) {
        int idMecanico = mecanico.getIdMecanico();
        int idUsuario = 0;

        // Obtener idUsuario asociado
        String sqlGetUsuario = "SELECT idUsuario FROM tbMecanicos WHERE idMecanico = ?";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sqlGetUsuario)) {
            pst.setInt(1, idMecanico);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    idUsuario = rs.getInt("idUsuario");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo obtener el usuario asociado.");
            return;
        }

        try {
            cnx.setAutoCommit(false);

            // Eliminar mecánico
            String sqlDelMecanico = "DELETE FROM tbMecanicos WHERE idMecanico = ?";
            try (PreparedStatement pst = cnx.prepareStatement(sqlDelMecanico)) {
                pst.setInt(1, idMecanico);
                pst.executeUpdate();
            }

            // Eliminar usuario
            String sqlDelUsuario = "DELETE FROM tbUsuarios WHERE idUsuario = ?";
            try (PreparedStatement pst = cnx.prepareStatement(sqlDelUsuario)) {
                pst.setInt(1, idUsuario);
                pst.executeUpdate();
            }

            cnx.commit();
            mostrarAlerta("Éxito", "Mecánico y usuario eliminados correctamente.");
            cargarMecanicos();
        } catch (SQLException e) {
            e.printStackTrace();
            try { if (cnx != null) cnx.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            mostrarAlerta("Error", "No se pudo eliminar el mecánico.");
        } finally {
            try { if (cnx != null) cnx.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    private void abrirFormularioMecanico(Mecanico mecanico) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("mecanico-formulario-view.fxml"));
            Scene scene = new Scene(loader.load());
            MecanicoFormularioController formularioController = loader.getController();
            formularioController.setMecanicosController(this);
            if (mecanico != null) {
                formularioController.setMecanicoParaEditar(mecanico);
            }
            Stage stage = new Stage();
            stage.setTitle(mecanico == null ? "Nuevo Mecánico" : "Editar Mecánico");
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

