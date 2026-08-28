package com.csj.archive.market.capital;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.csj.archive.market.order.MarketOrderRepository;
import com.csj.archive.market.payment.MarketPaymentRepository;
import com.csj.archive.market.profitability.OrderProfitabilityService;
import com.csj.archive.market.revenue.CostType;
import com.csj.archive.market.revenue.MarketCostEventRepository;
import com.csj.archive.market.revenue.MarketRevenueEventRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MarketCapitalServiceCashflowTest {

    @Test
    void cashflowDoesNotTreatPassThroughOrPayrollTwiceAsOperatingLoss() {
        MarketWorkforceAllocationRepository workforce = mock(MarketWorkforceAllocationRepository.class);
        MarketRevenueEventRepository revenue = mock(MarketRevenueEventRepository.class);
        MarketCostEventRepository cost = mock(MarketCostEventRepository.class);
        MarketOrderRepository orders = mock(MarketOrderRepository.class);
        MarketPaymentRepository payments = mock(MarketPaymentRepository.class);
        when(workforce.findByWorkdayIdAndEnabledTrueOrderByWorkforceRoleAsc(any())).thenReturn(List.of());
        when(revenue.totalRevenueByTypes(any())).thenReturn(new BigDecimal("132000"));
        when(cost.totalCost()).thenReturn(new BigDecimal("900000"));
        when(cost.totalCostByTypes(any())).thenAnswer(invocation -> {
            Iterable<CostType> types = invocation.getArgument(0);
            for (CostType type : types) {
                if (type == CostType.MARKET_OPERATION_COST_INCURRED) return new BigDecimal("100000");
            }
            return BigDecimal.ZERO;
        });
        when(payments.totalAmountByPaymentStatus(any())).thenReturn(BigDecimal.ZERO);
        when(orders.totalGmv()).thenReturn(BigDecimal.ZERO);

        MarketCapitalService service = new MarketCapitalService(
                workforce, mock(MarketWorkdaySnapshotRepository.class), revenue, cost, orders, payments,
                mock(OrderProfitabilityService.class));

        Map<String, Object> summary = service.cashflowSummary();

        assertThat(summary).containsEntry("recognizedRevenue", new BigDecimal("132000"))
                .containsEntry("totalExpense", new BigDecimal("100000"))
                .containsEntry("totalRecordedCost", new BigDecimal("900000"))
                .containsEntry("netProfit", new BigDecimal("32000"))
                .containsEntry("availableCash", new BigDecimal("50032000"));
    }
}
