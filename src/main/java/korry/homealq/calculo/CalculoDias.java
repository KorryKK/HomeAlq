package korry.homealq.calculo;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class CalculoDias {

    public static int diasDeFactura(
            LocalDate fechaInicio,
            LocalDate fechaFin){

        long dias = ChronoUnit.DAYS.between(fechaInicio, fechaFin);

        if (dias <= 0) {
            throw new IllegalArgumentException(
                    "La fecha fin debe de ser posterior a la fecha de inicio."
            );
        }

        return Math.toIntExact(dias);

    }

    public static int diasDeContratoEnFactura(
            LocalDate entrada,
            LocalDate salida,
            LocalDate inicioFactura,
            LocalDate finFactura) {

        // Validamos y obtenemos los días de la factura
        int diasFactura = diasDeFactura(inicioFactura, finFactura);

        // El inicio del tramo será la fecha más tardía
        LocalDate inicio = entrada.isAfter(inicioFactura)
                ? entrada
                : inicioFactura;

        // Si el contrato sigue abierto, utilizamos el fin de factura
        LocalDate fin = (salida == null || salida.isAfter(finFactura))
                ? finFactura
                : salida;

        if (inicio.isAfter(fin)) {
            return 0;
        }

        // Contamos ambos extremos
        long diasTramo = ChronoUnit.DAYS.between(inicio, fin) +1;

        // Nunca puede superar los días de la factura
        return Math.toIntExact(Math.min(diasTramo, diasFactura));

    }

    static void prueba() {
        LocalDate inicio = LocalDate.of(2026, 6,5);
        LocalDate fin = LocalDate.of(2026, 9,5);

        int dias = diasDeFactura(inicio, fin);

        IO.println("Días de factura: " + dias);
    }
}
