package korry.homealq.util.navegador;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import korry.homealq.util.enums.Imagen;
import korry.homealq.util.enums.Scene;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

public record Navegador(Stage stage) {

    /**
     * Constructor del navegador
     */
    public Navegador {
        Objects.requireNonNull(stage, "El Stage no puede ser nulo");
    }

    /**
     * Método para cambiar de pantalla.
     *
     * @param fxmlArchivo enumerado de destino (lleva asociada la ruta al FXML)
     * @return - Controlador cargado
     * - null si hubo algún error
     */
    public <T> T goTo(Scene fxmlArchivo) {

        // Obtenemos el recurso para comprobar que existe
        var recurso = Navegador.class.getResource(fxmlArchivo.getFxml());

        // Si es null, es que el nombre está mal escrito o el archivo no está ahí
        if (recurso == null) {
            System.err.println("❌ ERROR: No se encuentra el archivo: " + fxmlArchivo);
            System.err.println("Revisa si las mayúsculas coinciden exactamente.");
            return null; // Salimos para evitar el error de "Location is required"
        }

        return getController(recurso);
    }

    /**
     * Método para cargar la vista pasada por parámetro
     *
     * @param recurso URL del recurso a cargar
     * @return - Controlador cargado
     * - null si hubo algún error
     */
    private <T> T getController(URL recurso) {
        try {
            // 1. Cargamos el nuevo diseño (el archivo .fxml)
            FXMLLoader loader = new FXMLLoader(recurso);
            setControllerFactory(loader);

            Parent root = loader.load();

            // Obtenemos el controlador
            T controller = loader.getController();

            // Comprobamos que existe
            if (controller == null) {
                System.err.println("❌ ERROR: El archivo FXML no tiene un controlador asociado: " + recurso);
                return null;
            }

            // 2. Obtenemos la escena actual de la ventana
            javafx.scene.Scene escenaActual = stage.getScene();

            if (escenaActual == null) {
                // Si por alguna razón no hubiera escena (primera vez), la creamos
                escenaActual = new javafx.scene.Scene(root);
                stage.setScene(escenaActual);
            } else {
                // En lugar de una nueva Scene, cambiamos solo el "Root" (la raíz)
                // Esto mantiene el estado de la ventana (maximizado) intacto.
                escenaActual.setRoot(root);
            }
            return controller;

        } catch (IOException | RuntimeException e) {
            System.err.println("❌ Error al cargar la vista " + recurso);
            e.printStackTrace(System.err);
            return null;
        }
    }

    /**
     * Método para mostrar una vista modal
     *
     * @param fxmlArchivo enumerado de destino (lleva asociada la ruta al FXML)
     * @param title       Título de la ventana modal
     * @return - Controlador cargado
     * - null si hubo algún error
     */
    public <T> T goToModal(Scene fxmlArchivo, String title) {
        URL recurso = Navegador.class.getResource(fxmlArchivo.getFxml());
        if (recurso == null) {
            System.err.println("❌ ERROR: No se encuentra el archivo: " + fxmlArchivo.getFxml());
            System.err.println("Revisa si las mayúsculas coinciden exactamente.");
            return null;
        }
        return getModalController(recurso, title);
    }

    /**
     * Método para cargar la vista pasada por parámetro - como modal
     *
     * @param recurso URL del recurso a cargar
     * @param title    Título de la ventana modal
     * @return - Controlador cargado
     * - null si hubo algún error
     */
    private <T> T getModalController(URL recurso, String title) {
        try {

            FXMLLoader loader = new FXMLLoader(recurso);
            setControllerFactory(loader);

            Parent root = loader.load();

            // Obtenemos el controlador
            T controller = loader.getController();

            // Comprobamos que existe
            if (controller == null) {
                System.err.println("❌ ERROR: El archivo FXML no tiene un controlador asociado: " + recurso);
                return null;
            }

            Stage modal = new Stage();
            modal.initOwner(stage); // El stage principal
            modal.initModality(Modality.APPLICATION_MODAL);

            modal.setTitle(title);
            modal.setScene(new javafx.scene.Scene(root));
            modal.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream(Imagen.Logo128.getImg()))));
            modal.show();

            return controller;
        } catch (IOException | RuntimeException e) {
            System.err.println("❌ Error al cargar la vista " + recurso.getPath() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Método para pasar el navegador al nuevo controlador
     *
     * @param loader Es la ruta de la nueva vista con el controlador
     */
    private void setControllerFactory(FXMLLoader loader) {
        // Definir la manera en la que se van a generar las instancias de un controlador
        loader.setControllerFactory(type -> {
            try {

                // Instancia el typo con su constructor por defecto (vacío)
                Object controller = type.getDeclaredConstructor().newInstance();

                // Si esa instancia implementa la interfaz Navegable
                if (controller instanceof Navegable n) {
                    n.setNavegador(this);
                }
                return controller;
            } catch (Exception e) {
                throw new RuntimeException("Error al fabricar el controlador", e);
            }
        });
    }
}
