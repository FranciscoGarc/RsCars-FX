package com.rscars.taller.rscarsfx;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Proveedor {
    private final SimpleIntegerProperty idProveedor;
    private final SimpleStringProperty nombre;
    private final SimpleStringProperty telefono;
    private final SimpleStringProperty correo;

    public Proveedor(int idProveedor, String nombre, String telefono, String correo) {
        this.idProveedor = new SimpleIntegerProperty(idProveedor);
        this.nombre = new SimpleStringProperty(nombre);
        this.telefono = new SimpleStringProperty(telefono);
        this.correo = new SimpleStringProperty(correo);
    }

    @Override
    public String toString() {
        return getNombre();
    }

    public int getIdProveedor() { return idProveedor.get(); }
    public String getNombre() { return nombre.get(); }
    public String getTelefono() { return telefono.get(); }
    public String getCorreo() { return correo.get(); }

    public SimpleIntegerProperty idProveedorProperty() { return idProveedor; }
    public SimpleStringProperty nombreProperty() { return nombre; }
    public SimpleStringProperty telefonoProperty() { return telefono; }
    public SimpleStringProperty correoProperty() { return correo; }
}

