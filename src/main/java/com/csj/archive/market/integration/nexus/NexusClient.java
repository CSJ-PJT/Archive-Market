package com.csj.archive.market.integration.nexus;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NexusClient {

    private final RestClient.Builder restClientBuilder;
    private final NexusPublishProperties properties;

    public NexusClient(RestClient.Builder restClientBuilder, NexusPublishProperties properties) {
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
