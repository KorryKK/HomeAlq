package korry.homealq.util.database;

import korry.homealq.util.enums.MotorDB;
import org.flywaydb.core.Flyway;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {

    private final static String HOST = System.getenv("DB_HOST");
    private final static String USER = System.getenv("DB_USER");
    private final static String PASSWORD = System.getenv("DB_PASSWORD");
    private final static String DATABASE = System.getenv("DB_NAME");
    private final static String PORT = System.getenv("DB_PORT");
    private final static String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?sslMode=REQUIRED";


    private static ConfiguracionDB configuracion;

    private Database() {}

    // Elige a que base de datos se conecta la app
    public static void configurar(ConfiguracionDB nueva) {
        configuracion = nueva;
    }

    // Si nadie ha configurado nada, se usa la de por defecto
    private static ConfiguracionDB configuracionActual() {
        if (configuracion == null) {
            configuracion = ConfiguracionDB.porDefecto();
        }
        return configuracion;
    }

    // Abre una conexión nueva. Quien la pide tiene que cerrarla (try-with-resources)
    public static Connection conectar() throws SQLException {
        ConfiguracionDB config = configuracionActual();
        crearCarpetaSiHaceFalta(config);
        return DriverManager.getConnection(config.url(), config.usuario(), config.contrasena());
    }

    // Crea o actualiza las tablas aplicando los scripts de db/migration/<motor>.
    public static void inicializar() throws SQLException {
        ConfiguracionDB config = configuracionActual();
        crearCarpetaSiHaceFalta(config);

        Flyway.configure()
                .dataSource(config.url(), config.usuario(), config.contrasena())
                .locations("classpath:/db/migration/" + config.motor().getCarpetaMigraciones())
                .load()
                .migrate();
    }

    // Auxiliar
    // SQLite crea el archivo de la base de datos solo, pero no la carpeta que lo contiene
    private static void crearCarpetaSiHaceFalta(ConfiguracionDB config) throws SQLException {
        if (config.motor() != MotorDB.SQLITE) {
            return;
        }

        Path carpeta = config.archivo().toAbsolutePath().getParent();
        try {
            Files.createDirectories(carpeta);
        } catch (IOException e) {
            throw new SQLException("No se pudo crear la carpeta de la base de datos; " + carpeta, e);
        }
    }
}
