package com.csj.archive.market.integration.archiveos;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ArchiveOsClient {

    private final RestClient.Builder restClientBuilder;
    private final ArchiveOsProperties properties;

    public ArchiveOsClient(RestClient.Builder restClientBuilder, ArchiveOsProperties properties) {
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
