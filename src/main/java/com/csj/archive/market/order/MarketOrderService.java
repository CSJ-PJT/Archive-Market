package com.csj.archive.market.order;

import com.csj.archive.market.audit.AuditAction;
import com.csj.archive.market.audit.AuditLogService;
import com.csj.archive.market.common.BusinessException;
import com.csj.archive.market.common.IdGenerator;
import com.csj.archive.market.common.NotFoundException;
import com.csj.archive.market.customer.CustomerEntity;
import com.csj.archive.market.customer.CustomerRepository;
import com.csj.archive.market.customer.CustomerService;
import com.csj.archive.market.product.PricingPolicy;
import com.csj.archive.market.product.ProductEntity;
import com.csj.archive.market.product.ProductRepository;
import com.csj.archive.market.product.ProductService;
import com.csj.archive.market.profitability.OrderProfitabilityService;
import com.csj.archive.market.revenue.CostType;
import com.csj.archive.market.revenue.MarketEconomyService;
import com.csj.archive.market.revenue.RevenueType;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketOrderService {

    private final MarketOrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService;
    private final ProductService productService;
    private final PricingPolicy pricingPolicy;
    private final MarketEconomyService economyService;
    private final OrderProfitabilityService profitabilityService;
    private final AuditLogService auditLogService;

    public MarketOrderService(MarketOrderRepository orderRepository, CustomerRepository customerRepository,
                              ProductRepository productRepository, CustomerService customerService,
                              ProductService productService, PricingPolicy pricingPolicy,
                              MarketEconomyService economyService, OrderProfitabilityService profitabilityService,
                              AuditLogService auditLogService) {
        this.orderRepository = orderRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
        this.customerService = customerService;
        this.productService = productService;
        this.pricingPolicy = pricingPolicy;
        this.economyService = economyService;
        this.profitabilityService = profitabilityService;
        this.auditLogService = auditLogService;
    }

    @Transactional(readOnly = true)
    public Page<MarketOrderEntity> list(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public MarketOrderEntity get(String orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderId));
    }

    @Transactional
    public MarketOrderEntity create(CreateOrderRequest request) {
        if (productRepository.count() == 0) {
            productService.seed();
        }
        CustomerEntity customer = resolveCustomer(request.customerId());
        ProductEntity product = resolveProduct(request.productId());
        int quantity = request.quantity() == null ? 1 : request.quantity();
        String orderId = IdGenerator.prefixed("ORD");
        BigDecimal total = product.getBasePrice().multiply(BigDecimal.valueOf(quantity));
        BigDecimal discount = pricingPolicy.discountFor(customer.getCustomerType(), total);
        BigDecimal payment = total.subtract(discount);
        int risk = Math.min(100, customer.getRiskLevel() + (Boolean.TRUE.equals(request.express()) ? 10 : 0));
        MarketOrderEntity order = new MarketOrderEntity(
                orderId,
                customer.getCustomerId(),
                customer.getCustomerType(),
                total,
                discount,
                payment,
                "KRW",
                risk,
                risk >= 80);
        order.addItem(new MarketOrderItemEntity(
                orderId,
                product.getProductId(),
                product.getProductType().name(),
                quantity,
                product.getBasePrice(),
                product.getBaseCost(),
                total));
        MarketOrderEntity saved = orderRepository.save(order);
        String simulationRunId = IdGenerator.prefixed("SIM");
        profitabilityService.evaluate(saved.getOrderId(), simulationRunId);
        economyService.recordRevenue(RevenueType.CUSTOMER_DEMAND_CREATED, BigDecimal.ZERO, saved, simulationRunId,
                null, "Synthetic customer demand created");
        economyService.recordRevenue(RevenueType.SALES_ORDER_PLACED, BigDecimal.ZERO, saved, simulationRunId,
                null, "Synthetic sales order placed");
        economyService.recordCost(CostType.CUSTOMER_ACQUISITION_COST_INCURRED, BigDecimal.valueOf(5000), saved,
                simulationRunId, null, "Synthetic customer acquisition cost");
        if (discount.signum() > 0) {
            economyService.recordCost(CostType.DISCOUNT_COST_INCURRED, discount, saved, simulationRunId, null,
                    "Synthetic discount cost");
        }
        if (Boolean.TRUE.equals(request.express())) {
            economyService.recordRevenue(RevenueType.EXPRESS_ORDER_FEE_EARNED, BigDecimal.valueOf(15000), saved,
                    simulationRunId, null, "Synthetic express order fee");
        }
        economyService.enqueueOrderPlaced(saved, simulationRunId);
        auditLogService.record(AuditAction.ORDER_CREATED, "MARKET_ORDER", saved.getOrderId(), null,
                saved.getOrderStatus().name(), "Synthetic order created");
        return saved;
    }

    @Transactional
    public MarketOrderEntity confirm(String orderId) {
        MarketOrderEntity order = get(orderId);
        if (order.getOrderStatus() != OrderStatus.CREATED) {
            return order;
        }
        order.changeStatus(OrderStatus.CONFIRMED);
        economyService.recordRevenue(RevenueType.SALES_ORDER_CONFIRMED, BigDecimal.ZERO, order,
                IdGenerator.prefixed("SIM"), null, "Synthetic sales order confirmed");
        economyService.enqueueProductionAndShipment(order, IdGenerator.prefixed("SIM"));
        auditLogService.record(AuditAction.ORDER_CONFIRMED, "MARKET_ORDER", orderId, OrderStatus.CREATED.name(),
                OrderStatus.CONFIRMED.name(), "Order confirmed and Nexus requests enqueued");
        return order;
    }

    @Transactional
    public MarketOrderEntity cancel(String orderId) {
        MarketOrderEntity order = get(orderId);
        if (order.getOrderStatus() == OrderStatus.CANCELLED) {
            return order;
        }
        String before = order.getOrderStatus().name();
        order.changeStatus(OrderStatus.CANCELLED);
        economyService.enqueueGenericEvent("ORDER_CANCELLED", "MARKET_ORDER", order, IdGenerator.prefixed("SIM"),
                Map.of("orderId", order.getOrderId(), "reason", "Synthetic cancellation"));
        auditLogService.record(AuditAction.ORDER_CANCELLED, "MARKET_ORDER", orderId, before,
                OrderStatus.CANCELLED.name(), "Order cancelled");
        return order;
    }

    @Transactional
    public List<MarketOrderEntity> simulate(int count) {
        if (count < 1 || count > 10_000) {
            throw new BusinessException("SIMULATION_COUNT_LIMIT", "count must be between 1 and 10000");
        }
        productService.seed();
        List<CustomerEntity> customers = customerService.ensureCustomers(Math.min(Math.max(count, 6), 100));
        List<ProductEntity> products = productRepository.findByEnabledTrue();
        SplittableRandom random = new SplittableRandom();
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> create(new CreateOrderRequest(
                        customers.get(i % customers.size()).getCustomerId(),
                        products.get(i % products.size()).getProductId(),
                        1 + random.nextInt(5),
                        i % 9 == 0)))
                .toList();
    }

    public MarketOrderRepository repository() {
        return orderRepository;
    }

    private CustomerEntity resolveCustomer(String customerId) {
        if (customerId == null || customerId.isBlank()) {
            return customerService.createSynthetic((int) customerRepository.count());
        }
        return customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + customerId));
    }

    private ProductEntity resolveProduct(String productId) {
        if (productId == null || productId.isBlank()) {
            return productRepository.findByEnabledTrue().stream()
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("No enabled product"));
        }
        return productRepository.findByProductId(productId)
                .orElseThrow(() -> new NotFoundException("Product not found: " + productId));
    }
}
