module com.rscars.taller.rscarsfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    requires org.controlsfx.controls;
    requires jbcrypt;
    requires java.naming;
    requires jakarta.mail;

    opens com.rscars.taller.rscarsfx to javafx.fxml;
    exports com.rscars.taller.rscarsfx;
}