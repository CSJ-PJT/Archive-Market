package com.csj.archive.market.simulation;

import com.csj.archive.market.capital.MarketCapitalService;
import com.csj.archive.market.claim.ReturnClaimService;
import com.csj.archive.market.common.BusinessException;
import com.csj.archive.market.common.IdGenerator;
import com.csj.archive.market.customer.CustomerService;
import com.csj.archive.market.order.CreateOrderRequest;
import com.csj.archive.market.order.MarketOrderEntity;
import com.csj.archive.market.order.MarketOrderService;
import com.csj.archive.market.payment.PaymentService;
import com.csj.archive.market.product.ProductEntity;
import com.csj.archive.market.product.ProductService;
import com.csj.archive.market.profitability.OrderProfitabilityService;
import com.csj.archive.market.revenue.MarketEconomyService;
import java.time.LocalDate;
import java.util.List;
import java.util.SplittableRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketSimulationService {

    private final CustomerService customerService;
    private final ProductService productService;
    private final MarketOrderService orderService;
    private final PaymentService paymentService;
    private final ReturnClaimService returnClaimService;
    private final MarketEconomyService economyService;
    private final OrderProfitabilityService profitabilityService;
    private final MarketCapitalService capitalService;

    public MarketSimulationService(CustomerService customerService, ProductService productService,
                                   MarketOrderService orderService, PaymentService paymentService,
                                   ReturnClaimService returnClaimService, MarketEconomyService economyService,
                                   OrderProfitabilityService profitabilityService, MarketCapitalService capitalService) {
        this.customerService = customerService;
        this.productService = productService;
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.returnClaimService = returnClaimService;
        this.economyService = economyService;
        this.profitabilityService = profitabilityService;
        this.capitalService = capitalService;
    }

    @Transactional
    public SimulationResult demand(int count) {
        validateCount(count);
        String simulationRunId = IdGenerator.prefixed("SIM");
        customerService.ensureCustomers(count);
        return new SimulationResult(simulationRunId, null, count, 0, 0, 0, 0, economyService.summary());
    }

    @Transactional
    public SimulationResult orders(int count) {
        validateCount(count);
        String simulationRunId = IdGenerator.prefixed("SIM");
        List<ProductEntity> products = productService.seed();
        var customers = customerService.ensureCustomers(Math.min(Math.max(count, 6), 100));
        SplittableRandom random = new SplittableRandom();
        int captured = 0;
        for (int i = 0; i < count; i++) {
            MarketOrderEntity order = orderService.create(new CreateOrderRequest(
                    customers.get(i % customers.size()).getCustomerId(),
                    products.get(i % products.size()).getProductId(),
                    1 + random.nextInt(5),
                    i % 9 == 0));
            orderService.confirm(order.getOrderId());
            paymentService.capture(order.getOrderId());
            captured++;
        }
        return new SimulationResult(simulationRunId, null, count, count, captured, 0, 0, economyService.summary());
    }

    @Transactional
    public SimulationResult runDay(LocalDate date) {
        int count = 100;
        SimulationResult orderResult = orders(count);
        List<MarketOrderEntity> orders = orderService.repository().findAll();
        int returns = 0;
        int claims = 0;
        for (int i = 0; i < orders.size(); i++) {
            if (i % 20 == 0) {
                returnClaimService.requestReturn(orders.get(i).getOrderId());
                returns++;
            }
            if (i % 37 == 0) {
                returnClaimService.createClaim(orders.get(i).getOrderId());
                claims++;
            }
        }
        economyService.dailyClose(date);
        return new SimulationResult(orderResult.simulationRunId(), date, count, count, count, returns, claims,
                economyService.summary());
    }

    @Transactional
    public SimulationResult profitability(int count) {
        validateCount(count);
        SimulationResult orderResult = orders(count);
        orderService.repository().findAll().stream()
                .limit(count)
                .forEach(order -> profitabilityService.evaluate(order.getOrderId(), orderResult.simulationRunId()));
        return new SimulationResult(orderResult.simulationRunId(), null, count, count, orderResult.paymentsCaptured(),
                0, 0, profitabilityService.summary());
    }

    @Transactional
    public SimulationResult runWorkday(LocalDate date) {
        SimulationResult result = orders(100);
        capitalService.runWorkday(date);
        return new SimulationResult(result.simulationRunId(), date, 100, result.ordersCreated(),
                result.paymentsCaptured(), 0, 0, capitalService.combinedSummary());
    }

    private void validateCount(int count) {
        if (count < 1 || count > 10_000) {
            throw new BusinessException("SIMULATION_COUNT_LIMIT", "count must be between 1 and 10000");
        }
    }
}
