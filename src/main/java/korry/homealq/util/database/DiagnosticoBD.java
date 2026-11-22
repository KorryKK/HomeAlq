package korry.homealq.util.database;

import korry.homealq.util.enums.MotorDB;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Herramienta para comprobar el estado de las bases de datos (SQLite y MySQL).
 * Solo lee: no crea ni modifica nada, ni siquiera el archivo de SQLite si no existe.
 * <p>
 * Uso (ejecutar el main):
 * <ul>
 *   <li>sin argumentos: revisa SQLite y MySQL;</li>
 *   <li>"sqlite" o "mysql": revisa solo esa;</li>
 *   <li>"sqlite C:\ruta\archivo.db": revisa otro archivo SQLite (por ejemplo, una copia).</li>
 * </ul>
 * MySQL se configura con las variables de entorno DB_HOST, DB_PORT, DB_NAME, DB_USER y DB_PASSWORD.
 */
public final class DiagnosticoBD {

    private static final List<String> TABLAS_ESPERADAS = List.of(
            "habitacion", "persona", "contrato", "contrato_persona", "documento_contrato",
            "factura", "reparto", "cobro_alquiler", "configuracion", "usuario");

    /** Fecha "sin fin" para comparar los contratos que siguen abiertos. */
    private static final String SIN_FIN = "'9999-12-31'";

    private DiagnosticoBD() {
    }

    /** Lo que sale de revisar una base de datos: el texto y cuántos avisos y fallos hubo. */
    public record Resultado(String texto, int avisos, int fallos) {
    }

    // ---------- Punto de entrada ----------

    static void main(String[] args) {
        String cual = args.length > 0 ? args[0].toLowerCase(Locale.ROOT) : "todas";
        List<Resultado> resultados = new ArrayList<>();

        if (cual.equals("todas") || cual.equals("sqlite")) {
            ConfiguracionDB config = args.length > 1
                    ? ConfiguracionDB.sqlite(Path.of(args[1]))
                    : ConfiguracionDB.sqlitelocal();
            resultados.add(informe(config));
        }
        if (cual.equals("todas") || cual.equals("mysql")) {
            resultados.add(informeMysql());
        }
        if (resultados.isEmpty()) {
            System.out.println("Uso: DiagnosticoBD [sqlite [ruta del archivo] | mysql]");
            return;
        }

        int avisos = 0;
        int fallos = 0;
        for (Resultado resultado : resultados) {
            System.out.println(resultado.texto());
            avisos += resultado.avisos();
            fallos += resultado.fallos();
        }
        System.out.println("RESUMEN: " + fallos + " fallos y " + avisos + " avisos");
        if (fallos > 0) {
            System.exit(1);
        }
    }

    // ---------- Informes ----------

    /** MySQL con las variables de entorno; si faltan, lo dice como aviso (no es un fallo). */
    public static Resultado informeMysql() {
        try {
            return informe(ConfiguracionDB.mysqlDesdeEntorno());
        } catch (IllegalStateException e) {
            Informe informe = new Informe("MySQL");
            informe.aviso("MySQL no está configurado: " + e.getMessage());
            return informe.resultado();
        }
    }

    /** Revisa la base de datos de esa configuración. */
    public static Resultado informe(ConfiguracionDB config) {
        Informe informe = new Informe(config.toString());   // sin la contraseña
        String url = config.url();

        if (config.motor() == MotorDB.SQLITE) {
            // SQLite crea el archivo al conectar: se comprueba antes para no crear uno vacío
            Path archivo = config.archivo().toAbsolutePath();
            if (!Files.exists(archivo)) {
                informe.aviso("El archivo no existe todavía: se crea cuando arranca la app por primera vez");
                return informe.resultado();
            }
            try {
                long bytes = Files.size(archivo);
                if (bytes == 0) {
                    informe.aviso("El archivo está vacío (0 bytes)");
                } else {
                    informe.ok("Archivo encontrado (" + Math.max(1, bytes / 1024) + " KB)");
                }
            } catch (IOException e) {
                informe.fallo("No se pudo leer el archivo: " + e.getMessage());
                return informe.resultado();
            }
            url = url + "&open_mode=1";   // solo lectura
        }

        DriverManager.setLoginTimeout(10);
        long inicio = System.nanoTime();
        try (Connection conexion = DriverManager.getConnection(url, config.usuario(), config.contrasena())) {
            informe.ok("Conexión abierta en " + (System.nanoTime() - inicio) / 1_000_000 + " ms");
            inspeccionar(config.motor(), conexion, informe);
        } catch (SQLException e) {
            String pista = String.valueOf(e.getMessage()).contains("No suitable driver")
                    ? " (¿falta el driver en el pom o en el module-info?)" : "";
            informe.fallo("No se pudo conectar: " + e.getMessage() + pista);
        }
        return informe.resultado();
    }

    /** Todas las comprobaciones sobre una conexión ya abierta. */
    static void inspeccionar(MotorDB motor, Connection conexion, Informe informe) throws SQLException {
        if (motor == MotorDB.MYSQL) {
            conexion.setReadOnly(true);
        }

        DatabaseMetaData datos = conexion.getMetaData();
        informe.info(datos.getDatabaseProductName() + " " + datos.getDatabaseProductVersion()
                + " · driver " + datos.getDriverName() + " " + datos.getDriverVersion());

        if (motor == MotorDB.SQLITE) {
            paso(informe, "SQLite", () -> comprobarSqlite(conexion, informe));
        } else {
            paso(informe, "MySQL", () -> comprobarMysql(conexion, informe));
        }

        Set<String> tablas = new TreeSet<>();
        try {
            tablas.addAll(nombresDeTablas(motor, conexion));
        } catch (SQLException e) {
            informe.fallo("No se pudieron leer las tablas: " + e.getMessage());
            return;
        }

        paso(informe, "Flyway", () -> comprobarFlyway(conexion, tablas, informe));
        paso(informe, "Tablas", () -> comprobarTablas(tablas, informe));
        paso(informe, "Filas", () -> contarFilas(conexion, tablas, informe));
        paso(informe, "Datos", () -> comprobarDatos(conexion, tablas, informe));
    }

    // ---------- Comprobaciones de cada motor ----------

    private static void comprobarSqlite(Connection c, Informe informe) throws SQLException {
        if (contar(c, "PRAGMA foreign_keys") == 1) {
            informe.ok("Claves foráneas activadas");
        } else {
            informe.fallo("Las claves foráneas están APAGADAS: la base de datos no vigilaría las relaciones entre tablas");
        }

        String integridad = texto(c, "PRAGMA integrity_check");
        if ("ok".equals(integridad)) {
            informe.ok("Integridad del archivo: ok");
        } else {
            informe.fallo("Integridad del archivo: " + integridad);
        }

        List<String> violaciones = filas(c, "PRAGMA foreign_key_check");
        if (violaciones.isEmpty()) {
            informe.ok("Ninguna fila rompe las claves foráneas");
        } else {
            informe.fallo("Filas que rompen claves foráneas (tabla / fila / tabla padre / clave): "
                    + String.join("; ", violaciones));
        }
    }

    private static void comprobarMysql(Connection c, Informe informe) throws SQLException {
        informe.info("Base de datos y usuario: " + filas(c, "SELECT DATABASE(), CURRENT_USER()").get(0));

        String cifrado = texto(c, "SHOW SESSION STATUS LIKE 'Ssl_cipher'", 2);
        if (cifrado == null || cifrado.isBlank()) {
            informe.aviso("La conexión NO está cifrada (SSL)");
        } else {
            informe.ok("Conexión cifrada (" + cifrado + ")");
        }

        String juego = texto(c, "SELECT @@character_set_database");
        if (juego != null && juego.startsWith("utf8mb4")) {
            informe.ok("Juego de caracteres de la base de datos: " + juego);
        } else {
            informe.aviso("Juego de caracteres " + juego + ": los acentos y la ñ podrían guardarse mal (mejor utf8mb4)");
        }
    }

    // ---------- Flyway, tablas y filas ----------

    private static void comprobarFlyway(Connection c, Set<String> tablas, Informe informe) throws SQLException {
        if (!tablas.contains("flyway_schema_history")) {
            informe.aviso("No existe flyway_schema_history: esta base de datos nunca se ha migrado");
            return;
        }

        List<String> detalles = new ArrayList<>();
        boolean fallo = false;
        String sql = "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank";
        try (Statement sentencia = c.createStatement(); ResultSet r = sentencia.executeQuery(sql)) {
            while (r.next()) {
                String version = r.getString(1);
                boolean bien = r.getBoolean(3);
                fallo |= !bien;
                detalles.add((version == null ? "R" : "V" + version) + "  " + r.getString(2)
                        + (bien ? "" : "   <-- FALLÓ"));
            }
        }

        if (fallo) {
            informe.fallo("Flyway: hay migraciones que fallaron (success = 0)");
        } else if (detalles.isEmpty()) {
            informe.aviso("Flyway: la tabla de historial está vacía");
        } else {
            informe.ok("Flyway: " + detalles.size()
                    + (detalles.size() == 1 ? " migración aplicada" : " migraciones aplicadas") + " sin errores");
        }
        detalles.forEach(informe::detalle);
    }

    private static void comprobarTablas(Set<String> tablas, Informe informe) {
        List<String> faltan = new ArrayList<>(TABLAS_ESPERADAS);
        faltan.removeAll(tablas);

        List<String> sobran = new ArrayList<>(tablas);
        sobran.removeAll(TABLAS_ESPERADAS);
        sobran.remove("flyway_schema_history");

        if (faltan.isEmpty()) {
            informe.ok("Están las " + TABLAS_ESPERADAS.size() + " tablas esperadas");
        } else if (faltan.size() == TABLAS_ESPERADAS.size()) {
            informe.fallo("No hay ninguna tabla de la app: la base de datos está vacía (la app la prepara al arrancar)");
        } else {
            informe.fallo("Faltan tablas: " + String.join(", ", faltan));
        }

        if (!sobran.isEmpty()) {
            informe.aviso("Tablas que la app no conoce: " + String.join(", ", sobran));
            if (sobran.stream().anyMatch(t -> faltan.contains(t.toLowerCase(Locale.ROOT)))) {
                informe.aviso("Alguna parece una tabla esperada escrita con otras mayúsculas: "
                        + "MySQL en Linux distingue mayúsculas y minúsculas en los nombres");
            }
        }
    }

    private static void contarFilas(Connection c, Set<String> tablas, Informe informe) throws SQLException {
        informe.info("Filas por tabla:");
        for (String tabla : TABLAS_ESPERADAS) {
            if (tablas.contains(tabla)) {
                informe.detalle(String.format("%-20s %d", tabla, contar(c, "SELECT COUNT(*) FROM " + tabla)));
            }
        }
    }

    // ---------- Datos: lo que la base de datos no vigila y tiene que vigilar la app ----------

    private static void comprobarDatos(Connection c, Set<String> tablas, Informe informe) throws SQLException {
        if (tablas.contains("configuracion")) {
            if (contar(c, "SELECT COUNT(*) FROM configuracion") == 0) {
                informe.aviso("Falta la configuración del dueño: la app no deja registrar contratos ni facturas hasta rellenarla");
            } else {
                informe.ok("Configuración del dueño rellenada");
            }
        }

        if (tablas.contains("contrato")) {
            informe.info("Contratos vigentes hoy: " + contar(c,
                    "SELECT COUNT(*) FROM contrato WHERE fecha_inicio <= CURRENT_DATE "
                            + "AND (fecha_fin IS NULL OR fecha_fin >= CURRENT_DATE)"));
        }

        // Dos contratos se solapan si uno empieza ANTES de que acabe el otro (el mismo día vale)
        revisar(c, tablas, informe, List.of("contrato"),
                "Ningún contrato se solapa con otro en la misma habitación",
                "Contratos que se solapan en la misma habitación (contrato / contrato)",
                "SELECT a.id, b.id FROM contrato a JOIN contrato b "
                        + "ON a.habitacion_id = b.habitacion_id AND a.id < b.id "
                        + "WHERE a.fecha_inicio < COALESCE(b.fecha_fin, " + SIN_FIN + ") "
                        + "AND b.fecha_inicio < COALESCE(a.fecha_fin, " + SIN_FIN + ")");

        revisar(c, tablas, informe, List.of("contrato", "contrato_persona"),
                "Nadie está en dos contratos a la vez",
                "Personas en dos contratos a la vez (persona / contrato / contrato)",
                "SELECT DISTINCT p1.persona_id, a.id, b.id FROM contrato_persona p1 "
                        + "JOIN contrato_persona p2 ON p1.persona_id = p2.persona_id AND p1.contrato_id < p2.contrato_id "
                        + "JOIN contrato a ON a.id = p1.contrato_id JOIN contrato b ON b.id = p2.contrato_id "
                        + "WHERE a.fecha_inicio < COALESCE(b.fecha_fin, " + SIN_FIN + ") "
                        + "AND b.fecha_inicio < COALESCE(a.fecha_fin, " + SIN_FIN + ")");

        revisar(c, tablas, informe, List.of("contrato", "contrato_persona"),
                "Cada contrato tiene las personas que dice su tipo (individual 1, pareja 2)",
                "Contratos cuyo tipo no coincide con sus personas (contrato / tipo / personas)",
                "SELECT c.id, c.tipo, COUNT(p.persona_id) FROM contrato c "
                        + "LEFT JOIN contrato_persona p ON p.contrato_id = c.id GROUP BY c.id, c.tipo "
                        + "HAVING (c.tipo = 'INDIVIDUAL' AND COUNT(p.persona_id) <> 1) "
                        + "OR (c.tipo = 'PAREJA' AND COUNT(p.persona_id) <> 2)");

        revisar(c, tablas, informe, List.of("factura", "reparto"),
                "Los repartos de cada factura suman su importe total",
                "Facturas cuyos repartos no suman el total (factura / total / suma)",
                "SELECT f.id, f.importe_total, SUM(r.importe) FROM factura f JOIN reparto r ON r.factura_id = f.id "
                        + "GROUP BY f.id, f.importe_total HAVING SUM(r.importe) <> f.importe_total");
    }

    /** Si hay filas, es un fallo; si no, está bien. Se salta la comprobación si faltan tablas. */
    private static void revisar(Connection c, Set<String> tablas, Informe informe, List<String> necesarias,
                                String bien, String mal, String sql) throws SQLException {
        if (!tablas.containsAll(necesarias)) {
            informe.aviso("No se pudo comprobar «" + bien + "»: faltan tablas");
            return;
        }
        List<String> filas = filas(c, sql);
        if (filas.isEmpty()) {
            informe.ok(bien);
        } else {
            informe.fallo(mal + ": " + String.join("; ", filas) + (filas.size() == 10 ? "; ..." : ""));
        }
    }

    // ---------- Consultas auxiliares ----------

    private static List<String> nombresDeTablas(MotorDB motor, Connection c) throws SQLException {
        String sql = motor == MotorDB.SQLITE
                ? "SELECT name FROM sqlite_master WHERE type = 'table' AND substr(name, 1, 7) <> 'sqlite_'"
                : "SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'";
        List<String> nombres = new ArrayList<>();
        try (Statement sentencia = c.createStatement(); ResultSet r = sentencia.executeQuery(sql)) {
            while (r.next()) {
                nombres.add(r.getString(1));
            }
        }
        return nombres;
    }

    private static long contar(Connection c, String sql) throws SQLException {
        try (Statement sentencia = c.createStatement(); ResultSet r = sentencia.executeQuery(sql)) {
            r.next();
            return r.getLong(1);
        }
    }

    private static String texto(Connection c, String sql) throws SQLException {
        return texto(c, sql, 1);
    }

    private static String texto(Connection c, String sql, int columna) throws SQLException {
        try (Statement sentencia = c.createStatement(); ResultSet r = sentencia.executeQuery(sql)) {
            return r.next() ? r.getString(columna) : null;
        }
    }

    // Las primeras 10 filas, cada una con sus columnas separadas por " /"
    private static List<String> filas(Connection c, String sql) throws SQLException {
        List<String> filas = new ArrayList<>();
        try (Statement sentencia = c.createStatement(); ResultSet r = sentencia.executeQuery(sql)) {
            int columnas = r.getMetaData().getColumnCount();
            while (r.next() && filas.size() < 10) {
                StringBuilder fila = new StringBuilder();
                for (int i = 1; i <= columnas; i++) {
                    if (i > 1) {
                        fila.append(" / ");
                    }
                    fila.append(r.getString(i));
                }
                filas.add(fila.toString());
            }
        }
        return filas;
    }

    @FunctionalInterface
    private interface Paso {
        void ejecutar() throws SQLException;
    }

    /** Si una comprobación falla por un error de SQL, se anota y se sigue con las demás. */
    private static void paso(Informe informe, String nombre, Paso paso) {
        try {
            paso.ejecutar();
        } catch (SQLException e) {
            informe.fallo(nombre + ": " + e.getMessage());
        }
    }

    // ---------- El texto del informe ----------

    static final class Informe {

        private final StringBuilder texto = new StringBuilder();
        private int avisos;
        private int fallos;

        Informe(String titulo) {
            String raya = "=".repeat(64);
            texto.append(raya).append('\n').append(' ').append(titulo).append('\n').append(raya).append('\n');
        }

        void ok(String mensaje) {
            linea("[OK]     ", mensaje);
        }

        void info(String mensaje) {
            linea("[INFO]   ", mensaje);
        }

        void aviso(String mensaje) {
            avisos++;
            linea("[AVISO]  ", mensaje);
        }

        void fallo(String mensaje) {
            fallos++;
            linea("[FALLO]  ", mensaje);
        }

        void detalle(String mensaje) {
            linea("         ", mensaje);
        }

        private void linea(String marca, String mensaje) {
            texto.append(marca).append(mensaje).append('\n');
        }

        Resultado resultado() {
            return new Resultado(texto.toString(), avisos, fallos);
        }
    }
}
