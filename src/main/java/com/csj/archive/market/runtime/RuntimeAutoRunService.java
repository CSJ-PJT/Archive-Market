package com.csj.archive.market.runtime;

import com.csj.archive.market.capital.MarketCapitalService;
import com.csj.archive.market.order.CreateOrderRequest;
import com.csj.archive.market.order.MarketOrderEntity;
import com.csj.archive.market.order.MarketOrderService;
import com.csj.archive.market.payment.PaymentService;
import com.csj.archive.market.product.ProductEntity;
import com.csj.archive.market.product.ProductService;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class RuntimeAutoRunService {

    private static final String SERVICE_NAME = "Archive-Market";
    private static final Logger log = LoggerFactory.getLogger(RuntimeAutoRunService.class);

    private final RuntimeAutoRunProperties properties;
    private final ProductService productService;
    private final MarketOrderService orderService;
    private final PaymentService paymentService;
    private final MarketCapitalService capitalService;
    private final RuntimeEventService runtimeEventService;
    private final Clock clock;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private volatile Long lastTickKey;
    private volatile RuntimeStatusResponse status;

    public RuntimeAutoRunService(RuntimeAutoRunProperties properties, ProductService productService,
                                 MarketOrderService orderService, PaymentService paymentService,
                                 MarketCapitalService capitalService, RuntimeEventService runtimeEventService,
                                 Clock clock) {
        this.properties = properties;
        this.productService = productService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.capitalService = capitalService;
        this.runtimeEventService = runtimeEventService;
        this.clock = clock;
        this.status = new RuntimeStatusResponse(SERVICE_NAME, false, properties.getAutorun().isEnabled(),
                "IDLE", null, null, 0, 0, 0, "IDLE");
    }

    @Scheduled(fixedDelayString = "#{@runtimeAutoRunProperties.tickInterval.toMillis()}")
    public void scheduledTick() {
        if (!properties.getAutorun().isSchedulerEnabled()) {
            updateStatus("DISABLED", null, 0, 0, "IDLE");
            return;
        }
        runTick();
    }

    public RuntimeStatusResponse status() {
        Instant lastEventAt = runtimeEventService.latestEventAt().orElse(status.lastEventAt());
        return new RuntimeStatusResponse(SERVICE_NAME, properties.getAutorun().isEnabled(), properties.getAutorun().isEnabled(),
                schedulerStatus(), status.lastWorkAt(), lastEventAt, status.eventsProducedLastTick(),
                status.eventsConsumedLastTick(), status.backlogCount(), pipelineStatus(lastEventAt));
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startOnReady() {
        if (properties.getAutorun().isEnabled() && properties.getAutorun().isSchedulerEnabled()) {
            runTick();
        }
    }

    public RuntimeStatusResponse runTick() {
        if (!properties.getAutorun().isEnabled()) {
            updateStatus("DISABLED", null, 0, 0, "IDLE");
            return status();
        }
        if (!running.compareAndSet(false, true)) {
            updateStatus("LOCKED", null, 0, 0, "LIVE");
            return status();
        }
        try {
            long tickKey = tickKey();
            if (Objects.equals(lastTickKey, tickKey)) {
                updateStatus("RUNNING", null, 0, 0, "LIVE");
                return status();
            }
            Instant startedAt = Instant.now(clock);
            markRunningStarted(startedAt);
            int budget = Math.max(0, properties.getMaxEventsPerTick());
            int produced = 0;
            long backlog = backlogCount();
            if (budget > 0 && backlog <= properties.getMaxBacklogPerTick()) {
                createSyntheticOrderWork();
                produced++;
                budget--;
            }
            if (budget > 0) {
                capitalService.runWorkday(LocalDate.now(clock));
                produced++;
            }
            lastTickKey = tickKey;
            updateStatus("RUNNING", startedAt, produced, 0, "LIVE");
            return status();
        } catch (RuntimeException ex) {
            log.warn("Autonomous runtime work tick failed", ex);
            updateStatus("DEGRADED", null, 0, 0, "DEGRADED");
            return status();
        } finally {
            running.set(false);
        }
    }

    private void createSyntheticOrderWork() {
        List<ProductEntity> products = productService.seed();
        ProductEntity product = products.get((int) (Math.abs(tickKey()) % products.size()));
        MarketOrderEntity order = orderService.create(new CreateOrderRequest(null, product.getProductId(), 1, false));
        orderService.confirm(order.getOrderId());
        paymentService.capture(order.getOrderId());
    }

    private long backlogCount() {
        Object backlog = capitalService.workforceSummary().get("backlog");
        return backlog instanceof Number number ? number.longValue() : 0;
    }

    private void markRunningStarted(Instant startedAt) {
        this.status = new RuntimeStatusResponse(SERVICE_NAME, properties.getAutorun().isEnabled(),
                properties.getAutorun().isEnabled(), "RUNNING", startedAt, status.lastEventAt(), 0,
                0, status.backlogCount(), "LIVE");
    }

    private void updateStatus(String schedulerStatus, Instant lastWorkAt, int produced, int consumed,
                              String pipelineStatus) {
        Instant effectiveLastWorkAt = lastWorkAt == null ? status.lastWorkAt() : lastWorkAt;
        Instant lastEventAt = runtimeEventService.latestEventAt().orElse(status.lastEventAt());
        this.status = new RuntimeStatusResponse(SERVICE_NAME, properties.getAutorun().isEnabled(),
                properties.getAutorun().isEnabled(), schedulerStatus, effectiveLastWorkAt, lastEventAt, produced,
                consumed, backlogCount(), pipelineStatus);
    }

    private String schedulerStatus() {
        if (!properties.getAutorun().isEnabled() || !properties.getAutorun().isSchedulerEnabled()) {
            return "DISABLED";
        }
        return running.get() ? "RUNNING" : status.schedulerStatus();
    }

    private String pipelineStatus(Instant lastEventAt) {
        if (!properties.getAutorun().isEnabled()) {
            return "IDLE";
        }
        if (lastEventAt == null) {
            return "STARTING";
        }
        return "LIVE";
    }

    private long tickKey() {
        long intervalMillis = Math.max(1, properties.getTickInterval().toMillis());
        return Instant.now(clock).toEpochMilli() / intervalMillis;
    }
}
