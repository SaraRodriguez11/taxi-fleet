package com.flota.taxis.daily;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.flota.taxis.common.exception.BusinessRuleException;
import com.flota.taxis.taxi.Taxi;

class DailyRecordTest {

    private DailyRecord record;

    @BeforeEach
    void setUp() {
        Taxi taxi = new Taxi(null, "abc123", "Hyundai i10 2022", null);
        record = DailyRecord.open(taxi, LocalDate.of(2026, 3, 2), false, new BigDecimal("120000"));
    }

    private static BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    @Test
    void netoEsProduccionMenosAhorroMenosGastos() {
        record.register(false, bd("120000"), bd("130000"), bd("10000"), bd("25000.50"), null, null);

        assertThat(record.getNetDelivered()).isEqualByComparingTo("94999.50");
    }

    @Test
    void rechazaGastosQueSuperanLaProduccionRecibida() {
        assertThatThrownBy(() -> record.register(false, bd("120000"), bd("50000"), bd("30000"), bd("30000"), null, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rechazaMontosNegativos() {
        assertThatThrownBy(() -> record.register(false, bd("120000"), bd("-1"), bd("0"), bd("0"), null, null))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void diaDePicoYPlacaAbreSinProduccionEsperadaYEnDescanso() {
        Taxi taxi = new Taxi(null, "ABC123", "Kia Picanto", null);
        DailyRecord day = DailyRecord.open(taxi, LocalDate.of(2026, 3, 2), true, bd("120000"));

        assertThat(day.getExpectedProduction()).isEqualByComparingTo("0");
        assertThat(day.getStatus()).isEqualTo(ComplianceStatus.DESCANSO);
    }

    @Test
    void estadoSeDerivaDeLaProduccionCuandoNoSeIndica() {
        record.register(false, bd("120000"), bd("120000"), bd("0"), bd("0"), null, null);
        assertThat(record.getStatus()).isEqualTo(ComplianceStatus.CUMPLIDO);

        record.register(false, bd("120000"), bd("80000"), bd("0"), bd("0"), null, null);
        assertThat(record.getStatus()).isEqualTo(ComplianceStatus.PARCIAL);

        record.register(false, bd("120000"), bd("0"), bd("0"), bd("0"), null, null);
        assertThat(record.getStatus()).isEqualTo(ComplianceStatus.PENDIENTE);
    }

    @Test
    void cambiarElEstadoNoTocaLosMontos() {
        record.register(false, bd("120000"), bd("100000"), bd("10000"), bd("5000"), null, null);

        record.changeStatus(ComplianceStatus.DESCANSO);

        assertThat(record.getStatus()).isEqualTo(ComplianceStatus.DESCANSO);
        assertThat(record.getNetDelivered()).isEqualByComparingTo("85000");
    }

    @Test
    void marcarEntregadoCompletaLoRecibidoYRecalculaElNeto() {
        record.register(false, bd("120000"), bd("0"), bd("10000"), bd("0"), null, null);

        record.changeStatus(ComplianceStatus.CUMPLIDO);

        assertThat(record.getStatus()).isEqualTo(ComplianceStatus.CUMPLIDO);
        assertThat(record.getReceivedProduction()).isEqualByComparingTo("120000");
        assertThat(record.getNetDelivered()).isEqualByComparingTo("110000");
    }

    @Test
    void marcarEntregadoNoReducePorEncimaDeLoYaRecibido() {
        record.register(false, bd("120000"), bd("130000"), bd("0"), bd("0"), null, null);

        record.changeStatus(ComplianceStatus.CUMPLIDO);

        assertThat(record.getReceivedProduction()).isEqualByComparingTo("130000");
    }

    @Test
    void estadoNuloEsRechazado() {
        assertThatThrownBy(() -> record.changeStatus(null)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void recalcularPicoPlacaEnDiaSinMovimientoAjustaProduccionEsperadaYEstado() {
        // El día abre normal (esperada 120000, PENDIENTE) y luego resulta que sí era Pico y Placa
        record.reapplyPicoPlaca(true, bd("120000"));
        assertThat(record.isPicoPlaca()).isTrue();
        assertThat(record.getExpectedProduction()).isEqualByComparingTo("0");
        assertThat(record.getStatus()).isEqualTo(ComplianceStatus.DESCANSO);

        // ...y al revés: deja de ser Pico y Placa y recupera la producción esperada
        record.reapplyPicoPlaca(false, bd("130000"));
        assertThat(record.isPicoPlaca()).isFalse();
        assertThat(record.getExpectedProduction()).isEqualByComparingTo("130000");
        assertThat(record.getStatus()).isEqualTo(ComplianceStatus.PENDIENTE);
    }

    @Test
    void recalcularPicoPlacaEnDiaConMovimientoSoloCambiaElIndicador() {
        record.register(false, bd("120000"), bd("110000"), bd("10000"), bd("0"), null, null);

        record.reapplyPicoPlaca(true, bd("999999"));

        assertThat(record.isPicoPlaca()).isTrue();
        assertThat(record.getExpectedProduction()).isEqualByComparingTo("120000");
        assertThat(record.getReceivedProduction()).isEqualByComparingTo("110000");
        assertThat(record.getStatus()).isEqualTo(ComplianceStatus.PARCIAL);
    }

    @Test
    void recalcularPicoPlacaRespetaUnEstadoManual() {
        record.changeStatus(ComplianceStatus.PARCIAL); // el administrador lo fijó a mano, sin dinero

        record.reapplyPicoPlaca(true, bd("120000"));

        assertThat(record.getExpectedProduction()).isEqualByComparingTo("120000");
        assertThat(record.getStatus()).isEqualTo(ComplianceStatus.PARCIAL);
    }

    @Test
    void estadoExplicitoTienePrioridadSobreElDerivado() {
        record.register(false, bd("120000"), bd("0"), bd("0"), bd("0"), ComplianceStatus.DESCANSO, "Taller");

        assertThat(record.getStatus()).isEqualTo(ComplianceStatus.DESCANSO);
        assertThat(record.getNotes()).isEqualTo("Taller");
    }
}
