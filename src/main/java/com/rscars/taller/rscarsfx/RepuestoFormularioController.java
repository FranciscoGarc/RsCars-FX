package com.rscars.taller.rscarsfx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

public class RepuestoFormularioController implements Initializable {
    @FXML private Label lblTitulo;
    @FXML private TextField tfDescripcion, tfPrecio, tfStock;
    @FXML private ComboBox<Proveedor> cbProveedor;
    @FXML private Button btnGuardar, btnCancelar;

    private Repuesto repuestoParaEditar;
    private RepuestosController repuestosController;
    private boolean esNuevo = true;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarProveedoresEnComboBox();
    }

    public void setRepuestosController(RepuestosController controller) {
        this.repuestosController = controller;
    }

    private void cargarProveedoresEnComboBox() {
        ObservableList<Proveedor> listaProveedores = FXCollections.observableArrayList();
        String sql = "SELECT idProveedor, nombre, telefono, correo FROM tbProveedores";
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (Statement stmt = cnx.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                listaProveedores.add(new Proveedor(
                        rs.getInt("idProveedor"),
                        rs.getString("nombre"),
                        rs.getString("telefono"),
                        rs.getString("correo")
                ));
            }
            cbProveedor.setItems(listaProveedores);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudieron cargar los proveedores.");
        }
    }

    public void setRepuestoParaEditar(Repuesto repuesto) {
        this.repuestoParaEditar = repuesto;
        this.esNuevo = false;
        lblTitulo.setText("Editar Repuesto");
        tfDescripcion.setText(repuesto.getDescripcion());
        tfPrecio.setText(String.valueOf(repuesto.getPrecio()));
        tfStock.setText(String.valueOf(repuesto.getStock()));
        for (Proveedor proveedor : cbProveedor.getItems()) {
            if (proveedor.getIdProveedor() == repuesto.getIdProveedor()) {
                cbProveedor.getSelectionModel().select(proveedor);
                break;
            }
        }
    }

    @FXML
    void guardarRepuesto() {
        String descripcion = tfDescripcion.getText().trim();
        String precioStr = tfPrecio.getText().trim();
        String stockStr = tfStock.getText().trim();

        if (!ValidationUtil.isNotEmpty(descripcion) || !ValidationUtil.isNotEmpty(precioStr) ||
            !ValidationUtil.isNotEmpty(stockStr) || cbProveedor.getSelectionModel().getSelectedItem() == null) {
            mostrarAlerta("Error de Validación", "Todos los campos son obligatorios.");
            return;
        }

        if (!ValidationUtil.isDecimal(precioStr)) {
            mostrarAlerta("Error de Validación", "El precio debe ser un número válido (por ejemplo, 25.99).");
            return;
        }

        if (!ValidationUtil.isNumbersOnly(stockStr)) {
            mostrarAlerta("Error de Validación", "El stock debe ser un número entero.");
            return;
        }

        int idProveedorSeleccionado = cbProveedor.getSelectionModel().getSelectedItem().getIdProveedor();
        String sql;
        if (esNuevo) {
            sql = "INSERT INTO tbRepuestos (descripcion, precio, stock, idProveedor) VALUES (?, ?, ?, ?)";
        } else {
            sql = "UPDATE tbRepuestos SET descripcion = ?, precio = ?, stock = ?, idProveedor = ? WHERE idRepuesto = ?";
        }
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();
        try (PreparedStatement pst = cnx.prepareStatement(sql)) {
            pst.setString(1, descripcion);
            pst.setDouble(2, Double.parseDouble(precioStr));
            pst.setInt(3, Integer.parseInt(stockStr));
            pst.setInt(4, idProveedorSeleccionado);
            if (!esNuevo) {
                pst.setInt(5, repuestoParaEditar.getIdRepuesto());
            }
            pst.executeUpdate();
            mostrarAlerta("Éxito", "Repuesto guardado correctamente.");
            repuestosController.cargarRepuestos();
            cancelar();
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            mostrarAlerta("Error de BD", "No se pudo guardar el repuesto. Verifique los datos.");
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

