package com.rscars.taller.rscarsfx;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Contador {
    private final SimpleIntegerProperty idEmpleado;
    private final SimpleIntegerProperty idUsuario;
    private final SimpleStringProperty nombre;
    private final SimpleStringProperty apellido;
    private final SimpleStringProperty telefono;
    private final SimpleStringProperty direccion;
    private final SimpleStringProperty dui;

    public Contador(int idEmpleado, int idUsuario, String nombre, String apellido, String telefono, String direccion, String dui) {
        this.idEmpleado = new SimpleIntegerProperty(idEmpleado);
        this.idUsuario = new SimpleIntegerProperty(idUsuario);
        this.nombre = new SimpleStringProperty(nombre);
        this.apellido = new SimpleStringProperty(apellido);
        this.telefono = new SimpleStringProperty(telefono);
        this.direccion = new SimpleStringProperty(direccion);
        this.dui = new SimpleStringProperty(dui);
    }

    public int getIdEmpleado() { return idEmpleado.get(); }
    public int getIdUsuario() { return idUsuario.get(); }
    public String getNombre() { return nombre.get(); }
    public String getApellido() { return apellido.get(); }
    public String getTelefono() { return telefono.get(); }
    public String getDireccion() { return direccion.get(); }
    public String getDui() { return dui.get(); }

    public SimpleIntegerProperty idEmpleadoProperty() { return idEmpleado; }
    public SimpleIntegerProperty idUsuarioProperty() { return idUsuario; }
    public SimpleStringProperty nombreProperty() { return nombre; }
    public SimpleStringProperty apellidoProperty() { return apellido; }
    public SimpleStringProperty telefonoProperty() { return telefono; }
    public SimpleStringProperty direccionProperty() { return direccion; }
    public SimpleStringProperty duiProperty() { return dui; }
}

