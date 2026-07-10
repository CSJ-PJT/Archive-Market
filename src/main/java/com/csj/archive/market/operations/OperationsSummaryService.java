package com.csj.archive.market.operations;

import com.csj.archive.market.capital.MarketCapitalService;
import com.csj.archive.market.inbox.MarketInboxService;
import com.csj.archive.market.outbox.MarketOutboxService;
import com.csj.archive.market.payment.MarketPaymentRepository;
import com.csj.archive.market.payment.PaymentStatus;
import com.csj.archive.market.profitability.OrderProfitabilityService;
import com.csj.archive.market.revenue.MarketDailyCloseRepository;
import com.csj.archive.market.revenue.MarketEconomyService;
import com.csj.archive.market.runtime.RuntimeAutoRunService;
import com.csj.archive.market.runtime.RuntimeEventService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OperationsSummaryService {

    private final MarketEconomyService economyService;
    private final MarketOutboxService outboxService;
    private final MarketInboxService inboxService;
    private final MarketDailyCloseRepository dailyCloseRepository;
    private final OrderProfitabilityService profitabilityService;
    private final MarketCapitalService capitalService;
    private final RuntimeEventService runtimeEventService;
    private final RuntimeAutoRunService runtimeAutoRunService;
    private final MarketPaymentRepository paymentRepository;

    public OperationsSummaryService(MarketEconomyService economyService, MarketOutboxService outboxService,
                                    MarketInboxService inboxService, MarketDailyCloseRepository dailyCloseRepository,
                                    OrderProfitabilityService profitabilityService, MarketCapitalService capitalService,
                                    RuntimeEventService runtimeEventService, RuntimeAutoRunService runtimeAutoRunService,
                                    MarketPaymentRepository paymentRepository) {
        this.economyService = economyService;
        this.outboxService = outboxService;
        this.inboxService = inboxService;
        this.dailyCloseRepository = dailyCloseRepository;
        this.profitabilityService = profitabilityService;
        this.capitalService = capitalService;
        this.runtimeEventService = runtimeEventService;
        this.runtimeAutoRunService = runtimeAutoRunService;
        this.paymentRepository = paymentRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>(economyService.summary());
        result.put("serviceName", "Archive-Market");
        result.put("serviceRole", "Synthetic Commerce Backend");
        result.put("latestEventAt", runtimeEventService.latestEventAt().orElse(null));
        result.put("liveFlowAvailable", true);
        result.put("runtime", runtimeAutoRunService.status());
        result.put("degradedReason", "NONE");
        result.put("economy", economyAliases(result.get("economy")));
        result.put("payments", Map.of("captured", paymentRepository.countByPaymentStatus(PaymentStatus.CAPTURED)));
        result.put("outbox", outboxService.summary());
        result.put("inbox", inboxService.summary());
        result.put("profitability", profitabilityAliases(profitabilityService.summary()));
        result.putAll(capitalService.combinedSummary());
        result.put("integration", Map.of(
                "nexus", "DRY_RUN_CAPABLE",
                "ledger", "DRY_RUN_CAPABLE",
                "archiveOs", "DRY_RUN_CAPABLE"));
        result.put("lastDailyClose", dailyCloseRepository.findTopByOrderByCloseDateDescCreatedAtDesc().orElse(null));
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> economyAliases(Object economy) {
        Map<String, Object> result = new LinkedHashMap<>((Map<String, Object>) economy);
        result.put("revenue", result.get("totalRevenue"));
        result.put("cost", result.get("totalCost"));
        return result;
    }

    private Map<String, Object> profitabilityAliases(Map<String, Object> profitability) {
        Map<String, Object> result = new LinkedHashMap<>(profitability);
        result.put("reviewRequired", result.get("reviewRequiredOrders"));
        return result;
    }
}
