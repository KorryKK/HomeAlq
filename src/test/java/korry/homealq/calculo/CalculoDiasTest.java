package korry.homealq.calculo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CalculoDiasTest {

    // ===========================
    // MÉTODO AUXILIAR
    // ===========================

    /**
     * Crea una fecha del año 2026
     * @param dia día del mes
     * @param mes mes del año
     * @return fecha LocalDate
     */
    private LocalDate fecha(int dia, int mes) {
        return LocalDate.of(2026, mes, dia);
    }

    // ===========================
    // TESTS: diasDeFactura()
    // PARAMETRIZADOS
    // ===========================

    @ParameterizedTest(name = "{index} - {0}/{1}/{2} -> {3}/{4}/{5}: esperado {6} días")
    @CsvSource({
            "2026, 9,  19, 2026, 10, 19, 30",
            "2026, 6,  10, 2026, 9,  10, 92",
            "2026, 7,  19, 2026, 8,  19, 31,",
            "2026, 10, 19, 2026, 11, 19, 31",
            "2026, 12, 19, 2027, 1,  19, 31",
            "2027, 2,  19, 2027, 3,  19, 28",
            "2028, 2,  19, 2028, 3,  19, 29"
    })
    void diasDeFactura_debeCalcularDiasCorrectamente(
            int anioInicio, int mesInicio, int diaInicio,
            int anioFin, int mesFin, int diaFin,
            int esperado) {

        LocalDate inicio = LocalDate.of(
                anioInicio, mesInicio, diaInicio
        );

        LocalDate fin = LocalDate.of(
                anioFin, mesFin, diaFin
        );

        assertEquals(
                esperado,
                CalculoDias.diasDeFactura(inicio, fin)
        );
    }


    // ===========================
    // TESTS: diasDeFactura()
    // ===========================

    @Test
    @DisplayName("Una factura de un mes debe devolver 30 días")
    void diasDeFactura_unMes_devuelve30() {
        assertEquals(
                30,
                CalculoDias.diasDeFactura(
                        fecha(19,9),
                        fecha(19,10)
                )
        );
    }

    @Test
    @DisplayName("Una factura de tres meses debe devolver 92 días")
    void diasDeFactura_tresMeses_devuelve92() {
        assertEquals(
                92,
                CalculoDias.diasDeFactura(
                        fecha(10,6),
                        fecha(10,9)
                )
        );
    }

    @Test
    @DisplayName("Una factura de julio a agosto debe devolver 31 días")
    void diasDeFactura_julioAgosto_devuelve31() {
        assertEquals(
                31,
                CalculoDias.diasDeFactura(
                        fecha(19,7),
                        fecha(19,8)
                )
        );
    }

    @Test
    @DisplayName("Una factura de octubre a noviembre debe devolver 31 días")
    void diasDeFactura_octubreNoviembre_devuelve31() {
        assertEquals(
                31,
                CalculoDias.diasDeFactura(
                        fecha(19,10),
                        fecha(19,11)
                )
        );
    }

    @Test
    @DisplayName("Una factura de diciembre a enero debe devolver 31 días")
    void diasDeFactura_diciembreEnero_devuelve31() {
        assertEquals(
                31,
                CalculoDias.diasDeFactura(
                        LocalDate.of(2026, 12, 19),
                        LocalDate.of(2027, 1, 19)
                )
        );
    }

    @Test
    @DisplayName("Febrero de 2027 debe devolver 28 días")
    void diasDeFactura_febrero2027_devuelve28() {
        assertEquals(
                28,
                CalculoDias.diasDeFactura(
                        LocalDate.of(2027, 2, 19),
                        LocalDate.of(2027, 3, 19)
                )
        );
    }

    @Test
    @DisplayName("Febrero de 2028 debe devolver 29 días por ser bisiesto")
    void diasDeFactura_febrero2028_devuelve29() {
        assertEquals(
                29,
                CalculoDias.diasDeFactura(
                        LocalDate.of(2028, 2, 19),
                        LocalDate.of(2028, 3, 19)
                )
        );
    }

    @Test
    @DisplayName("Una factura con fechas iguales debe lanzar una excepción")
    void diasDeFactura_fechasIguales_lanzaExcepcion() {
        IllegalArgumentException excepcion = assertThrows(
                IllegalArgumentException.class,
                () -> CalculoDias.diasDeFactura(
                        fecha(19,9),
                        fecha(19,9)
                )
        );

        assertEquals(
                "La fecha fin debe de ser posterior a la fecha de inicio.",
                excepcion.getMessage()
        );
    }

    @Test
    @DisplayName("Una factura con fecha final anterior debe lanzar una excepción")
    void diasDeFactura_fechaFinalAnterior_lanzaExcepcion() {
        IllegalArgumentException e = assertThrows(
                IllegalArgumentException.class,
                () -> CalculoDias.diasDeFactura(
                        fecha (19,9),
                        fecha(19,8)
                )
        );

        assertEquals(
                "La fecha fin debe de ser posterior a la fecha de inicio.",
                e.getMessage()
        );
    }


    // ===========================
    // CONFIGURACION DE FACTURA
    // ===========================
    private final LocalDate inicioFactura = fecha(19,9);
    private final LocalDate finFactura = fecha(19,10);

    // ===========================
    // TESTS: diasDeContratoEnFactura()
    // PARAMETRIZADOS
    // ===========================

    @ParameterizedTest(name = """
            Test {index} - DIAS CONTRATO EN FACTURA
            FACTURA: 19/9/2026 -> 19/10/2026 -> 30 DÍAS
            CONTRATO: {0}/{1}/{2} -> {3}/{4}/{5} -> {6} DÍAS
            """)
    @CsvSource(
            value = {
                    "1, 9, 2026, null, null, null, 30"
            },
            nullValues = "null"
    )
    void testContrato(
            int diaEntrada,
            int mesEntrada,
            int anioEntrada,
            Integer diaSalida,
            Integer mesSalida,
            Integer anioSalida,
            int esperado
    ) {
        LocalDate entrada = LocalDate.of(
                anioEntrada, mesEntrada, diaEntrada
        );

        LocalDate salida = null;

        if (diaSalida != null) {
            salida = LocalDate.of(
                    anioSalida,  mesSalida, diaSalida
            );
        }

        assertEquals(
                esperado,
                CalculoDias.diasDeContratoEnFactura(entrada, salida, inicioFactura, finFactura)
        );
    }


    // ===========================
    // TESTS: diasDeContratoEnFactura()
    // ===========================

    @Test
    @DisplayName("Un contrato anterior a la factura y abierto ocupa 30 días")
    void diasDeContrato_entradaAnterior_contratoAbierto_devuelve30() {
        assertEquals(
                30,
                CalculoDias.diasDeContratoEnFactura(
                        fecha(1, 9),
                        null,
                        inicioFactura,
                        finFactura
                )
        );
    }

    @Test
    @DisplayName("Un contrato que empieza el primer día ocupa 30 días")
    void diasDeContrato_entradaPrimerDia_contratoAbierto_Devuelve30() {
        assertEquals(
                30,
                CalculoDias.diasDeContratoEnFactura(
                        fecha(19, 9),
                        null,
                        inicioFactura,
                        finFactura
                )
        );
    }

    @Test
    @DisplayName("Un contrato que empieza el 20 de septiembre ocupa 30 días")
    void diasDeContrato_entrada20Sep_contratoAbierto_Devuelve30() {
        assertEquals(
                30,
                CalculoDias.diasDeContratoEnFactura(
                        fecha(20,9),
                        null,
                        inicioFactura,
                        finFactura
                )
        );
    }

    @Test
    @DisplayName("Un contrato que empieza el 21 de septiembre ocupa 29 días")
    void diasDeContrato_entrada21Sep_contratoAbierto_Devuelve29() {
        assertEquals(
                29,
                CalculoDias.diasDeContratoEnFactura(
                        fecha(21,9),
                        null,
                        inicioFactura,
                        finFactura
                )
        );
    }

    @Test
    @DisplayName("Un contrato que empieza el 30 de septiembre ocupa 20 días")
    void diasDeContrato_entrada30Sep_contratoAbierto_Devuelve20() {
        assertEquals(
                20,
                CalculoDias.diasDeContratoEnFactura(
                        fecha(30,9),
                        null,
                        inicioFactura,
                        finFactura
                )
        );
    }

    @Test
    @DisplayName("Un contrato que termina el 18 de octubre ocupa 30 días")
    void diasDeContrato_salida18Oct_devuelve30() {
        assertEquals(
                30,
                CalculoDias.diasDeContratoEnFactura(
                        fecha(1,9),
                        fecha(18,10),
                        inicioFactura,
                        finFactura
                )
        );
    }

    @Test
    @DisplayName("Un contrato que termina el 4 de octubre ocupa 16 días")
    void diasDeContrato_salida4Oct_devuelve16() {
        assertEquals(
                16,
                CalculoDias.diasDeContratoEnFactura(
                        fecha(1,9),
                        fecha(4,10),
                        inicioFactura,
                        finFactura
                )
        );
    }

    @Test
    @DisplayName("Un contrato que termina el 19 de septiembre ocupa 1 día")
    void diasDeContrato_salida19Sep_devuelve1() {
        assertEquals(
                1,
                CalculoDias.diasDeContratoEnFactura(
                        fecha(1, 9),
                        fecha(19,9),
                        inicioFactura,
                        finFactura
                )
        );
    }

    @Test
    @DisplayName("Un contrato que empieza el último día ocupa 1 día")
    void diasDeContrato_entrada19Oct_contratoAbierto_devuelve1() {
        assertEquals(
                1,
                CalculoDias.diasDeContratoEnFactura(
                        fecha(19,10),
                        null,
                        inicioFactura,
                        finFactura
                )
        );
    }

    @Test
    @DisplayName("Un contrato que termina después de la factura ocupa 30 días")
    void diasDeContrato_salidaPosterior_devuelve30() {
        assertEquals(
                30,
                CalculoDias.diasDeContratoEnFactura(
                        fecha(1,9),
                        fecha(1,11),
                        inicioFactura,
                        finFactura
                )
        );
    }

    @Test
    @DisplayName("Un contrato que termina antes de la factura ocupa 0 días")
    void diasDeContrato_saleAntesDeFactura_devuelve0() {
        assertEquals(
                0,
                CalculoDias.diasDeContratoEnFactura(
                        fecha(1,9),
                        fecha(10,9),
                        inicioFactura,
                        finFactura
                )
        );
    }

    @Test
    @DisplayName("Un contrato que empieza después de la factura ocupa 0 días")
    void diasDeContrato_entradaDespuesDeFactura_devuelve0() {
        assertEquals(
                0,
                CalculoDias.diasDeContratoEnFactura(
                        fecha(25,10),
                        null,
                        inicioFactura,
                        finFactura
                )
        );
    }

}
