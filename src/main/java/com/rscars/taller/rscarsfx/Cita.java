package com.rscars.taller.rscarsfx;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Cita {
    private final SimpleIntegerProperty idCita;
    private final SimpleStringProperty fechaHora;
    private final SimpleStringProperty estado;

    // Representación en texto para la tabla
    private final SimpleStringProperty cliente;
    private final SimpleStringProperty vehiculo;
    private final SimpleStringProperty servicio;

    // IDs para la lógica de edición
    private final int idCliente;
    private final int idVehiculo;
    private final int idServicio;

    public Cita(int idCita, String fechaHora, String estado, String cliente, String vehiculo, String servicio, int idCliente, int idVehiculo, int idServicio) {
        this.idCita = new SimpleIntegerProperty(idCita);
        this.fechaHora = new SimpleStringProperty(fechaHora);
        this.estado = new SimpleStringProperty(estado);
        this.cliente = new SimpleStringProperty(cliente);
        this.vehiculo = new SimpleStringProperty(vehiculo);
        this.servicio = new SimpleStringProperty(servicio);

        // Guardamos los IDs
        this.idCliente = idCliente;
        this.idVehiculo = idVehiculo;
        this.idServicio = idServicio;
    }

    // Getters para las propiedades de la tabla (no necesitan cambios)
    public int getIdCita() { return idCita.get(); }
    public String getFechaHora() { return fechaHora.get(); }
    public String getEstado() { return estado.get(); }
    public String getCliente() { return cliente.get(); }
    public String getVehiculo() { return vehiculo.get(); }
    public String getServicio() { return servicio.get(); }

    // Getters para los IDs (NUEVO)
    public int getIdCliente() { return idCliente; }
    public int getIdVehiculo() { return idVehiculo; }
    public int getIdServicio() { return idServicio; }
}