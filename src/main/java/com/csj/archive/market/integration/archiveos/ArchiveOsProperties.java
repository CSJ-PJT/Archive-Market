package com.csj.archive.market.integration.archiveos;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "market.integration.archiveos")
public record ArchiveOsProperties(String baseUrl, String publishPath) {
}
