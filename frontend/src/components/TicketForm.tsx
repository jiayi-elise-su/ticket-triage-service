import { useState } from "react";
import { submitTicket } from "../api";

export function TicketForm({ tenant, onSubmitted }: { tenant: string; onSubmitted: () => void }) {
  const [subject, setSubject] = useState("");
  const [body, setBody] = useState("");
  const [busy, setBusy] = useState(false);

  async function handleSubmit() {
    if (!body.trim()) return;
    setBusy(true);
    try {
      await submitTicket(tenant, subject, body);
      setSubject("");
      setBody("");
      onSubmitted();
    } finally {
      setBusy(false);
    }
  }

  return (
    <div style={{ display: "grid", gap: 8, maxWidth: 480, marginBottom: 24 }}>
      <input
        placeholder="Subject"
        value={subject}
        onChange={(e) => setSubject(e.target.value)}
      />
      <textarea
        placeholder="Describe the issue…"
        rows={3}
        value={body}
        onChange={(e) => setBody(e.target.value)}
      />
      <button onClick={handleSubmit} disabled={busy || !body.trim()}>
        {busy ? "Submitting…" : "Submit ticket"}
      </button>
    </div>
  );
}
