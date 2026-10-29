import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api, ApiError, type Notice, type SchoolTree } from "../api";
import { useAuth } from "../auth";
import { Problem } from "../bits";
import { Ladder } from "../ladder";
import { NoticeList } from "./Notes";

type Scope = "SECTION" | "GRADE" | "SCHOOL";

export function Office() {
  const { user } = useAuth();
  const [tree, setTree] = useState<SchoolTree | null>(null);
  const [gradeId, setGradeId] = useState<number | null>(null);
  const [sectionId, setSectionId] = useState<number | null>(null);
  const [scope, setScope] = useState<Scope>("SECTION");
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [notes, setNotes] = useState<Notice[]>([]);
  const [problem, setProblem] = useState("");
  const [busy, setBusy] = useState(false);
  const [cityNote, setCityNote] = useState(false);

  function load() {
    Promise.all([api<SchoolTree>("/api/office/tree"), api<Notice[]>("/api/notices")])
      .then(([nextTree, nextNotes]) => {
        setTree(nextTree);
        setNotes(nextNotes);
        const preferred = nextTree.grades.find((grade) => grade.level === 8) ?? nextTree.grades[0];
        setGradeId((current) => current ?? preferred?.id ?? null);
        setSectionId((current) => current ?? preferred?.sections[0]?.id ?? null);
      })
      .catch((error: unknown) => setProblem(error instanceof ApiError ? error.message : "The office did not open."));
  }

  useEffect(load, []);
  if (user && user.role !== "SCHOOL_ADMIN") return <p className="quiet-line">The office desk is for the school admin.</p>;

  const grade = tree?.grades.find((item) => item.id === gradeId);
  const section = grade?.sections.find((item) => item.id === sectionId);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setBusy(true);
    setProblem("");
    try {
      await api("/api/notices", {
        method: "POST",
        body: JSON.stringify({
          scope,
          sectionId: scope === "SECTION" ? sectionId : null,
          gradeId: scope === "GRADE" ? gradeId : null,
          title,
          body
        })
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

  const target =
    scope === "SCHOOL"
      ? "the whole school"
      : scope === "GRADE"
        ? `${grade?.label ?? "this class"}, every section`
        : `${grade?.label ?? ""} · ${section?.name ?? ""}`;

  return (
    <div>
      <p className="eyebrow">The school, not the accounts desk</p>
      <h1>{tree?.name ?? "Office"}</h1>
      <p className="deck">Classes 1 to 12 are here, five sections each. Fees, attendance, and exams stay on the shelf until you pull them in.</p>
      <Problem message={problem} />
      {tree && (
        <Ladder
          tree={tree}
          gradeId={gradeId}
          sectionId={sectionId}
          onGrade={(id) => {
            setGradeId(id);
            const next = tree.grades.find((item) => item.id === id);
            setSectionId(next?.sections[0]?.id ?? null);
          }}
          onSection={setSectionId}
        />
      )}
      <form className="composer" onSubmit={submit}>
        <div className="scopes">
          <button type="button" className={scope === "SECTION" ? "quiet on" : "quiet"} onClick={() => setScope("SECTION")}>
            This section
          </button>
          <button type="button" className={scope === "GRADE" ? "quiet on" : "quiet"} onClick={() => setScope("GRADE")}>
            This class, every section
          </button>
          <button type="button" className={scope === "SCHOOL" ? "quiet on" : "quiet"} onClick={() => setScope("SCHOOL")}>
            Whole school
          </button>
          <button type="button" className="quiet" onClick={() => setCityNote(true)}>
            Every school in the city
          </button>
        </div>
        {cityNote && (
          <p className="held">
            Speaking to every school is the City plan. {tree?.name ?? "This school"} is on Campus, so a note stops at the gate.{" "}
            <Link to="/later">See the plans</Link>
          </p>
        )}
        <p className="quiet-line">This note goes to {target}.</p>
        <label>
          Title
          <input value={title} onChange={(event) => setTitle(event.target.value)} />
        </label>
        <label>
          Note
          <textarea value={body} onChange={(event) => setBody(event.target.value)} rows={3} />
        </label>
        <button className="primary" type="submit" disabled={busy}>
          {busy ? "Sending…" : "Send the note"}
        </button>
      </form>
      <h2>Already sent</h2>
      <NoticeList notes={notes} />
      <div className="shelf-row">
        {["Fees", "Attendance", "Exams", "Transport"].map((name) => (
          <Link key={name} className="later-link" to="/later">
            {name}
            <small>Pull in later</small>
          </Link>
        ))}
      </div>
    </div>
  );
}
