import { useEffect, useState } from "react";
import { api, ApiError, clock, type CityPage } from "../api";
import { useAuth } from "../auth";
import { Problem } from "../bits";

export function City() {
  const { user } = useAuth();
  const [page, setPage] = useState<CityPage | null>(null);
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [problem, setProblem] = useState("");
  const [busy, setBusy] = useState(false);
  const [source, setSource] = useState(false);

  function load() {
    api<CityPage>("/api/city")
      .then(setPage)
      .catch((error: unknown) => setProblem(error instanceof ApiError ? error.message : "The city desk did not open."));
  }

  useEffect(load, []);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setBusy(true);
    setProblem("");
    try {
      await api("/api/city/events", { method: "POST", body: JSON.stringify({ title, body }) });
      setTitle("");
      setBody("");
      load();
    } catch (error) {
      setProblem(error instanceof ApiError ? error.message : "The event did not go.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div>
      <p className="eyebrow">Saharanpur · last 24 hours</p>
      <h1>The city</h1>
      <p className="deck">School notes stay in the diary. This page is the city, and the events schools share.</p>
      <Problem message={problem} />
      <div className="city-grid">
        <section>
          <div className="city-head">
            <h2>From the wire</h2>
            <button type="button" className="quiet" onClick={() => setSource(true)}>
              Connect a newspaper later
            </button>
          </div>
          {source && (
            <p className="held">
              The Python job already keeps these sample lines inside the last 24 hours. A paper you have the right to use replaces the sample text. This screen stays.
            </p>
          )}
          <ul className="wire">
            {page?.news.map((item) => (
              <li key={item.id}>
                <span className="stamp">Sample</span>
                <strong>{item.title}</strong>
                <p>{item.summary}</p>
                <small>
                  {item.sourceName} · {clock(item.publishedAt)}
                </small>
              </li>
            ))}
          </ul>
          {page && page.news.length === 0 && <p className="quiet-line">The city desk is still setting type.</p>}
        </section>
        <section>
          <h2>Between schools</h2>
          <ul className="notes">
            {page?.events.map((event) => (
              <li key={event.id}>
                <div className="note-top">
                  <strong>{event.title}</strong>
                  <span>{event.targetLabel}</span>
                </div>
                <p>{event.body}</p>
                <small>{event.authorName}</small>
              </li>
            ))}
          </ul>
          {user?.role === "PLATFORM_ADMIN" && (
            <form className="composer" onSubmit={submit}>
              <label>
                Event
                <input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="Inter-school quiz" />
              </label>
              <label>
                Detail
                <textarea value={body} onChange={(event) => setBody(event.target.value)} rows={3} />
              </label>
              <button className="primary" type="submit" disabled={busy}>
                {busy ? "Sending…" : "Post to every school"}
              </button>
            </form>
          )}
        </section>
      </div>
    </div>
  );
}
