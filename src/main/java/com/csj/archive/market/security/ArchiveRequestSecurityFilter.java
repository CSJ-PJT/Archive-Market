package com.csj.archive.market.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** RC boundary: browser traffic cannot mutate Market without an admin token. */
@Component
public class ArchiveRequestSecurityFilter extends OncePerRequestFilter {
    private final ArchiveSecurityProperties properties;
    private final ConcurrentHashMap<String, Window> writeWindows = new ConcurrentHashMap<>();
    public ArchiveRequestSecurityFilter(ArchiveSecurityProperties properties) { this.properties = properties; }

    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (!properties.enabled() || HttpMethod.OPTIONS.matches(request.getMethod())) { chain.doFilter(request, response); return; }
        if (request.getContentLengthLong() > properties.maxPayloadBytes()) { response.sendError(413, "payload_too_large"); return; }
        if (isPublic(request)) { chain.doFilter(request, response); return; }
        String source = request.getHeader(ArchiveScopeRegistry.SOURCE_HEADER);
        String scope = request.getHeader(ArchiveScopeRegistry.SCOPE_HEADER);
        String token = bearer(request.getHeader("Authorization"));
        boolean read = HttpMethod.GET.matches(request.getMethod()) || HttpMethod.HEAD.matches(request.getMethod());
        String expectedScope = read ? ArchiveScopeRegistry.AUTHENTICATED_READ : ArchiveScopeRegistry.ADMIN_OPERATE;
        String expectedToken = read ? properties.authenticatedReadToken() : properties.adminOperatorToken();
        if (token == null || source == null || scope == null) { response.sendError(401, "service_credentials_required"); return; }
        if (!"archive-os".equals(source.trim().toLowerCase()) || !expectedScope.equals(canonicalScope(scope))) { response.sendError(403, "service_identity_or_scope_denied"); return; }
        if (!constantTimeEquals(expectedToken, token)) { response.sendError(401, "invalid_service_token"); return; }
        if (!read && !allow(request.getRequestURI())) { response.setHeader("Retry-After", "60"); response.sendError(429, "write_rate_limit_exceeded"); return; }
        chain.doFilter(request, response);
    }
    private boolean isPublic(HttpServletRequest request) { String p=request.getRequestURI(); return p.equals("/actuator/health") || p.equals("/actuator/health/liveness") || p.equals("/actuator/health/readiness") || (HttpMethod.GET.matches(request.getMethod()) && (p.equals("/api/health") || p.equals("/api/operations/summary"))); }
    private String canonicalScope(String scope) { String normalized=scope.trim(); return ArchiveScopeRegistry.LEGACY_SCOPE_ALIASES.getOrDefault(normalized, normalized); }
    private String bearer(String value) { if(value==null||!value.startsWith("Bearer ")) return null; String token=value.substring(7).trim(); return token.isBlank()?null:token; }
    private boolean constantTimeEquals(String expected,String supplied) { return expected!=null&&!expected.isBlank()&&MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), supplied.getBytes(StandardCharsets.UTF_8)); }
    private boolean allow(String path) { long minute=Instant.now().getEpochSecond()/60; Window w=writeWindows.compute(path,(key,current)->current==null||current.minute!=minute?new Window(minute,new AtomicInteger(1)):new Window(minute,new AtomicInteger(current.count.incrementAndGet()))); return w.count.get()<=properties.maxWritesPerMinute(); }
    private record Window(long minute, AtomicInteger count) { }
}
