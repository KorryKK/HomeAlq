package korry.homealq;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import korry.homealq.util.database.Database;
import korry.homealq.util.enums.Imagen;
import korry.homealq.util.enums.Scene;
import korry.homealq.util.enums.Titulo;
import korry.homealq.util.navegator.Navegador;
import korry.homealq.view.Main.HomeAlqAppController;
import korry.homealq.view.Main.HomeAlqAppPresenter;

import java.util.Objects;

public class HomeAlqApp extends Application{

    Navegador navegador;

    @Override
    public void start(Stage stage){

        // Antes de abrir ninguna pantalla: creamos o actualizamos las tablas
        try {
            Database.inicializar();
        } catch (Exception e) {
            mostrarErrorYSalir(e);
            return;
        }

        navegador = new Navegador(stage);

        HomeAlqAppController controller = navegador.goTo(Scene.HomeAlqApp);
        if (controller != null) {
            controller.setTitle(Titulo.HomeAlqApp);
            controller.setPresenter(new HomeAlqAppPresenter(controller));
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(Imagen.Logo128.getImg()))));
            stage.setMaximized(true);
            stage.show();
        }
    }

    private void mostrarErrorYSalir(Exception e) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("HomeAlq");
        alerta.setHeaderText("No se pudo preparar la base de datos");
        alerta.setContentText(e.getMessage());
        alerta.showAndWait();
        Platform.exit();
    }
}
