package com.flota.taxis.report;

import com.flota.taxis.daily.Totals;

public record TaxiTotals(Long taxiId, String plate, Totals totals) {

    public TaxiTotals plus(TaxiTotals other) {
        return new TaxiTotals(taxiId, plate, totals.plus(other.totals));
    }
}
