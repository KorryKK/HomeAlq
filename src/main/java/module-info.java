module korry.homealq {

    // JAVAFX
    requires javafx.controls;
    requires javafx.fxml;

    // LIBRERÍAS
    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    // BASE DE DATOS
    requires java.sql;
    requires flyway.core;
    requires flyway.mysql;

    // JAVA
    //requires java.desktop;

    // OPENS DE FLYWAY
    opens db.migration.sqlite;
    opens db.migration.mysql;

    // OPENS JAVAFX
    opens korry.homealq to javafx.fxml;
    opens korry.homealq.view.main to javafx.fxml;

    // OPENS JUNIT
    opens korry.homealq.calculo to org.junit.platform.commons;

    // EXPORTS
    exports korry.homealq;
}