module korry.homealq {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires eu.hansolo.tilesfx;

    // OPENS
    opens korry.homealq to javafx.fxml;
    opens korry.homealq.view.Main to javafx.fxml;

    // EXPORTS
    exports korry.homealq;
}