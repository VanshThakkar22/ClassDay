import { useEffect, useState } from "react";
import { api, ApiError, type GradeOption, type Paper } from "../api";
import { useAuth } from "../auth";
import { PdfDrop, Problem, useReader } from "../bits";

export function Papers() {
  const { user } = useAuth();
  const { open, reader, problem: readProblem } = useReader();
  const [grades, setGrades] = useState<GradeOption[]>([]);
  const [papers, setPapers] = useState<Paper[]>([]);
  const [gradeId, setGradeId] = useState<number | null>(null);
  const [title, setTitle] = useState("");
  const [note, setNote] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [problem, setProblem] = useState("");
  const [busy, setBusy] = useState(false);

  function load() {
    Promise.all([api<GradeOption[]>("/api/teacher/grades"), api<Paper[]>("/api/teacher/papers")])
      .then(([nextGrades, nextPapers]) => {
        setGrades(nextGrades);
        setPapers(nextPapers);
        setGradeId((current) => current ?? nextGrades[0]?.gradeId ?? null);
      })
      .catch((error: unknown) => setProblem(error instanceof ApiError ? error.message : "The class list did not load."));
  }

  useEffect(load, []);

  if (user && user.role !== "TEACHER") return <p className="quiet-line">Class papers are sent by a teacher.</p>;

  async function send(event: React.FormEvent) {
    event.preventDefault();
    if (!file || gradeId == null) return;
    setBusy(true);
    setProblem("");
    const body = new FormData();
    body.set("gradeId", String(gradeId));
    body.set("title", title);
    body.set("note", note);
    body.set("file", file);
    try {
      await api("/api/teacher/papers", { method: "POST", body });
      setTitle("");
      setNote("");
      setFile(null);
      load();
    } catch (error) {
      setProblem(error instanceof ApiError ? error.message : "The paper did not go.");
    } finally {
      setBusy(false);
    }
  }

  const chosen = grades.find((grade) => grade.gradeId === gradeId);

  return (
    <div>
      <p className="eyebrow">Once, not five times</p>
      <h1>For every section</h1>
      <p className="deck">
        A sample paper uses the same syllabus in A, B, C, D, and E. Send it to the class once. It reaches every section.
      </p>
      <Problem message={problem || readProblem} />
      <div className="numbers">
        {grades.map((grade) => (
          <button
            key={grade.gradeId}
            type="button"
            className={grade.gradeId === gradeId ? "num on" : "num"}
            onClick={() => setGradeId(grade.gradeId)}
          >
            {grade.label.replace("Class ", "")}
          </button>
        ))}
      </div>
      <form className="composer" onSubmit={send}>
        <p className="quiet-line">{chosen ? `This goes to every section of ${chosen.label}.` : "You have no class on this demo."}</p>
        <label>
          Title
          <input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="Science sample paper" />
        </label>
        <label>
          A line for the student
          <textarea value={note} onChange={(event) => setNote(event.target.value)} rows={2} />
        </label>
        <PdfDrop file={file} onFile={setFile} idle="Drop the sample PDF" />
        <button className="primary" type="submit" disabled={busy || !file || !title.trim() || gradeId == null}>
          {busy ? "Sending…" : "Send once to the class"}
        </button>
      </form>
      <ul className="papers">
        {papers.map((paper) => (
          <li key={paper.id}>
            <button type="button" className="sheet-link" onClick={() => open(`/api/files/papers/${paper.id}`, paper.filename)}>
              {paper.gradeLabel} · {paper.title}
            </button>
            {paper.note && <p>{paper.note}</p>}
          </li>
        ))}
      </ul>
      {reader}
    </div>
  );
}
