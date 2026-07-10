package com.csj.archive.market.profitability;

import com.csj.archive.market.common.IdGenerator;
import com.csj.archive.market.common.NotFoundException;
import com.csj.archive.market.inbox.ExternalEventRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExternalCostComponentAdapter {

    private final ProfitabilityCostComponentAdjustmentRepository adjustmentRepository;
    private final OrderProfitabilityService profitabilityService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ExternalCostComponentAdapter(ProfitabilityCostComponentAdjustmentRepository adjustmentRepository,
                                        OrderProfitabilityService profitabilityService,
                                        ObjectMapper objectMapper,
                                        Clock clock) {
        this.adjustmentRepository = adjustmentRepository;
        this.profitabilityService = profitabilityService;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Transactional
    public Optional<ProfitabilityCostComponentAdjustmentEntity> adapt(ExternalEventRequest request) {
        if (adjustmentRepository.existsBySourceEventIdOrIdempotencyKey(request.eventId(), request.idempotencyKey())) {
            return Optional.empty();
        }
        Optional<CostComponentType> componentType = componentType(request);
        Optional<String> orderId = text(request.payload(), "orderId");
        Optional<BigDecimal> amount = amount(request.payload(), componentType.orElse(null));
        if (componentType.isEmpty() || orderId.isEmpty() || amount.isEmpty()) {
            return Optional.empty();
        }
        OrderProfitabilityAssessmentEntity assessment;
        try {
            assessment = profitabilityService.applyMeasuredCost(orderId.get(), componentType.get(), amount.get());
        } catch (NotFoundException ex) {
            return Optional.empty();
        }
        ProfitabilityCostComponentAdjustmentEntity adjustment = adjustmentRepository.save(
                new ProfitabilityCostComponentAdjustmentEntity(
                        IdGenerator.prefixed("COSTADJ"),
                        assessment.getOrderId(),
                        request.source(),
                        request.eventId(),
                        request.idempotencyKey(),
                        componentType.get(),
                        amount.get(),
                        text(request.payload(), "currency").orElse("KRW"),
                        json(request.payload()),
                        Instant.now(clock)));
        return Optional.of(adjustment);
    }

    private Optional<CostComponentType> componentType(ExternalEventRequest request) {
        String source = request.source().toUpperCase(Locale.ROOT);
        String eventType = request.eventType().toUpperCase(Locale.ROOT);
        if (source.contains("NEXUS") || eventType.contains("PRODUCTION") || eventType.contains("MANUFACTURING")) {
            return Optional.of(CostComponentType.PRODUCTION_COST);
        }
        if (source.contains("LOGISTICS") || eventType.contains("SHIPMENT") || eventType.contains("LOGISTICS")
                || eventType.contains("DELIVERY")) {
            return Optional.of(CostComponentType.LOGISTICS_COST);
        }
        if (source.contains("LEDGER") || eventType.contains("LEDGER") || eventType.contains("SETTLEMENT")
                || eventType.contains("PAYMENT")) {
            if (eventType.contains("PAYMENT_PROCESSING")) {
                return Optional.of(CostComponentType.PAYMENT_PROCESSING_FEE);
            }
            return Optional.of(CostComponentType.LEDGER_SETTLEMENT_FEE);
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> amount(Map<String, Object> payload, CostComponentType componentType) {
        if (componentType == null) {
            return Optional.empty();
        }
        List<String> fields = switch (componentType) {
            case PRODUCTION_COST -> List.of("actualProductionCost", "productionCost", "manufacturingCost", "totalCost");
            case LOGISTICS_COST -> List.of("actualLogisticsCost", "logisticsCost", "shipmentCost", "deliveryCost", "totalCost");
            case LEDGER_SETTLEMENT_FEE -> List.of("ledgerSettlementFee", "settlementFee", "ledgerFee", "feeAmount");
            case PAYMENT_PROCESSING_FEE -> List.of("paymentProcessingFee", "processingFee", "feeAmount");
        };
        for (String field : fields) {
            Object value = payload.get(field);
            if (value != null) {
                return Optional.of(toAmount(value));
            }
        }
        return Optional.empty();
    }

    private Optional<String> text(Map<String, Object> payload, String field) {
        Object value = payload.get(field);
        return value == null || value.toString().isBlank() ? Optional.empty() : Optional.of(value.toString());
    }

    private BigDecimal toAmount(Object value) {
        return new BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP);
    }

    private String json(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
