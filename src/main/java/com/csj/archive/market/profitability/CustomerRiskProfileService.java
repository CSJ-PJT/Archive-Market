package com.csj.archive.market.profitability;

import com.csj.archive.market.claim.MarketClaimRepository;
import com.csj.archive.market.claim.MarketReturnRepository;
import com.csj.archive.market.common.NotFoundException;
import com.csj.archive.market.customer.CustomerEntity;
import com.csj.archive.market.customer.CustomerRepository;
import com.csj.archive.market.customer.SyntheticCustomer;
import com.csj.archive.market.order.MarketOrderRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerRiskProfileService {

    private final CustomerRiskProfileRepository riskProfileRepository;
    private final CustomerRepository customerRepository;
    private final MarketOrderRepository orderRepository;
    private final MarketReturnRepository returnRepository;
    private final MarketClaimRepository claimRepository;

    public CustomerRiskProfileService(CustomerRiskProfileRepository riskProfileRepository,
                                      CustomerRepository customerRepository,
                                      MarketOrderRepository orderRepository,
                                      MarketReturnRepository returnRepository,
                                      MarketClaimRepository claimRepository) {
        this.riskProfileRepository = riskProfileRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.returnRepository = returnRepository;
        this.claimRepository = claimRepository;
    }

    @Transactional(readOnly = true)
    public CustomerRiskProfileEntity get(String customerId) {
        return riskProfileRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new NotFoundException("Customer risk profile not found: " + customerId));
    }

    @Transactional
    public CustomerRiskProfileEntity recalculate(String customerId) {
        CustomerEntity customer = customerRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + customerId));
        Set<String> customerOrderIds = orderRepository.findAll().stream()
                .filter(order -> order.getCustomerId().equals(customerId))
                .map(order -> order.getOrderId())
                .collect(Collectors.toSet());
        long orderCount = customerOrderIds.size();
        long returnCount = returnRepository.findAll().stream()
                .filter(item -> customerOrderIds.contains(item.getOrderId()))
                .count();
        long claimCount = claimRepository.findAll().stream()
                .filter(item -> customerOrderIds.contains(item.getOrderId()))
                .count();
        RiskDefaults defaults = defaults(customer.getCustomerType());
        BigDecimal observedReturn = observed(returnCount, orderCount);
        BigDecimal observedClaim = observed(claimCount, orderCount);
        BigDecimal returnProbability = defaults.returnProbability().max(observedReturn);
        BigDecimal claimProbability = defaults.claimProbability().max(observedClaim);
        BigDecimal ltv = BigDecimal.valueOf(1_000_000L + (long) Math.max(1, orderCount) * 250_000L);
        CustomerRiskProfileEntity profile = riskProfileRepository.findByCustomerId(customerId)
                .orElseGet(() -> new CustomerRiskProfileEntity(
                        customerId,
                        customer.getCustomerType(),
                        customer.getRiskLevel(),
                        returnProbability,
                        claimProbability,
                        defaults.discountSensitivity(),
                        ltv,
                        orderCount,
                        returnCount,
                        claimCount));
        profile.recalculate(customer.getRiskLevel(), returnProbability, claimProbability,
                defaults.discountSensitivity(), ltv, orderCount, returnCount, claimCount);
        return riskProfileRepository.save(profile);
    }

    private RiskDefaults defaults(SyntheticCustomer customerType) {
        return switch (customerType) {
            case HIGH_RISK_CUSTOMER -> new RiskDefaults(bd("0.32"), bd("0.24"), bd("0.35"));
            case RETURN_PRONE_CUSTOMER -> new RiskDefaults(bd("0.40"), bd("0.16"), bd("0.25"));
            case DISCOUNT_SEEKER -> new RiskDefaults(bd("0.18"), bd("0.10"), bd("0.70"));
            case VIP_CUSTOMER -> new RiskDefaults(bd("0.08"), bd("0.06"), bd("0.30"));
            case B2B_CUSTOMER -> new RiskDefaults(bd("0.06"), bd("0.05"), bd("0.20"));
            case RETAIL_CUSTOMER -> new RiskDefaults(bd("0.10"), bd("0.07"), bd("0.25"));
        };
    }

    private BigDecimal observed(long count, long total) {
        if (total == 0) {
            return BigDecimal.ZERO;
        }
        return BigDecimal.valueOf(count).divide(BigDecimal.valueOf(total), 4, RoundingMode.HALF_UP);
    }

    private BigDecimal bd(String value) {
        return new BigDecimal(value);
    }

    private record RiskDefaults(BigDecimal returnProbability, BigDecimal claimProbability,
                                BigDecimal discountSensitivity) {
    }
}
