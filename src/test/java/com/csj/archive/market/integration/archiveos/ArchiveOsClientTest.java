package com.csj.archive.market.integration.archiveos;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class ArchiveOsClientTest {

    @Test
    void transientConnectionFailureRemainsRetryable() {
        RestClient.Builder builder = RestClient.builder().requestFactory((uri, method) -> {
            throw new IOException("temporary connection failure");
        });
        ArchiveOsClient client = new ArchiveOsClient(
                builder,
                new ArchiveOsProperties("http://archiveos", "/api/live-flow/events"),
                "test-token");

        assertThatThrownBy(() -> client.publish("{}"))
                .isInstanceOf(ArchiveOsPublishException.class)
                .satisfies(error -> org.assertj.core.api.Assertions.assertThat(
                        ((ArchiveOsPublishException) error).isRetryable()).isTrue());
    }
}
