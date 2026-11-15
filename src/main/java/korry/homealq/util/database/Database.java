package korry.homealq.util.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static class Esquemas {
        //language=MySQL
        private final String tablaHabitacion = """
                CREATE TABLE IF NOT EXISTS HABITACION (
                    ID INTEGER AUTO_INCREMENT NOT NULL PRIMARY KEY,
                    NUMERO INTEGER NOT NULL UNIQUE
                );""";

        //language=MySQL
        private final String tablaPersona = """
                CREATE TABLE IF NOT EXISTS PERSONA (
                    ID INTEGER AUTO_INCREMENT NOT NULL PRIMARY KEY,
                    NOMBRE VARCHAR(40) NOT NULL,
                    APELLIDOS VARCHAR(60) NOT NULL,
                    DNI VARCHAR(20) NOT NULL UNIQUE,
                    TELEFONO INTEGER NOT NULL
                );""";
    }

    private final static String HOST = System.getenv("DB_HOST");
    private final static String USER = System.getenv("DB_USER");
    private final static String PASSWORD = System.getenv("DB_PASSWORD");
    private final static String DATABASE = System.getenv("DB_NAME");
    private final static String PORT = System.getenv("DB_PORT");
    private final static String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?sslMode=REQUIRED";

    public static Connection conectar() throws SQLException {

        if (HOST == null || USER == null ||
                PASSWORD == null || DATABASE == null) {
            throw new IllegalStateException(
                    "Faltan variables de entorno para MySQL."
            );
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static String getHost() {
        return HOST;
    }

    
    //public static void inicializar() {}
}
