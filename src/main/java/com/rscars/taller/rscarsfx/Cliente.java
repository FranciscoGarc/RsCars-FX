package com.rscars.taller.rscarsfx;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Cliente {
    private final SimpleIntegerProperty idCliente;
    private final SimpleStringProperty nombre;
    private final SimpleStringProperty apellido;
    private final SimpleStringProperty telefono;
    private final SimpleStringProperty direccion;
    private final SimpleStringProperty dui;

    public Cliente(int id, String nombre, String apellido, String telefono, String direccion, String dui) {
        this.idCliente = new SimpleIntegerProperty(id);
        this.nombre = new SimpleStringProperty(nombre);
        this.apellido = new SimpleStringProperty(apellido);
        this.telefono = new SimpleStringProperty(telefono);
        this.direccion = new SimpleStringProperty(direccion);
        this.dui = new SimpleStringProperty(dui);
    }

    // Getters
    public int getIdCliente() { return idCliente.get(); }
    public String getNombre() { return nombre.get(); }
    public String getApellido() { return apellido.get(); }
    public String getTelefono() { return telefono.get(); }
    public String getDireccion() { return direccion.get(); }
    public String getDui() { return dui.get(); }

    // Property Getters
    public SimpleIntegerProperty idClienteProperty() { return idCliente; }
    public SimpleStringProperty nombreProperty() { return nombre; }
    public SimpleStringProperty apellidoProperty() { return apellido; }
    public SimpleStringProperty telefonoProperty() { return telefono; }
    public SimpleStringProperty direccionProperty() { return direccion; }
    public SimpleStringProperty duiProperty() { return dui; }
}