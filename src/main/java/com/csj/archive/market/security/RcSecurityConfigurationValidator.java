package com.csj.archive.market.security;

import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
class RcSecurityConfigurationValidator {
    private final Environment environment; private final ArchiveSecurityProperties properties;
    RcSecurityConfigurationValidator(Environment environment, ArchiveSecurityProperties properties) { this.environment=environment; this.properties=properties; }
    @PostConstruct void validate() { if (java.util.Arrays.asList(environment.getActiveProfiles()).contains("rc") && (!properties.enabled() || !properties.configured())) throw new IllegalStateException("Market RC security requires read and admin tokens"); }
}
