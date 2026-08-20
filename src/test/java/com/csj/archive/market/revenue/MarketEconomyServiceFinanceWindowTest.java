package com.csj.archive.market.revenue;

import com.csj.archive.market.capital.MarketCapitalService;
import com.csj.archive.market.claim.MarketClaimRepository;
import com.csj.archive.market.claim.MarketReturnRepository;
import com.csj.archive.market.order.MarketOrderRepository;
import com.csj.archive.market.outbox.MarketOutboxService;
import com.csj.archive.market.payment.MarketPaymentRepository;
import com.csj.archive.market.profitability.OrderProfitabilityService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MarketEconomyServiceFinanceWindowTest {
    private static final Instant NOW = Instant.parse("2026-08-20T03:00:00Z");
    private static final Instant START = NOW.minus(Duration.ofHours(24));

    private final MarketRevenueEventRepository revenueRepository = mock(MarketRevenueEventRepository.class);
    private final MarketCostEventRepository costRepository = mock(MarketCostEventRepository.class);
    private final MarketOrderRepository orderRepository = mock(MarketOrderRepository.class);
    private final MarketPaymentRepository paymentRepository = mock(MarketPaymentRepository.class);
    private final MarketCapitalService capitalService = mock(MarketCapitalService.class);

    @Test
    void currentFinanceAvailabilityIgnoresOrdersWhenNoRevenueOrCostEventExists() {
        stubWindowTotals();
        when(revenueRepository.findLatestCreatedAtBetween(START, NOW)).thenReturn(Optional.empty());
        when(costRepository.findLatestCreatedAtBetween(START, NOW)).thenReturn(Optional.empty());

        Map<String, Object> summary = service().financialSummary();

        assertThat(summary).containsEntry("calculationScope", "ROLLING_24H_RECOGNIZED_EVENTS")
                .containsEntry("periodStart", START)
                .containsEntry("periodEnd", NOW)
                .containsEntry("dataAvailable", false)
                .containsEntry("sourceLatestEventAt", null);
        verify(orderRepository, never()).findLatestCreatedAt();
    }

    @Test
    void currentFinanceLineageUsesLatestRevenueOrCostInsideTheBoundedWindow() {
        stubWindowTotals();
        when(revenueRepository.findLatestCreatedAtBetween(START, NOW))
                .thenReturn(Optional.of(NOW.minus(Duration.ofMinutes(10))));
        when(costRepository.findLatestCreatedAtBetween(START, NOW))
                .thenReturn(Optional.of(NOW.minus(Duration.ofMinutes(5))));

        Map<String, Object> summary = service().financialSummary();

        assertThat(summary).containsEntry("dataAvailable", true)
                .containsEntry("sourceLatestEventAt", NOW.minus(Duration.ofMinutes(5)));
    }

    private void stubWindowTotals() {
        when(orderRepository.totalGmvBetween(START, NOW)).thenReturn(BigDecimal.ZERO);
        when(paymentRepository.totalAmountByPaymentStatusBetween(any(), eq(START), eq(NOW))).thenReturn(BigDecimal.ZERO);
        when(revenueRepository.totalRevenueByTypesBetween(any(), eq(START), eq(NOW))).thenReturn(BigDecimal.ZERO);
        when(costRepository.totalCostBetween(START, NOW)).thenReturn(BigDecimal.ZERO);
        when(costRepository.totalCostByTypesBetween(any(), eq(START), eq(NOW))).thenReturn(BigDecimal.ZERO);
        when(revenueRepository.findAll()).thenReturn(List.of());
        when(costRepository.findAll()).thenReturn(List.of());
        when(capitalService.workforceSummary()).thenReturn(Map.of(
                "backlog", 0,
                "capacityUtilization", BigDecimal.ZERO));
    }

    private MarketEconomyService service() {
        return new MarketEconomyService(
                revenueRepository,
                costRepository,
                mock(MarketProfitSnapshotRepository.class),
                mock(MarketDailyCloseRepository.class),
                orderRepository,
                mock(MarketReturnRepository.class),
                mock(MarketClaimRepository.class),
                paymentRepository,
                mock(MarketOutboxService.class),
                mock(OrderProfitabilityService.class),
                capitalService,
                Clock.fixed(NOW, ZoneOffset.UTC));
    }
}
