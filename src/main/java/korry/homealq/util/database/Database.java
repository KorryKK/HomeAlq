package korry.homealq.util.database;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private final static String urlDB = "jdbc:sqlite:" +
            System.getProperty("user.home") + File.separator +
            "Pymeris" +   File.separator +
            "database_protecto.db";

    public static Connection conectar() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(urlDB);
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
        } catch (SQLException e) {
            System.out.println("Error al conectar a SQLite DB: " + e.getMessage());
        }
        return conn;
    }
}
