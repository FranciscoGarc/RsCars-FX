package com.rscars.taller.rscarsfx;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.*;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import javafx.scene.control.Label;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.BarChart;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.chart.XYChart;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.ResourceBundle;


public class MainController implements Initializable {

    @FXML private Label lblBienvenida;
    @FXML private PieChart graficoEstadoCitas;
    @FXML private BarChart<String, Number> graficoServicios; // Gráfico extra como ejemplo
    private String nombreUsuarioLogueado;


    @FXML
    private BorderPane mainPanel;

    @FXML
    private MenuItem menuCerrarSesion;

    @FXML
    private MenuItem menuGestionClientes;

    @FXML
    private MenuItem menuGestionVehiculos;

    @FXML
    private MenuItem menuGestionServicios;

    @FXML
    private MenuItem menuAgendarCita;
    @FXML
    private MenuItem menuGestionEmpleados;
    @FXML
    private MenuItem menuGestionMecanicos;
    @FXML
    private MenuItem menuGestionContadores;

    @FXML
    private MenuItem menuVerDashboard;


    // Aquí agregaremos los métodos para manejar los clics del menú

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        cargarDatosGraficoCitas();
        cargarDatosGraficoServicios(); // Carga el gráfico de ejemplo
    }

    /**
     * Este método ahora recibe el nombre del usuario y lo guarda.
     */
    public void inicializarConUsuario(int idTipoUsuario, String nombreUsuario) {
        // Guardamos el nombre para usarlo después
        this.nombreUsuarioLogueado = nombreUsuario;

        // AQUÍ es donde debemos configurar el mensaje de bienvenida
        configurarMensajeBienvenida();

        // La lógica de permisos no cambia
        if (idTipoUsuario == 2) { // Es Contador
            menuGestionMecanicos.setVisible(false);
            menuGestionContadores.setVisible(false);
            menuGestionEmpleados.setVisible(false);
        }
    }


    // --- MÉTODO ACTUALIZADO ---
    private void configurarMensajeBienvenida() {
        LocalTime ahora = LocalTime.now();
        String saludo;
        if (ahora.isBefore(LocalTime.of(12, 0))) {
            saludo = "¡Buenos días";
        } else if (ahora.isBefore(LocalTime.of(19, 0))) {
            saludo = "¡Buenas tardes";
        } else {
            saludo = "¡Buenas noches";
        }

        // Usamos el nombre guardado para personalizar el mensaje
        String nombreFinal = (nombreUsuarioLogueado != null && !nombreUsuarioLogueado.isBlank()) ? nombreUsuarioLogueado.trim() : "";
        lblBienvenida.setText(saludo + ", " + nombreFinal + "!");
    }

    private void cargarDatosGraficoCitas() {
        String sql = "SELECT estado, COUNT(*) as total FROM tbCitas GROUP BY estado";
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String estado = rs.getString("estado").trim();
                int total = rs.getInt("total");
                pieChartData.add(new PieChart.Data(estado + " (" + total + ")", total));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        graficoEstadoCitas.setData(pieChartData);
    }

    private void cargarDatosGraficoServicios() {
        String sql = "SELECT S.descripcion, COUNT(C.idCita) AS total " +
                "FROM tbCitas C " +
                "JOIN tbServicios S ON C.idServicio = S.idServicio " +
                "GROUP BY S.descripcion " +
                "ORDER BY total DESC";

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Citas por Servicio");
        Connection cnx = ConexionDB.obtenerInstancia().getCnx();

        try (Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String servicio = rs.getString("descripcion").trim();
                int total = rs.getInt("total");
                series.getData().add(new XYChart.Data<>(servicio, total));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        graficoServicios.getData().add(series);
    }

    @FXML
    void onGestionClientesClick() {
        System.out.println("Cargando gestión de clientes...");
        cargarVista("clientes-view.fxml");
    }
    @FXML
    void onGestionVehiculosClick() {
        System.out.println("Cargando gestión de vehículos...");
        cargarVista("vehiculos-view.fxml");
    }
    @FXML
    void onGestionServiciosClick() {
        System.out.println("Cargando gestión de servicios...");
        cargarVista("servicios-view.fxml");
    }
    @FXML
    void onGestionMecanicosClick() {
        System.out.println("Cargando gestión de mecánicos...");
        cargarVista("mecanicos-view.fxml");
    }
    @FXML
    void onGestionContadoresClick() {
        System.out.println("Cargando gestión de contadores...");
        cargarVista("contadores-view.fxml");
    }
    @FXML
    void onGestionProveedoresClick() {
        System.out.println("Cargando gestión de proveedores...");
        cargarVista("proveedores-view.fxml");
    }
    @FXML
    void onGestionRepuestosClick() {
        System.out.println("Cargando gestión de repuestos...");
        cargarVista("repuestos-view.fxml");
    }

    @FXML
    void onAgendarCitaClick() {
        System.out.println("Cargando gestión de citas...");
        cargarVista("citas-view.fxml");
    }

    @FXML
    void onVerDashboardClick() {
        System.out.println("Cargando dashboard principal...");
        // Cargar la vista principal con los gráficos
        cargarVistaDashboard();
    }

    @FXML
    void onReporteCitasClick() {
        ReporteService.generarReporteCitas();
    }
    @FXML
    void onCerrarSesionClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("login-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load());

            // Crear una nueva ventana (Stage) para la pantalla de login
            Stage loginStage = new Stage();
            loginStage.setTitle("RsCars Taller - Inicio de Sesión");
            loginStage.setScene(scene);
            loginStage.show();

            // Obtener la ventana actual (el panel principal) a través de cualquier
            // componente que esté en ella, como el 'mainPanel'
            Stage mainStage = (Stage) mainPanel.getScene().getWindow();
            mainStage.close();

        } catch (IOException e) {
            // Imprimir el error en caso de que no se pueda cargar el FXML
            e.printStackTrace();
        }
    }
    private void cargarVista(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(HelloApplication.class.getResource(fxmlFile));
            Pane view = loader.load();
            mainPanel.setCenter(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void cargarVistaDashboard() {
        // Recreamos la vista del dashboard con los gráficos
        VBox dashboardView = new VBox(20);
        dashboardView.setStyle("-fx-padding: 20;");

        // Añadir mensaje de bienvenida
        configurarMensajeBienvenida();
        dashboardView.getChildren().add(lblBienvenida);

        // Crear el GridPane para los gráficos
        GridPane gridGraficos = new GridPane();
        gridGraficos.setHgap(20);
        gridGraficos.setVgap(20);

        // Configurar columnas
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        gridGraficos.getColumnConstraints().addAll(col1, col2);

        // Añadir los gráficos al grid
        // Limpiar datos anteriores
        graficoEstadoCitas.setData(FXCollections.observableArrayList());
        graficoServicios.getData().clear();

        // Cargar nuevos datos
        cargarDatosGraficoCitas();
        cargarDatosGraficoServicios();

        gridGraficos.add(graficoEstadoCitas, 0, 0);
        gridGraficos.add(graficoServicios, 1, 0);

        // Añadir el grid al dashboard
        dashboardView.getChildren().add(gridGraficos);

        // Mostrar en el panel principal
        mainPanel.setCenter(dashboardView);
    }
}