package com.triage.agents;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/** Stubbed tools handlers call. Deterministic mock data so the flow runs offline. */
@Component
public class Tools {
    public Map<String, Object> lookupAccount(String tenantId) {
        return Map.of("accountId", "acct_" + (Math.abs(tenantId.hashCode()) % 10000), "balanceDue", 42.0);
    }
    public Map<String, Object> createRefund(double amount) {
        return Map.of("refundId", "rf_stub_001", "amount", amount, "status", "submitted");
    }
    public List<String> searchKnownIssues(String query) {
        return List.of("Clear cache and retry login.", "Known incident #1421: reset emails delayed ~10 min.");
    }
    public boolean refundEligible(double amount) { return amount <= 100.0; }
}
