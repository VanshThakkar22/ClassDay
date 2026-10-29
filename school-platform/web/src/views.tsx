import { FormEvent, useEffect, useState } from "react";
import {
  DailyFile,
  GradeBlock,
  Home,
  NewsItem,
  Notice,
  PaperFile,
  TeacherCard,
  clock,
  login,
  openPdf,
  postNotice,
  uploadDaily,
  uploadPaper,
} from "./api";

const DEMOS = [
  ["Teacher, two sections", "meera.sharma@classday.local"],
  ["Second teacher, same section", "arjun.verma@classday.local"],
  ["Student who can catch up", "aarav@classday.local"],
  ["Parent", "parent.aarav@classday.local"],
  ["School office", "admin.north@classday.local"],
  ["Other school teacher", "neha.kapoor@classday.local"],
  ["City desk", "platform@classday.local"],
] as const;

export function Login({ onSuccess }: { onSuccess: (token: string) => void }) {
  const [email, setEmail] = useState("meera.sharma@classday.local");
  const [password, setPassword] = useState("demo1234");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function submit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const result = await login(email, password);
      onSuccess(result.token);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not sign in");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="login-grid">
      <form className="card stack" onSubmit={submit}>
        <p className="brand">Classday</p>
        <h2>The school day, in one place.</h2>
        <p className="focus">Two Saharanpur schools. Classes 1 to 12. Five sections each. A teacher files one PDF and leaves.</p>
        <label>
          Email
          <input value={email} onChange={(event) => setEmail(event.target.value)} autoComplete="username" />
        </label>
        <label>
          Password
          <input type="password" value={password} onChange={(event) => setPassword(event.target.value)} autoComplete="current-password" />
        </label>
        {error ? <p className="error">{error}</p> : null}
        <button className="solid" disabled={busy} type="submit">{busy ? "Signing in…" : "Sign in"}</button>
      </form>
      <div className="card stack">
        <h3>Open a demo desk</h3>
        <p className="muted tiny">Password for every account is demo1234.</p>
        <div className="demos">
          {DEMOS.map(([label, account]) => (
            <button
              key={account}
              type="button"
              onClick={() => {
                setEmail(account);
                setPassword("demo1234");
              }}
            >
              <strong>{label}</strong>
              <div className="tiny muted">{account}</div>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}

export function TeacherHome({
  home,
  token,
  date,
  onDate,
  onChanged,
}: {
  home: Home;
  token: string;
  date: string;
  onDate: (date: string) => void;
  onChanged: () => void;
}) {
  return (
    <div className="stack">
      <section className="card stack">
        <p className="brand">End of the day</p>
        <h2>{home.user.schoolName}</h2>
        <p className="focus">{home.focus}</p>
        <label>
          Day
          <input className="date" type="date" value={date} onChange={(event) => onDate(event.target.value)} />
        </label>
      </section>
      {home.teacherCards.map((card) => (
        <TeacherCardView key={`${card.sectionId}-${card.subject}`} card={card} token={token} date={date} onChanged={onChanged} />
      ))}
      <PaperList papers={home.classPapers} token={token} />
      <ClassPaperForm grades={[...new Map(home.teacherCards.map((card) => [card.gradeId, card.classLevel])).entries()]} token={token} onChanged={onChanged} />
    </div>
  );
}

function TeacherCardView({
  card,
  token,
  date,
  onChanged,
}: {
  card: TeacherCard;
  token: string;
  date: string;
  onChanged: () => void;
}) {
  const skipKey = `skip:${date}:${card.sectionId}:${card.subject}`;
  const [note, setNote] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [skipped, setSkipped] = useState(localStorage.getItem(skipKey) === "1");
  useEffect(() => {
    setSkipped(localStorage.getItem(skipKey) === "1");
  }, [skipKey]);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  async function save(event: FormEvent) {
    event.preventDefault();
    if (!file) {
      setError("Choose a PDF, or skip the day.");
      return;
    }
    setBusy(true);
    setError("");
    try {
      await uploadDaily(token, card.sectionId, card.subject, note, date, file);
      localStorage.removeItem(skipKey);
      setSkipped(false);
      setMessage("Saved. You are done for this section.");
      setFile(null);
      onChanged();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Upload failed");
    } finally {
      setBusy(false);
    }
  }

  return (
    <article className="card stack">
      <h3>{card.classLabel}</h3>
      <p className="muted">{card.subject}</p>
      {card.today ? (
        <div className="file-line">
          <div>
            <strong>{card.today.fileName}</strong>
            <div className="tiny muted">{card.today.note || "No note"} · {clock(card.today.uploadedAt)}</div>
          </div>
          <button className="ghost" type="button" onClick={() => openPdf(token, `/api/files/daily/${card.today!.id}`)}>Open PDF</button>
        </div>
      ) : skipped ? (
        <p className="ok">Nothing shared. No file was stored, and nothing else is waiting.</p>
      ) : (
        <p className="muted">No PDF yet. Upload one, or leave the day empty.</p>
      )}
      <form className="stack" onSubmit={save}>
        <label>
          Note for an absent student
          <input value={note} maxLength={280} onChange={(event) => setNote(event.target.value)} placeholder="Optional, one line" />
        </label>
        <input
          type="file"
          accept="application/pdf,.pdf"
          onChange={(event) => setFile(event.target.files?.[0] ?? null)}
        />
        {error ? <p className="error">{error}</p> : null}
        {message ? <p className="ok">{message}</p> : null}
        <div className="row">
          <button className="solid" disabled={busy} type="submit">{card.today ? "Replace PDF" : "Upload PDF"}</button>
          <button
            className="textish"
            type="button"
            onClick={() => {
              localStorage.setItem(skipKey, "1");
              setSkipped(true);
              setMessage("");
              setError("");
            }}
          >
            Nothing happened today
          </button>
        </div>
      </form>
    </article>
  );
}

export function FamilyHome({ home, token, date, onDate }: { home: Home; token: string; date: string; onDate: (date: string) => void }) {
  return (
    <div className="stack">
      <section className="card stack">
        <p className="brand">What happened in class</p>
        <h2>{home.sectionLabel || home.user.schoolName}</h2>
        <p className="focus">{home.focus}</p>
        <label>
          Day
          <input className="date" type="date" value={date} onChange={(event) => onDate(event.target.value)} />
        </label>
      </section>
      <section className="card stack">
        <h3>Today's files</h3>
        {home.todayWork.length === 0 ? (
          <p className="muted">Nothing was uploaded for this day. That is a normal day, not a missing assignment.</p>
        ) : (
          home.todayWork.map((file) => <DailyRow key={file.id} file={file} token={token} />)
        )}
      </section>
      <PaperList papers={home.classPapers} token={token} />
      <NoticeList notices={home.notices} />
      <CityBoard items={home.cityNews} />
    </div>
  );
}

function DailyRow({ file, token }: { file: DailyFile; token: string }) {
  return (
    <div className="file-line">
      <div>
        <strong>{file.subject}</strong>
        <div className="tiny muted">{file.teacherName}{file.note ? ` · ${file.note}` : ""}</div>
      </div>
      <button className="ghost" type="button" onClick={() => openPdf(token, `/api/files/daily/${file.id}`)}>Open PDF</button>
    </div>
  );
}

function PaperList({ papers, token }: { papers: PaperFile[]; token: string }) {
  if (papers.length === 0) return null;
  return (
    <section className="card stack">
      <h3>Sample papers for the whole class</h3>
      <p className="tiny muted">Sent once. Every section of that class can open it.</p>
      {papers.map((paper) => (
        <div className="file-line" key={paper.id}>
          <div>
            <strong>{paper.title}</strong>
            <div className="tiny muted">{paper.classLabel} · {paper.uploadedBy}</div>
          </div>
          <button className="ghost" type="button" onClick={() => openPdf(token, `/api/files/papers/${paper.id}`)}>Open PDF</button>
        </div>
      ))}
    </section>
  );
}

export function AdminHome({ home, token, onChanged }: { home: Home; token: string; onChanged: () => void }) {
  return (
    <div className="stack">
      <section className="card stack">
        <p className="brand">School office</p>
        <h2>{home.user.schoolName}</h2>
        <p className="focus">{home.focus}</p>
        <p className="tiny muted">Classes 1–12, sections A–E. Most sections have no students yet. The demo student is in Class 8-A at North Ridge, and Class 10-A at Riverbank.</p>
      </section>
      <section className="card">
        <h3>Classes</h3>
        {home.grades.map((grade) => (
          <GradeDetails key={grade.gradeId} grade={grade} />
        ))}
      </section>
      <NoticeComposer home={home} token={token} onChanged={onChanged} />
      <ClassPaperForm grades={home.grades.map((grade) => [grade.gradeId, grade.level] as [number, number])} token={token} onChanged={onChanged} />
      <PaperList papers={home.classPapers} token={token} />
      <NoticeList notices={home.notices} showReceipts />
      <CityBoard items={home.cityNews} />
    </div>
  );
}

function GradeDetails({ grade }: { grade: GradeBlock }) {
  const active = grade.sections.some((section) => section.teachers.length > 0 || section.studentCount > 0);
  return (
    <details open={active}>
      <summary>Class {grade.level}</summary>
      {grade.sections.map((section) => (
        <div className="section-line" key={section.sectionId}>
          <strong>{section.name}</strong>
          <span>{section.teachers.length ? section.teachers.join(", ") : "No teacher yet"}{section.studentCount ? ` · ${section.studentCount} student` : ""}</span>
          <span className="muted">{section.filesToday ? `${section.filesToday} PDF` : "Nothing shared"}</span>
        </div>
      ))}
    </details>
  );
}

function NoticeComposer({ home, token, onChanged }: { home: Home; token: string; onChanged: () => void }) {
  const [scope, setScope] = useState("SCHOOL");
  const [sectionId, setSectionId] = useState<number | "">("");
  const [gradeId, setGradeId] = useState<number | "">("");
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [error, setError] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      await postNotice(token, {
        scope,
        sectionId: sectionId === "" ? undefined : sectionId,
        gradeId: gradeId === "" ? undefined : gradeId,
        title,
        body,
      });
      setTitle("");
      setBody("");
      onChanged();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not send");
    }
  }

  return (
    <form className="card stack" onSubmit={submit}>
      <h3>Send a notice once</h3>
      <label>
        Reach
        <select value={scope} onChange={(event) => setScope(event.target.value)}>
          <option value="SECTION">One section</option>
          <option value="GRADE">One class, every section</option>
          <option value="SCHOOL">Whole school</option>
        </select>
      </label>
      {scope === "SECTION" ? (
        <label>
          Section
          <select value={sectionId} onChange={(event) => setSectionId(Number(event.target.value))}>
            <option value="">Choose</option>
            {home.grades.flatMap((grade) =>
              grade.sections.map((section) => (
                <option key={section.sectionId} value={section.sectionId}>
                  Class {grade.level} · {section.name}
                </option>
              )),
            )}
          </select>
        </label>
      ) : null}
      {scope === "GRADE" ? (
        <label>
          Class
          <select value={gradeId} onChange={(event) => setGradeId(Number(event.target.value))}>
            <option value="">Choose</option>
            {home.grades.map((grade) => (
              <option key={grade.gradeId} value={grade.gradeId}>Class {grade.level}</option>
            ))}
          </select>
        </label>
      ) : null}
      <label>
        Title
        <input value={title} onChange={(event) => setTitle(event.target.value)} />
      </label>
      <label>
        Message
        <textarea value={body} onChange={(event) => setBody(event.target.value)} />
      </label>
      {error ? <p className="error">{error}</p> : null}
      <button className="solid" type="submit">Send notice</button>
    </form>
  );
}

function ClassPaperForm({
  grades,
  token,
  onChanged,
}: {
  grades: [number, number][];
  token: string;
  onChanged: () => void;
}) {
  const [gradeId, setGradeId] = useState<number | "">(grades[0]?.[0] ?? "");
  const [title, setTitle] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (gradeId === "" || !file) {
      setError("Choose the class and a PDF.");
      return;
    }
    setError("");
    try {
      await uploadPaper(token, gradeId, title, file);
      setMessage("Sent once to every section of that class.");
      setTitle("");
      onChanged();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not send");
    }
  }

  if (grades.length === 0) return null;
  return (
    <form className="card stack" onSubmit={submit}>
      <h3>Sample paper for a whole class</h3>
      <p className="tiny muted">The syllabus is the same in every section, so this is uploaded once.</p>
      <label>
        Class
        <select value={gradeId} onChange={(event) => setGradeId(Number(event.target.value))}>
          {grades.map(([id, level]) => (
            <option key={id} value={id}>Class {level}</option>
          ))}
        </select>
      </label>
      <label>
        Title
        <input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="Class 10 mathematics sample paper" />
      </label>
      <input type="file" accept="application/pdf,.pdf" onChange={(event) => setFile(event.target.files?.[0] ?? null)} />
      {error ? <p className="error">{error}</p> : null}
      {message ? <p className="ok">{message}</p> : null}
      <button className="solid" type="submit">Send to every section</button>
    </form>
  );
}

export function PlatformHome({ home, token, onChanged }: { home: Home; token: string; onChanged: () => void }) {
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [error, setError] = useState("");

  async function submit(event: FormEvent) {
    event.preventDefault();
    setError("");
    try {
      await postNotice(token, { scope: "CITY", title, body });
      setTitle("");
      setBody("");
      onChanged();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not post");
    }
  }

  return (
    <div className="stack">
      <section className="card stack">
        <p className="brand">Saharanpur</p>
        <h2>Both schools, one system.</h2>
        <p className="focus">{home.focus}</p>
      </section>
      <div className="login-grid">
        {home.schools.map((school) => (
          <article className="card stack" key={school.id}>
            <h3>{school.name}</h3>
            <p>{school.classes} classes · {school.sections} sections</p>
            <p className="tiny muted">Plan {school.plan}. Students and files stay inside this school.</p>
          </article>
        ))}
      </div>
      <CityBoard items={home.cityNews} />
      <NoticeList notices={home.notices} showReceipts />
      <form className="card stack" onSubmit={submit}>
        <h3>Inter-school event</h3>
        <label>
          Title
          <input value={title} onChange={(event) => setTitle(event.target.value)} />
        </label>
        <label>
          Message
          <textarea value={body} onChange={(event) => setBody(event.target.value)} />
        </label>
        {error ? <p className="error">{error}</p> : null}
        <button className="solid" type="submit">Post to every school</button>
      </form>
    </div>
  );
}

function NoticeList({ notices, showReceipts = false }: { notices: Notice[]; showReceipts?: boolean }) {
  if (notices.length === 0) return null;
  return (
    <section className="card stack">
      <h3>Notices</h3>
      {notices.map((notice) => (
        <article key={notice.id}>
          <strong>{notice.title}</strong>
          <div className="tiny muted">{notice.targetLabel} · {notice.scope}</div>
          <p>{notice.body}</p>
          {showReceipts && notice.audienceCount !== null ? (
            <p className="tiny muted">Seen by {notice.seenCount} of {notice.audienceCount} students and parents</p>
          ) : null}
        </article>
      ))}
    </section>
  );
}

function CityBoard({ items }: { items: NewsItem[] }) {
  return (
    <section className="card stack news">
      <h3>Saharanpur, last 24 hours</h3>
      {items.length === 0 ? <p className="muted">Nothing new in the last day.</p> : null}
      {items.map((item) => (
        <article key={item.id}>
          <a href={item.sourceUrl} target="_blank" rel="noreferrer">{item.title}</a>
          <div className="tiny muted">{item.sourceName} · {clock(item.publishedAt)}</div>
          <p>{item.summary}</p>
        </article>
      ))}
    </section>
  );
}
