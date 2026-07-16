package com.csj.archive.market.security;

import java.util.Map;

/** Canonical Archive service identity and scope contract. Values match every Archive service. */
public final class ArchiveScopeRegistry {
    public static final String SOURCE_HEADER = "X-Archive-Source-System";
    public static final String SCOPE_HEADER = "X-Archive-Service-Scope";
    public static final String PRODUCTION_INGEST = "production:ingest";
    public static final String LOGISTICS_INGEST = "logistics:ingest";
    public static final String LEDGER_INGEST = "ledger:ingest";
    public static final String RUNTIME_INGEST = "runtime:ingest";
    public static final String LEDGER_APPROVAL_CALLBACK = "ledger:approval-callback";
    public static final String AUTHENTICATED_READ = "authenticated:read";
    public static final String ADMIN_OPERATE = "admin:operate";
    public static final Map<String, String> LEGACY_SCOPE_ALIASES = Map.of("ledger:read", AUTHENTICATED_READ, "runtime:read", AUTHENTICATED_READ);
    private ArchiveScopeRegistry() { }
}
