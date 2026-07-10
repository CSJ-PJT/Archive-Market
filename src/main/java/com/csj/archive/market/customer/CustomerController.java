package com.csj.archive.market.customer;

import com.csj.archive.market.common.ApiResponse;
import com.csj.archive.market.common.PageResponse;
import com.csj.archive.market.common.TraceIdFilter;
import org.slf4j.MDC;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping
    ApiResponse<PageResponse<CustomerEntity>> customers(Pageable pageable) {
        return ApiResponse.ok(PageResponse.from(customerService.list(pageable)), MDC.get(TraceIdFilter.TRACE_ID));
    }
}
