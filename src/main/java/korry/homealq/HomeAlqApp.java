package korry.homealq;

import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;
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

        navegador = new Navegador(stage);

        HomeAlqAppController controller = navegador.goTo(Scene.HomeAlqApp);
        assert controller != null;
        controller.setTitle(Titulo.HomeAlqApp);
        controller.setPresenter(new HomeAlqAppPresenter(controller));
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(Imagen.Logo128.getImg()))));
        stage.setMaximized(true);
        stage.show();
    }
}
