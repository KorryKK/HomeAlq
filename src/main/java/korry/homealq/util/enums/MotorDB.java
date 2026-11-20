package korry.homealq.util.enums;

public enum MotorDB {

    SQLITE("sqlite"),
    MYSQL("mysql");

    private final String carpetaMigraciones;

    MotorDB(String carpetaMigraciones) {
        this.carpetaMigraciones = carpetaMigraciones;
    }

    // Carpeta (dentro de db/migration) donde están los scripts de ese motor.
    public String getCarpetaMigraciones() {
        return carpetaMigraciones;
    }
}
