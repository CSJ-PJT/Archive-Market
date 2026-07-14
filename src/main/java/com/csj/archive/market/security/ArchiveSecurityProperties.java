package com.csj.archive.market.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ArchiveSecurityProperties {
    private final boolean enabled;
    private final long maxPayloadBytes;
    private final int maxBatchItems;
    private final int maxWritesPerMinute;
    private final List<String> allowedOrigins;
    private final String authenticatedReadToken;
    private final String adminOperatorToken;

    public ArchiveSecurityProperties(@Value("${archive.security.enabled:false}") boolean enabled,
                                     @Value("${archive.security.max-payload-bytes:1048576}") long maxPayloadBytes,
                                     @Value("${archive.security.max-batch-items:100}") int maxBatchItems,
                                     @Value("${archive.security.max-writes-per-minute:60}") int maxWritesPerMinute,
                                     @Value("${archive.security.allowed-origins:http://127.0.0.1:5173,http://localhost:5173}") String allowedOrigins,
                                     @Value("${archive.security.authenticated-read-token:}") String authenticatedReadToken,
                                     @Value("${archive.security.admin-operator-token:}") String adminOperatorToken) {
        this.enabled = enabled;
        this.maxPayloadBytes = Math.max(1024, maxPayloadBytes);
        this.maxBatchItems = Math.max(1, Math.min(500, maxBatchItems));
        this.maxWritesPerMinute = Math.max(1, maxWritesPerMinute);
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(",")).map(String::trim).filter(v -> !v.isBlank()).toList();
        this.authenticatedReadToken = authenticatedReadToken;
        this.adminOperatorToken = adminOperatorToken;
    }
    public boolean enabled() { return enabled; }
    public long maxPayloadBytes() { return maxPayloadBytes; }
    public int maxBatchItems() { return maxBatchItems; }
    public int maxWritesPerMinute() { return maxWritesPerMinute; }
    public List<String> allowedOrigins() { return allowedOrigins; }
    public String authenticatedReadToken() { return authenticatedReadToken; }
    public String adminOperatorToken() { return adminOperatorToken; }
    public boolean configured() { return !authenticatedReadToken.isBlank() && !adminOperatorToken.isBlank(); }
}
