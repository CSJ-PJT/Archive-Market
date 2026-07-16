package com.csj.archive.market.integration.nexus;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;

@Component
public class NexusClient {

    private final RestClient.Builder restClientBuilder;
    private final NexusPublishProperties properties;
    private final String token;

    public NexusClient(RestClient.Builder restClientBuilder, NexusPublishProperties properties, @Value("${archive.tokens.market-to-nexus:}") String token) {
        this.restClientBuilder = restClientBuilder;
        this.properties = properties;
        this.token = token;
    }

    public void publish(String payload) {
        restClientBuilder.build()
                .post()
                .uri(properties.baseUrl() + properties.publishPath())
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> { headers.setBearerAuth(token); headers.set("X-Archive-Source-System", "archive-market"); headers.set("X-Archive-Service-Scope", "production:ingest"); })
                .body(payload)
                .retrieve()
                .toBodilessEntity();
    }
}
