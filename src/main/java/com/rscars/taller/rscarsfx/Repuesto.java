package com.rscars.taller.rscarsfx;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class Repuesto {
    private final SimpleIntegerProperty idRepuesto;
    private final SimpleStringProperty descripcion;
    private final SimpleIntegerProperty precio;
    private final SimpleIntegerProperty stock;
    private final SimpleIntegerProperty idProveedor;

    public Repuesto(int idRepuesto, String descripcion, int precio, int stock, int idProveedor) {
        this.idRepuesto = new SimpleIntegerProperty(idRepuesto);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.precio = new SimpleIntegerProperty(precio);
        this.stock = new SimpleIntegerProperty(stock);
        this.idProveedor = new SimpleIntegerProperty(idProveedor);
    }

    public int getIdRepuesto() { return idRepuesto.get(); }
    public String getDescripcion() { return descripcion.get(); }
    public int getPrecio() { return precio.get(); }
    public int getStock() { return stock.get(); }
    public int getIdProveedor() { return idProveedor.get(); }

    public SimpleIntegerProperty idRepuestoProperty() { return idRepuesto; }
    public SimpleStringProperty descripcionProperty() { return descripcion; }
    public SimpleIntegerProperty precioProperty() { return precio; }
    public SimpleIntegerProperty stockProperty() { return stock; }
    public SimpleIntegerProperty idProveedorProperty() { return idProveedor; }
}

