package com.csj.archive.market.operations;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    Map<String, Object> health() {
        return Map.of("service", "Archive-Market", "status", "HEALTHY");
    }
}
