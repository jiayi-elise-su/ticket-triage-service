// Minimal API client. Tenant is sent via the X-Tenant-Id header on every call.
const BASE = "http://localhost:8080";

export interface Ticket {
  id: number;
  subject: string;
  body: string;
  category: string | null;
  priority: string | null;
  assignedAgent: string | null;
  result: string | null;
  latencyMs: number | null;
  status: string;
  createdAt: string;
}

function headers(tenant: string) {
  return { "Content-Type": "application/json", "X-Tenant-Id": tenant };
}

export async function submitTicket(tenant: string, subject: string, body: string) {
  const res = await fetch(`${BASE}/api/tickets`, {
    method: "POST",
    headers: headers(tenant),
    body: JSON.stringify({ subject, body }),
  });
  if (!res.ok) throw new Error(`submit failed: ${res.status}`);
  return res.json();
}

export async function listTickets(tenant: string): Promise<Ticket[]> {
  const res = await fetch(`${BASE}/api/tickets?limit=50`, { headers: headers(tenant) });
  if (!res.ok) throw new Error(`list failed: ${res.status}`);
  const page = await res.json();
  return page.items;
}
