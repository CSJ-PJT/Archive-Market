package com.csj.archive.market.integration.archiveos;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;

@Component
public class ArchiveOsClient {

    private final RestClient.Builder restClientBuilder;
    private final ArchiveOsProperties properties;
    private final String internalIngestToken;

    public ArchiveOsClient(RestClient.Builder restClientBuilder, ArchiveOsProperties properties,
                           @Value("${archive.tokens.market-to-os:}") String internalIngestToken) {
        this.restClientBuilder = restClientBuilder;
        this.properties = properties;
        this.internalIngestToken = internalIngestToken;
    }

    public void publish(String payload) {
        if (internalIngestToken == null || internalIngestToken.isBlank()) {
            throw new ArchiveOsPublishException("CREDENTIAL_MISSING", false);
        }
        try {
            restClientBuilder.build()
                    .post()
                    .uri(properties.baseUrl() + properties.publishPath())
                    .contentType(MediaType.APPLICATION_JSON)
                    .headers(headers -> {
                        headers.setBearerAuth(internalIngestToken);
                        headers.set("X-Archive-Source-System", "archive-market");
                        headers.set("X-Archive-Service-Scope", "runtime:ingest");
                    })
                    .body(payload)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientResponseException ex) {
            int statusCode = ex.getStatusCode().value();
            if (ex.getResponseBodyAsString().contains("\"duplicate\":true")) {
                return;
            }
            if (statusCode == 401 || statusCode == 403) {
                throw new ArchiveOsPublishException("CONFIG_ERROR: ARCHIVEOS_HTTP_" + statusCode, false);
            }
            boolean retryable = statusCode == 408 || statusCode == 429 || statusCode >= 500;
            throw new ArchiveOsPublishException("ARCHIVEOS_HTTP_" + statusCode, retryable);
        } catch (ArchiveOsPublishException ex) {
            throw ex;
        } catch (RuntimeException ex) {
            throw new ArchiveOsPublishException("ARCHIVEOS_REQUEST_FAILED", false);
        }
    }
}
