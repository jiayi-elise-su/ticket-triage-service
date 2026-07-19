import { Ticket } from "../api";

const PRIORITY_COLOR: Record<string, string> = {
  high: "#e5484d",
  medium: "#f5a623",
  low: "#3d9970",
};

export function TicketList({ tickets }: { tickets: Ticket[] }) {
  return (
    <table style={{ borderCollapse: "collapse", width: "100%" }}>
      <thead>
        <tr style={{ textAlign: "left", borderBottom: "1px solid #ccc" }}>
          <th>ID</th><th>Subject</th><th>Status</th><th>Category</th>
          <th>Priority</th><th>Agent</th><th>Result</th><th>Latency</th>
        </tr>
      </thead>
      <tbody>
        {tickets.map((t) => (
          <tr key={t.id} style={{ borderBottom: "1px solid #eee" }}>
            <td>{t.id}</td>
            <td>{t.subject || "—"}</td>
            <td>{t.status}</td>
            <td>{t.category ?? "…"}</td>
            <td style={{ color: t.priority ? PRIORITY_COLOR[t.priority] : "#999" }}>
              {t.priority ?? "…"}
            </td>
            <td>{t.assignedAgent ?? "…"}</td>
            <td style={{ maxWidth: 320 }}>{t.result ?? "…"}</td>
            <td>{t.latencyMs != null ? `${t.latencyMs}ms` : "…"}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
