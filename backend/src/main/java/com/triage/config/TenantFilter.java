package com.triage.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Every /api/** call must carry X-Tenant-Id; stashed in TenantContext for the request. */
@Component
@Order(1)
public class TenantFilter extends OncePerRequestFilter {
    public static final String HEADER = "X-Tenant-Id";

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        if (!req.getRequestURI().startsWith("/api/") || "OPTIONS".equalsIgnoreCase(req.getMethod())) {
            chain.doFilter(req, res);
            return;
        }
        String tenantId = req.getHeader(HEADER);
        if (tenantId == null || tenantId.isBlank()) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing " + HEADER + " header");
            return;
        }
        try {
            TenantContext.set(tenantId);
            chain.doFilter(req, res);
        } finally {
            TenantContext.clear();
        }
    }
}
