package com.rscars.taller.rscarsfx;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class Servicio {
    private final SimpleIntegerProperty idServicio;
    private final SimpleStringProperty descripcion;
    private final SimpleDoubleProperty costo;

    public Servicio(int idServicio, String descripcion, double costo) {
        this.idServicio = new SimpleIntegerProperty(idServicio);
        this.descripcion = new SimpleStringProperty(descripcion);
        this.costo = new SimpleDoubleProperty(costo);
    }
    @Override
    public String toString() {
        return getDescripcion();
    }
    public int getIdServicio() { return idServicio.get(); }
    public String getDescripcion() { return descripcion.get(); }
    public double getCosto() { return costo.get(); }

    public SimpleIntegerProperty idServicioProperty() { return idServicio; }
    public SimpleStringProperty descripcionProperty() { return descripcion; }
    public SimpleDoubleProperty costoProperty() { return costo; }
}
