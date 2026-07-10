package com.csj.archive.market.customer;

import com.csj.archive.market.common.IdGenerator;
import java.util.List;
import java.util.SplittableRandom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional(readOnly = true)
    public Page<CustomerEntity> list(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @Transactional
    public CustomerEntity createSynthetic(int seed) {
        SyntheticCustomer[] types = SyntheticCustomer.values();
        SyntheticCustomer type = types[Math.floorMod(seed, types.length)];
        int risk = switch (type) {
            case HIGH_RISK_CUSTOMER -> 85;
            case RETURN_PRONE_CUSTOMER -> 70;
            case DISCOUNT_SEEKER -> 45;
            case VIP_CUSTOMER -> 20;
            default -> 30;
        };
        return customerRepository.save(new CustomerEntity(
                IdGenerator.prefixed("CUST"),
                type,
                risk,
                "Synthetic " + type.name().replace('_', ' ') + " " + Math.abs(seed)));
    }

    @Transactional
    public List<CustomerEntity> ensureCustomers(int count) {
        if (customerRepository.count() >= count) {
            return customerRepository.findAll(Pageable.ofSize(count)).getContent();
        }
        SplittableRandom random = new SplittableRandom();
        while (customerRepository.count() < count) {
            createSynthetic(random.nextInt(100_000));
        }
        return customerRepository.findAll(Pageable.ofSize(count)).getContent();
    }
}
