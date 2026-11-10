package korry.homealq.util.enums;

public enum Imagen {
    Logo128("/Imagenes/Logo/Logo128.png");

    private final String ruta;

    Imagen(String path){
        ruta = path;
    }

    public String getImg(){
        return ruta;
    }
}
