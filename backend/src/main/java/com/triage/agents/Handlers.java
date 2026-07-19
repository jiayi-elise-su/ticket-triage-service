package com.triage.agents;

import org.springframework.stereotype.Component;

import java.util.Map;

/** Specialized handler agents: router picks one by category; each calls its own tools. */
@Component
public class Handlers {
    private final Tools tools;
    public Handlers(Tools tools) { this.tools = tools; }

    /** returns [assignedAgent, resultText, finalStatus] */
    public String[] handle(String category, String tenantId, String subject, String body) {
        return switch (category) {
            case "billing" -> {
                Map<String, Object> a = tools.lookupAccount(tenantId);
                yield new String[]{"billing",
                        "Billing: account " + a.get("accountId") + ", balance due $" + a.get("balanceDue") + ".",
                        "HANDLED"};
            }
            case "technical" -> new String[]{"technical",
                    "Technical: " + String.join(" | ", tools.searchKnownIssues(body)), "HANDLED"};
            case "refund" -> {
                Map<String, Object> a = tools.lookupAccount(tenantId);
                double due = (double) a.get("balanceDue");
                if (tools.refundEligible(due)) {
                    Map<String, Object> rf = tools.createRefund(due);
                    yield new String[]{"refund",
                            "Refund: " + rf.get("refundId") + " for $" + rf.get("amount") + " submitted.", "HANDLED"};
                }
                yield new String[]{"refund", "Refund exceeds auto-approve limit; escalating.", "NEEDS_HUMAN"};
            }
            default -> new String[]{"escalate", "Escalated to a human agent.", "NEEDS_HUMAN"};
        };
    }
}
