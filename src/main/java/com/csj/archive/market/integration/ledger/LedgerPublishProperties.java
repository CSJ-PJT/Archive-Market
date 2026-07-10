package com.csj.archive.market.integration.ledger;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "market.integration.ledger")
public record LedgerPublishProperties(String baseUrl, String publishPath) {
}
