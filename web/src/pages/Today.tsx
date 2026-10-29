import { useEffect, useState } from "react";
import { api, ApiError, type TodayDesk } from "../api";
import { PdfDrop, Problem, useReader } from "../bits";
import { useAuth } from "../auth";

export function Today() {
  const { user } = useAuth();
  const { open, reader, problem: readProblem } = useReader();
  const [desk, setDesk] = useState<TodayDesk | null>(null);
  const [problem, setProblem] = useState("");
  const [pending, setPending] = useState<number | null>(null);

  function load() {
    api<TodayDesk>("/api/teacher/today")
      .then(setDesk)
      .catch((error: unknown) => setProblem(error instanceof ApiError ? error.message : "Today did not load."));
  }

  useEffect(load, []);

  if (user && user.role !== "TEACHER") {
    return <p className="quiet-line">Today’s desk is for a teacher.</p>;
  }

  return (
    <div>
      <p className="eyebrow">After the last bell</p>
      <h1>Today</h1>
      <p className="deck">
        {user?.name.split(" ")[0]}, send the period home, or leave it blank. Then you are done. You do not reply one by one.
      </p>
      <Problem message={problem || readProblem} />
      {desk && <p className="date-line">{desk.dateLabel}</p>}
      <div className="stack">
        {desk?.sections.map((section) => (
          <Period
            key={`${section.sectionId}-${section.subject}`}
            section={section}
            busy={pending === section.sectionId}
            onOpen={open}
            onSent={() => {
              setPending(null);
              load();
            }}
            onBusy={() => setPending(section.sectionId)}
            onFail={(message) => {
              setPending(null);
              setProblem(message);
            }}
          />
        ))}
      </div>
      {reader}
    </div>
  );
}

function Period({
  section,
  busy,
  onOpen,
  onSent,
  onBusy,
  onFail
}: {
  section: TodayDesk["sections"][number];
  busy: boolean;
  onOpen: (path: string, title: string) => void;
  onSent: () => void;
  onBusy: () => void;
  onFail: (message: string) => void;
}) {
  const [file, setFile] = useState<File | null>(null);
  const [note, setNote] = useState(section.mine?.note ?? "");

  async function send(event: React.FormEvent) {
    event.preventDefault();
    if (!file) return;
    onBusy();
    const body = new FormData();
    body.set("sectionId", String(section.sectionId));
    body.set("note", note);
    body.set("file", file);
    try {
      await api("/api/teacher/daily-work", { method: "POST", body });
      setFile(null);
      onSent();
    } catch (error) {
      onFail(error instanceof ApiError ? error.message : "The sheet did not go.");
    }
  }

  return (
    <article className="period">
      <header>
        <h2>
          {section.gradeLabel} · {section.sectionName}
        </h2>
        <span>{section.subject}</span>
      </header>
      {section.mine ? (
        <div className="sent">
          <button type="button" className="sheet-link" onClick={() => onOpen(`/api/files/daily/${section.mine!.id}`, section.mine!.filename)}>
            Sent · {section.mine.filename}
          </button>
          {section.mine.note && <p>{section.mine.note}</p>}
        </div>
      ) : (
        <p className="quiet-line">Nothing from you yet. Leave it if the period needs nothing.</p>
      )}
      <form onSubmit={send}>
        <label>
          A line for someone who was away
          <textarea value={note} onChange={(event) => setNote(event.target.value)} rows={2} />
        </label>
        <PdfDrop file={file} onFile={setFile} idle={section.mine ? "Drop a different PDF to replace today’s" : "Drop today’s PDF"} />
        <button className="primary" type="submit" disabled={!file || busy}>
          {busy ? "Sending…" : section.mine ? "Replace today’s sheet" : "Send this sheet"}
        </button>
      </form>
      {section.colleagues.length > 0 && (
        <ul className="colleagues">
          {section.colleagues.map((person) => (
            <li key={`${person.teacherName}-${person.subject}`}>
              {person.fileId ? (
                <button type="button" onClick={() => onOpen(`/api/files/daily/${person.fileId}`, person.filename || person.subject)}>
                  {person.teacherName} sent {person.subject}
                </button>
              ) : (
                <span>
                  {person.teacherName} has not sent {person.subject}. That is allowed.
                </span>
              )}
            </li>
          ))}
        </ul>
      )}
    </article>
  );
}
