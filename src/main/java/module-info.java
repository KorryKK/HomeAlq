module korry.homealq {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires java.sql;
    requires flyway.core;
    requires java.desktop;

    // OPENS DE FLYWAY
    opens db.migration.sqlite;
    opens db.migration.mysql;

    // OPENS
    opens korry.homealq to javafx.fxml;
    opens korry.homealq.view.main to javafx.fxml;

    // EXPORTS
    exports korry.homealq;
}