import { useEffect, useState } from "react";
import { api, ApiError, type Notice, type TodayDesk } from "../api";
import { useAuth } from "../auth";
import { Problem } from "../bits";

export function Notes() {
  const { user } = useAuth();
  const [sections, setSections] = useState<TodayDesk["sections"]>([]);
  const [sectionId, setSectionId] = useState<number | null>(null);
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [sent, setSent] = useState<Notice[]>([]);
  const [problem, setProblem] = useState("");
  const [busy, setBusy] = useState(false);

  function load() {
    Promise.all([api<TodayDesk>("/api/teacher/today"), api<Notice[]>("/api/notices")])
      .then(([desk, notes]) => {
        setSections(desk.sections);
        setSectionId((current) => current ?? desk.sections[0]?.sectionId ?? null);
        setSent(notes);
      })
      .catch((error: unknown) => setProblem(error instanceof ApiError ? error.message : "Notes did not load."));
  }

  useEffect(load, []);
  if (user && user.role !== "TEACHER") return <p className="quiet-line">Section notes are sent by a teacher.</p>;

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setBusy(true);
    setProblem("");
    try {
      await api("/api/notices", {
        method: "POST",
        body: JSON.stringify({ scope: "SECTION", sectionId, title, body })
      });
      setTitle("");
      setBody("");
      load();
    } catch (error) {
      setProblem(error instanceof ApiError ? error.message : "The note did not go.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div>
      <p className="eyebrow">A short note</p>
      <h1>To your section</h1>
      <p className="deck">Homework for the whole class belongs on For the class. This page is a note to one section.</p>
      <Problem message={problem} />
      <form className="composer" onSubmit={submit}>
        <label>
          Section
          <select value={sectionId ?? ""} onChange={(event) => setSectionId(Number(event.target.value))}>
            {sections.map((section) => (
              <option key={`${section.sectionId}-${section.subject}`} value={section.sectionId}>
                {section.gradeLabel} · {section.sectionName} · {section.subject}
              </option>
            ))}
          </select>
        </label>
        <label>
          Title
          <input value={title} onChange={(event) => setTitle(event.target.value)} />
        </label>
        <label>
          Note
          <textarea value={body} onChange={(event) => setBody(event.target.value)} rows={3} />
        </label>
        <button className="primary" type="submit" disabled={busy || sectionId == null}>
          {busy ? "Sending…" : "Send the note"}
        </button>
      </form>
      <NoticeList notes={sent} />
    </div>
  );
}

export function NoticeList({ notes }: { notes: Notice[] }) {
  if (notes.length === 0) return <p className="quiet-line">Nothing sent from this desk yet.</p>;
  return (
    <ul className="notes">
      {notes.map((note) => (
        <li key={note.id}>
          <div className="note-top">
            <strong>{note.title}</strong>
            <span>{note.targetLabel}</span>
          </div>
          <p>{note.body}</p>
          <small>
            {note.authorName}
            {" · "}
            {note.recipientCount === 0
              ? "No family on the demo is in this room yet."
              : `${note.seenCount} of ${note.recipientCount} on Openbell have opened it.`}
          </small>
        </li>
      ))}
    </ul>
  );
}
