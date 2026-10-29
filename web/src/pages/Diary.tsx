import { useEffect, useState } from "react";
import { api, ApiError, shiftDate, type Diary as DiaryPage } from "../api";
import { useAuth } from "../auth";
import { Problem, useReader } from "../bits";

export function Diary() {
  const { user } = useAuth();
  const { open, reader, problem: readProblem } = useReader();
  const [page, setPage] = useState<DiaryPage | null>(null);
  const [date, setDate] = useState<string | null>(null);
  const [studentId, setStudentId] = useState<number | null>(null);
  const [problem, setProblem] = useState("");

  useEffect(() => {
    const params = new URLSearchParams();
    if (date) params.set("date", date);
    if (studentId) params.set("studentId", String(studentId));
    const query = params.toString();
    api<DiaryPage>(`/api/diary${query ? `?${query}` : ""}`)
      .then((next) => {
        setPage(next);
        setDate((current) => current ?? next.date);
        setStudentId((current) => current ?? next.selectedStudentId);
      })
      .catch((error: unknown) => setProblem(error instanceof ApiError ? error.message : "The diary did not open."));
  }, [date, studentId]);

  if (user && user.role !== "PARENT" && user.role !== "STUDENT") {
    return <p className="quiet-line">The diary opens for a student or a parent.</p>;
  }

  const student = page?.students.find((item) => item.id === page.selectedStudentId);
  const today = user?.serverDate;

  return (
    <div>
      <p className="eyebrow">Were you away?</p>
      <h1>{student ? student.name : "Diary"}</h1>
      {student && (
        <p className="deck">
          {student.gradeLabel} · Section {student.sectionName} · {student.schoolName}
        </p>
      )}
      <Problem message={problem || readProblem} />
      {page && date && (
        <div className="day-step">
          <button type="button" className="quiet" onClick={() => setDate(shiftDate(date, -1))}>
            Previous day
          </button>
          <strong>{page.dateLabel}</strong>
          <button type="button" className="quiet" disabled={!today || date >= today} onClick={() => setDate(shiftDate(date, 1))}>
            Next day
          </button>
        </div>
      )}
      {page && page.students.length > 1 && (
        <div className="numbers">
          {page.students.map((item) => (
            <button key={item.id} type="button" className={item.id === studentId ? "num on" : "num"} onClick={() => setStudentId(item.id)}>
              {item.name.split(" ")[0]}
            </button>
          ))}
        </div>
      )}
      <section>
        <h2>What the teachers sent</h2>
        {page && page.work.length === 0 && (
          <p className="quiet-line">A quiet day. Subjects with nothing to send stay off this page.</p>
        )}
        <ul className="papers">
          {page?.work.map((item) => (
            <li key={item.id}>
              <button type="button" className="sheet-link" onClick={() => open(`/api/files/daily/${item.id}`, item.filename)}>
                {item.subject} · {item.teacherName}
              </button>
              {item.note && <p>{item.note}</p>}
            </li>
          ))}
        </ul>
      </section>
      <section>
        <h2>For every section of the class</h2>
        {page && page.papers.length === 0 && <p className="quiet-line">No shared paper yet.</p>}
        <ul className="papers">
          {page?.papers.map((item) => (
            <li key={item.id}>
              <button type="button" className="sheet-link" onClick={() => open(`/api/files/papers/${item.id}`, item.filename)}>
                {item.title}
              </button>
              {item.note && <p>{item.note}</p>}
            </li>
          ))}
        </ul>
      </section>
      <section>
        <h2>Notes</h2>
        <ul className="notes">
          {page?.notices.map((note) => (
            <li key={note.id}>
              <div className="note-top">
                <strong>{note.title}</strong>
                <span>{note.targetLabel}</span>
              </div>
              <p>{note.body}</p>
              <small>{note.authorName}</small>
            </li>
          ))}
        </ul>
      </section>
      {reader}
    </div>
  );
}
