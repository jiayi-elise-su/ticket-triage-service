import { useEffect, useState } from "react";
import { listTickets, Ticket } from "./api";
import { TicketForm } from "./components/TicketForm";
import { TicketList } from "./components/TicketList";

export default function App() {
  const [tenant, setTenant] = useState("acme");
  const [tickets, setTickets] = useState<Ticket[]>([]);

  async function refresh() {
    try {
      setTickets(await listTickets(tenant));
    } catch (e) {
      console.error(e);
    }
  }

  // poll every 2s so you can watch tickets move QUEUED -> TRIAGING -> HANDLED
  useEffect(() => {
    refresh();
    const id = setInterval(refresh, 2000);
    return () => clearInterval(id);
  }, [tenant]);

  return (
    <div style={{ fontFamily: "system-ui", padding: 24, maxWidth: 1000, margin: "0 auto" }}>
      <h1>Ticket Triage Dashboard</h1>
      <label>
        Tenant:{" "}
        <select value={tenant} onChange={(e) => setTenant(e.target.value)}>
          <option value="acme">acme</option>
          <option value="globex">globex</option>
        </select>
      </label>
      <div style={{ marginTop: 16 }}>
        <TicketForm tenant={tenant} onSubmitted={refresh} />
        <TicketList tickets={tickets} />
      </div>
    </div>
  );
}
