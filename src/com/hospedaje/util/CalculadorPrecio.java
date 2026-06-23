package com.hospedaje.util;

import com.hospedaje.modelo.Habitacion;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Calcula precios con rebajas y genera proyecciones
 * mensuales/anuales usando HashMap.
 */
public class CalculadorPrecio {

    // HashMap con las rebajas por tipo de cuarto
    // Clave: tipo de habitación | Valor: descuento por noche desde el 2do día
    private static final HashMap<String, Double> REBAJAS_POR_TIPO = new HashMap<>();

    static {
        REBAJAS_POR_TIPO.put("SIMPLE",      5.0);
        REBAJAS_POR_TIPO.put("SIMPLE_2P",   5.0);
        REBAJAS_POR_TIPO.put("MATRIMONIAL", 5.0);
        REBAJAS_POR_TIPO.put("INDIVIDUAL",  5.0);
        REBAJAS_POR_TIPO.put("DOBLE",       5.0);
    }

    // HashMap con días mínimos para aplicar rebaja por tipo
    private static final HashMap<String, Integer> DIAS_MINIMO_REBAJA = new HashMap<>();

    static {
        DIAS_MINIMO_REBAJA.put("SIMPLE",      2);
        DIAS_MINIMO_REBAJA.put("SIMPLE_2P",   2);
        DIAS_MINIMO_REBAJA.put("MATRIMONIAL", 2);
        DIAS_MINIMO_REBAJA.put("INDIVIDUAL",  2);
        DIAS_MINIMO_REBAJA.put("DOBLE",       2);
    }

    /**
     * Calcula el precio total considerando la rebaja si aplica.
     *
     * Regla:
     * - Día 1: precio normal
     * - Día 2 en adelante: precio normal - S/5 por noche
     *
     * @param habitacion  La habitación seleccionada
     * @param dias        Cantidad de días de alojamiento
     * @param usoCamas    1 o 2 (para cuartos dobles)
     * @return            Precio total a cobrar
     */
    public static BigDecimal calcularTotal(Habitacion habitacion, int dias, int usoCamas) {
        String tipo = habitacion.getTipo();

        // Precio base por noche
        BigDecimal precioBase;
        if ("DOBLE".equals(tipo) && usoCamas == 1 && habitacion.getPrecioCama1() != null) {
            precioBase = habitacion.getPrecioCama1();
        } else {
            precioBase = habitacion.getPrecioNoche();
        }

        if (dias <= 0) return precioBase;
        if (dias == 1) return precioBase;

        // Obtener rebaja por tipo
        double rebajaPorNoche = REBAJAS_POR_TIPO.getOrDefault(tipo, 0.0);
        int diasMinimo = DIAS_MINIMO_REBAJA.getOrDefault(tipo, 2);

        if (dias < diasMinimo || rebajaPorNoche == 0) {
            return precioBase.multiply(BigDecimal.valueOf(dias));
        }

        // Día 1 a precio normal, resto con rebaja
        BigDecimal precioConRebaja = precioBase.subtract(BigDecimal.valueOf(rebajaPorNoche));
        if (precioConRebaja.compareTo(BigDecimal.ZERO) < 0) {
            precioConRebaja = BigDecimal.ZERO;
        }

        BigDecimal total = precioBase.add(
                precioConRebaja.multiply(BigDecimal.valueOf(dias - 1))
        );

        return total.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Retorna el desglose del precio para mostrar al usuario.
     * Ejemplo: "1 noche x S/25 + 2 noches x S/20 = S/65"
     */
    public static String getDesglose(Habitacion habitacion, int dias, int usoCamas) {
        if (dias <= 0) return "";
        String tipo = habitacion.getTipo();

        BigDecimal precioBase;
        if ("DOBLE".equals(tipo) && usoCamas == 1 && habitacion.getPrecioCama1() != null) {
            precioBase = habitacion.getPrecioCama1();
        } else {
            precioBase = habitacion.getPrecioNoche();
        }

        double rebajaPorNoche = REBAJAS_POR_TIPO.getOrDefault(tipo, 0.0);
        int diasMinimo = DIAS_MINIMO_REBAJA.getOrDefault(tipo, 2);

        if (dias == 1 || dias < diasMinimo || rebajaPorNoche == 0) {
            return dias + " noche(s) x S/" + precioBase + " = S/"
                    + calcularTotal(habitacion, dias, usoCamas);
        }

        BigDecimal precioConRebaja = precioBase.subtract(BigDecimal.valueOf(rebajaPorNoche));
        BigDecimal total = calcularTotal(habitacion, dias, usoCamas);

        return String.format("1 noche x S/%.0f (precio normal) + %d noche(s) x S/%.0f (con rebaja S/%.0f) = S/%.2f",
                precioBase.doubleValue(),
                dias - 1,
                precioConRebaja.doubleValue(),
                rebajaPorNoche,
                total.doubleValue());
    }

    /**
     * Genera proyección mensual de ingresos usando HashMap.
     * Clave: nombre del mes | Valor: ingresos estimados
     *
     * Se basa en el promedio de ocupación real de la BD,
     * o usa un porcentaje estimado si no hay datos.
     *
     * @param ingresosRealesPorMes  Mapa con ingresos reales (mes -> total)
     * @param totalHabitaciones     Número total de habitaciones
     * @param precioPromedioNoche   Precio promedio por noche
     * @return LinkedHashMap ordenado por mes con proyección
     */
    public static LinkedHashMap<String, Double> proyeccionMensual(
            Map<Integer, Double> ingresosRealesPorMes,
            int totalHabitaciones,
            double precioPromedioNoche) {

        LinkedHashMap<String, Double> proyeccion = new LinkedHashMap<>();
        String[] meses = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};
        int[] diasPorMes = {31,28,31,30,31,30,31,31,30,31,30,31};

        for (int i = 1; i <= 12; i++) {
            if (ingresosRealesPorMes.containsKey(i)) {
                // Usar dato real si existe
                proyeccion.put(meses[i-1], ingresosRealesPorMes.get(i));
            } else {
                // Estimar con 60% de ocupación promedio
                double estimado = totalHabitaciones * precioPromedioNoche
                        * diasPorMes[i-1] * 0.60;
                proyeccion.put(meses[i-1], estimado);
            }
        }
        return proyeccion;
    }

    /**
     * Calculates the annual total by summing all the values in the monthly HashMap.
     */
    public static double totalAnual(LinkedHashMap<String, Double> proyeccionMensual) {
        return proyeccionMensual.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    /**
     * Returns whether a room has a rebate available.
     */
    public static boolean tieneRebaja(String tipo) {
        return REBAJAS_POR_TIPO.containsKey(tipo)
                && REBAJAS_POR_TIPO.get(tipo) > 0;
    }

    /**
     * Returns the rebate amount per night according to type.
     */
    public static double getRebajaPorNoche(String tipo) {
        return REBAJAS_POR_TIPO.getOrDefault(tipo, 0.0);
    }
}