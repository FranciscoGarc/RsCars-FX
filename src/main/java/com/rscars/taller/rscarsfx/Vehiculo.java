package com.rscars.taller.rscarsfx;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Vehiculo {
    private final SimpleIntegerProperty idVehiculo;
    private final SimpleStringProperty marca;
    private final SimpleStringProperty modelo;
    private final SimpleIntegerProperty anio;
    private final SimpleStringProperty placa;
    private final SimpleIntegerProperty idCliente;

    public Vehiculo(int idVehiculo, String marca, String modelo, int anio, String placa, int idCliente) {
        this.idVehiculo = new SimpleIntegerProperty(idVehiculo);
        this.marca = new SimpleStringProperty(marca);
        this.modelo = new SimpleStringProperty(modelo);
        this.anio = new SimpleIntegerProperty(anio);
        this.placa = new SimpleStringProperty(placa);
        this.idCliente = new SimpleIntegerProperty(idCliente);
    }

    // Getters
    public int getIdVehiculo() { return idVehiculo.get(); }
    public String getMarca() { return marca.get(); }
    public String getModelo() { return modelo.get(); }
    public int getAnio() { return anio.get(); }
    public String getPlaca() { return placa.get(); }
    public int getIdCliente() { return idCliente.get(); }


    // Property Getters
    public SimpleIntegerProperty idVehiculoProperty() { return idVehiculo; }
    public SimpleStringProperty marcaProperty() { return marca; }
    public SimpleStringProperty modeloProperty() { return modelo; }
    public SimpleIntegerProperty anioProperty() { return anio; }
    public SimpleStringProperty placaProperty() { return placa; }
    public SimpleIntegerProperty idClienteProperty() { return idCliente; }
}