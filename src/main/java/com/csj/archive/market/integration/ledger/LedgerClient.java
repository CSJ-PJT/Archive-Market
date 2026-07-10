package com.csj.archive.market.integration.ledger;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class LedgerClient {

    private final RestClient.Builder restClientBuilder;
    private final LedgerPublishProperties properties;

    public LedgerClient(RestClient.Builder restClientBuilder, LedgerPublishProperties properties) {
        this.restClientBuilder = restClientBuilder;
        this.properties = properties;
    }

    public void publish(String payload) {
        restClientBuilder.build()
                .post()
                .uri(properties.baseUrl() + properties.publishPath())
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
