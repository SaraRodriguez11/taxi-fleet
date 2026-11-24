package com.flota.taxis.daily;

import java.math.BigDecimal;

/** Totales contables inmutables. Los nulos de SUM() (sin filas) se normalizan a cero. */
public record Totals(BigDecimal expected, BigDecimal received, BigDecimal savings, BigDecimal expenses,
        BigDecimal net) {

    public static final Totals ZERO = new Totals(null, null, null, null, null);

    public Totals {
        expected = nz(expected);
        received = nz(received);
        savings = nz(savings);
        expenses = nz(expenses);
        net = nz(net);
    }

    public Totals plus(Totals other) {
        return new Totals(expected.add(other.expected), received.add(other.received),
                savings.add(other.savings), expenses.add(other.expenses), net.add(other.net));
    }

    private static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
