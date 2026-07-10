package com.csj.archive.market.operations;

import com.csj.archive.market.inbox.MarketInboxService;
import com.csj.archive.market.outbox.MarketOutboxService;
import com.csj.archive.market.profitability.OrderProfitabilityService;
import com.csj.archive.market.revenue.MarketDailyCloseRepository;
import com.csj.archive.market.revenue.MarketEconomyService;
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

    public OperationsSummaryService(MarketEconomyService economyService, MarketOutboxService outboxService,
                                    MarketInboxService inboxService, MarketDailyCloseRepository dailyCloseRepository,
                                    OrderProfitabilityService profitabilityService) {
        this.economyService = economyService;
        this.outboxService = outboxService;
        this.inboxService = inboxService;
        this.dailyCloseRepository = dailyCloseRepository;
        this.profitabilityService = profitabilityService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> summary() {
        Map<String, Object> result = new LinkedHashMap<>(economyService.summary());
        result.put("outbox", outboxService.summary());
        result.put("inbox", inboxService.summary());
        result.put("profitability", profitabilityService.summary());
        result.put("integration", Map.of(
                "nexus", "DRY_RUN_CAPABLE",
                "ledger", "DRY_RUN_CAPABLE",
                "archiveOs", "DRY_RUN_CAPABLE"));
        result.put("lastDailyClose", dailyCloseRepository.findTopByOrderByCloseDateDescCreatedAtDesc().orElse(null));
        return result;
    }
}
