package com.rscars.taller.rscarsfx;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Cita {
    private final SimpleIntegerProperty idCita;
    private final SimpleStringProperty fechaHora;
    private final SimpleStringProperty estado;
    private final SimpleStringProperty cliente; // Guardará "Nombre Apellido"
    private final SimpleStringProperty vehiculo; // Guardará "Marca Modelo (Placa)"
    private final SimpleStringProperty servicio; // Guardará la descripción del servicio

    public Cita(int idCita, String fechaHora, String estado, String cliente, String vehiculo, String servicio) {
        this.idCita = new SimpleIntegerProperty(idCita);
        this.fechaHora = new SimpleStringProperty(fechaHora);
        this.estado = new SimpleStringProperty(estado);
        this.cliente = new SimpleStringProperty(cliente);
        this.vehiculo = new SimpleStringProperty(vehiculo);
        this.servicio = new SimpleStringProperty(servicio);
    }

    // Getters
    public int getIdCita() { return idCita.get(); }
    public String getFechaHora() { return fechaHora.get(); }
    public String getEstado() { return estado.get(); }
    public String getCliente() { return cliente.get(); }
    public String getVehiculo() { return vehiculo.get(); }
    public String getServicio() { return servicio.get(); }
}