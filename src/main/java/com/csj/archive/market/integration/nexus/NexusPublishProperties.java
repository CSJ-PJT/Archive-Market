package com.csj.archive.market.integration.nexus;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "market.integration.nexus")
public record NexusPublishProperties(String baseUrl, String publishPath) {
}
