package korry.homealq.util.enums;

public enum Scene {
    HomeAlqApp("/Vistas/HomeAlqApp.fxml"),;

    private final String fxml;

    Scene(String path) {
        fxml=path;
    }

    public String getFxml() {
        return fxml;
    }
}
