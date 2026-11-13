package korry.homealq.util.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {



    private final static String host = System.getenv("DB_HOST");
    private final static String user = System.getenv("DB_USER");
    private final static String password = System.getenv("DB_PASSWORD");
    private final static String database = System.getenv("DB_NAME");
    private final static String port = System.getenv("DB_PORT");
    private final static String URL = "jdbc:mysql://" + host + ":" + port + "/" + database + "?sslMode=REQUIRED";

    public static Connection conectar() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, user, password);
            IO.println("¡Conectado correctamente!");
        } catch (SQLException e) {
            System.err.println("Error al conectar a SQLite DB: " + e.getMessage());
        }
        return conn;
    }

    public static void inicializar() {

    }
}
