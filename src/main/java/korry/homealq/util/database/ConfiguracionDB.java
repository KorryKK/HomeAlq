package korry.homealq.util.database;

import korry.homealq.util.enums.MotorDB;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record ConfiguracionDB(MotorDB motor, Path archivo, String host, int puerto,
                              String baseDatos, String usuario, String contrasena) {

    public ConfiguracionDB {
        Objects.requireNonNull(motor, "El motor de la base de datos no puede ser nulo");
    }

    // --------------- FORMAS DE CREAR LA CONFIGURACIÓN

    // SQLite en el archivo que se indique (los test usan un archivo temporal).
    public static ConfiguracionDB sqlite(Path archivo) {
        return new ConfiguracionDB(MotorDB.SQLITE, archivo, null, 0, null, null, null);
    }

    // SQLite en la carpeta del usuario: %APPDATA%HomeAlq en Windows, ~/.homealq en el resto.
    public static ConfiguracionDB sqlitelocal() {
        return sqlite(carpetaDeLaApp().resolve("homealq.db"));
    }

    // MySQL con los datos de las variables de entorno DB_HOST, DB_PORT, DB_NAME, DB_USER y DB_PASSWORD.
    public static ConfiguracionDB mysqlDesdeEntorno() {
        List<String> faltan = new ArrayList<>();
        String host = leerVariable("DB_HOST", faltan);
        String puerto = leerVariable("DB_PORT", faltan);
        String baseDatos = leerVariable("DB_NAME", faltan);
        String usuario = leerVariable("DB_USER", faltan);
        String contrasena = leerVariable("DB_PASSWORD", faltan);

        if (!faltan.isEmpty()) {
            throw new IllegalStateException("Faltan estas variables de entorno: " + String.join(", ", faltan));
        }

        try {
            return new ConfiguracionDB(MotorDB.MYSQL, null, host, Integer.parseInt(puerto),
                    baseDatos, usuario, contrasena);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("DB_PORT tiene que ser un número y vale: " + puerto, e);
        }
    }

    // La que usa la app ahora: SQLite, salvo que la variable DB_MOTOR valga MYSQL.
    public static ConfiguracionDB porDefecto() {
        String motor = System.getenv("DB_MOTOR");
        if (motor != null && motor.equalsIgnoreCase("mysql")) {
            return mysqlDesdeEntorno();
        }

        return sqlitelocal();
    }

    // URL JDBC. En SQLite activa las claves foráneas, que vienen apagadas por defecto.
    public String url() {
        return switch (motor) {
            case SQLITE -> "jdbc:sqlite:" + archivo.toAbsolutePath() + "?foreign_keys=on";
            case MYSQL -> "jdbc:mysql://" + host + ":" + puerto + "/" + baseDatos + "?sslMode=REQUIRED";
        };
    }

    @Override
    public String toString() {
        return switch (motor) {
            case SQLITE -> "SQLite (" + archivo + ")";
            case MYSQL -> "MySQL (" + usuario + "@" + host + ":" + puerto + "/" + baseDatos + ")";
        };
    }

    // --------------------- AUXILIARES ---------------------
    private static Path carpetaDeLaApp() {
        String appData = System.getenv("APPDATA");

        if (appData != null && !appData.isEmpty()) {
            return Path.of(appData, "HomeAlq");
        }

        return Path.of(System.getProperty("user.home"), "HomeAlq");
    }

    private static String leerVariable(String nombre, List<String> faltan) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isEmpty()) {
            faltan.add(nombre);
        }
        return valor;
    }
}
