package korry.homealq.util.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private final static String HOST = System.getenv("DB_HOST");
    private final static String USER = System.getenv("DB_USER");
    private final static String PASSWORD = System.getenv("DB_PASSWORD");
    private final static String DATABASE = System.getenv("DB_NAME");
    private final static String PORT = System.getenv("DB_PORT");
    private final static String URL = "jdbc:mysql://" + HOST + ":" + PORT + "/" + DATABASE + "?sslMode=REQUIRED";

    public static Connection conectar() throws SQLException {

        if (HOST == null || USER == null ||
                PASSWORD == null || DATABASE == null || PORT == null) {

            String mensaje = "Faltan las siguientes variables: ";
            if (HOST == null)
                mensaje = mensaje + "\nHost";
            if (USER == null) {
                mensaje = mensaje + "\nUser";
            }
            if (PASSWORD == null) {
                mensaje = mensaje + "\nPassword";
            }
            if (DATABASE == null) {
                mensaje = mensaje + "\nDatabase";
            }
            if (PORT == null) {
                mensaje = mensaje + "\nPort";
            }
            throw new IllegalStateException(mensaje);
        }

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
