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
  deleteNotice,
  postNotice,
  todayInSaharanpur,
  uploadDaily,
  uploadPaper,
} from "./api";

export function SchoolCrest({ name, compact = false }: { name: string; compact?: boolean }) {
  const letters = monogram(name);
  return (
    <div className={compact ? "crest-block compact" : "crest-block"}>
      <svg className="crest" viewBox="0 0 120 120" role="img" aria-label={name}>
        <circle cx="60" cy="60" r="56" fill="#f7f4ee" stroke="#c6a15b" strokeWidth="3" />
        <circle cx="60" cy="60" r="48" fill="#16324f" />
        <circle cx="60" cy="60" r="40" fill="none" stroke="#e6d3a1" strokeWidth="1.25" />
        <text x="60" y="68" textAnchor="middle" fill="#f7f4ee" fontSize="28" fontFamily="Cormorant Garamond, serif" fontWeight="700">
          {letters}
        </text>
      </svg>
      <h1 className="crest-name">{name}</h1>
      {name !== "Saharanpur" ? <p className="crest-place">Saharanpur</p> : <p className="crest-place">ClassDay</p>}
    </div>
  );
}

function monogram(name: string) {
  const skip = new Set(["school", "public", "the", "of", "and"]);
  const words = name.split(/\s+/).filter((word) => word && !skip.has(word.toLowerCase()));
  const source = words.length ? words : [name];
  if (source.length === 1) return source[0].slice(0, 2).toUpperCase();
  return source.slice(0, 2).map((word) => word[0].toUpperCase()).join("");
}

export function Login({ onSuccess }: { onSuccess: (token: string) => void }) {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
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
    <form className="gate-card stack" onSubmit={submit}>
      <p className="gate-brand">ClassDay</p>
      <h1>Sign in</h1>
      <p className="muted">Use the email and password given by your school.</p>
      <label>
        Email
        <input
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          autoComplete="username"
          placeholder="name@school.edu"
          required
        />
      </label>
      <label>
        Password
        <input
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          autoComplete="current-password"
          placeholder="Password"
          required
        />
      </label>
      {error ? <p className="error">{error}</p> : null}
      <button className="solid" disabled={busy} type="submit">{busy ? "Signing in…" : "Sign in"}</button>
    </form>
  );
}

export function TeacherHome({
  home,
  token,
  date,
  onDate,
  onChanged,
  pane,
}: {
  home: Home;
  token: string;
  date: string;
  onDate: (date: string) => void;
  onChanged: () => void;
  pane: string;
}) {
  const grades = [...new Map(home.teacherCards.map((card) => [card.gradeId, card.classLevel])).entries()];
  if (pane === "home") {
    return (
      <div className="work-grid">
        <div className="stat-row">
          <article className="stat"><span>Sections</span><strong>{home.teacherCards.length}</strong></article>
          <article className="stat"><span>Papers</span><strong>{home.classPapers.length}</strong></article>
          <article className="stat"><span>Notices</span><strong>{home.notices.length}</strong></article>
        </div>
      </div>
    );
  }
  if (pane === "work") {
    return <WorkDesk home={home} token={token} date={date} onDate={onDate} onChanged={onChanged} />;
  }
  if (pane === "papers") {
    return (
      <div className="stack">
        <PaperList papers={home.classPapers} token={token} />
        <ClassPaperForm grades={grades} token={token} onChanged={onChanged} />
      </div>
    );
  }
  if (pane === "notices") return <NoticeList notices={home.notices} />;
  if (pane === "city") return <CityBoard items={home.cityNews} />;
  return null;
}

function WorkDesk({
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
  const today = todayInSaharanpur();
  const [sectionId, setSectionId] = useState<number | "">("");
  const [subject, setSubject] = useState("");
  const [draftDate, setDraftDate] = useState(date || today);
  const [step, setStep] = useState<"pick" | "confirm" | "form">("pick");

  const classes = [...new Map(home.teacherCards.map((card) => [card.sectionId, card.classLabel])).entries()];
  const subjects = [...new Set(home.teacherCards.filter((card) => card.sectionId === sectionId).map((card) => card.subject))];
  const card = home.teacherCards.find((item) => item.sectionId === sectionId && item.subject === subject);

  function chooseClass(next: number | "") {
    setSectionId(next);
    const nextSubjects = home.teacherCards.filter((item) => item.sectionId === next).map((item) => item.subject);
    setSubject(nextSubjects.length === 1 ? nextSubjects[0] : "");
    setStep("pick");
  }

  function continueFromPicker() {
    if (sectionId === "" || !subject || !draftDate) return;
    if (draftDate === today) {
      onDate(draftDate);
      setStep("form");
      return;
    }
    setStep("confirm");
  }

  function openForDate() {
    if (!draftDate) return;
    onDate(draftDate);
    setStep("form");
  }

  if (step === "form" && card) {
    return (
      <div className="stack">
        <button className="textish" type="button" onClick={() => setStep("pick")}>Back to class and subject</button>
        <TeacherCardView card={card} token={token} date={draftDate} onChanged={onChanged} />
      </div>
    );
  }

  if (step === "confirm") {
    return (
      <section className="card stack picker">
        <h3>Which date is this work for?</h3>
        <p className="muted">This is not today. Choose the day, then the form opens for that class only.</p>
        <label>
          Date
          <input className="date" type="date" value={draftDate} onChange={(event) => setDraftDate(event.target.value)} />
        </label>
        <div className="row">
          <button className="solid" type="button" onClick={openForDate}>Open form</button>
          <button className="textish" type="button" onClick={() => setStep("pick")}>Back</button>
        </div>
      </section>
    );
  }

  return (
    <section className="card stack picker">
      <h3>Class work</h3>
      <p className="muted">Choose one class and subject. The upload opens only for that choice.</p>
      <label>
        Class
        <select value={sectionId} onChange={(event) => chooseClass(event.target.value ? Number(event.target.value) : "")}>
          <option value="">Choose a class</option>
          {classes.map(([id, label]) => (
            <option key={id} value={id}>{label}</option>
          ))}
        </select>
      </label>
      <label>
        Subject
        <select value={subject} onChange={(event) => { setSubject(event.target.value); setStep("pick"); }}>
          <option value="">Choose a subject</option>
          {subjects.map((name) => (
            <option key={name} value={name}>{name}</option>
          ))}
        </select>
      </label>
      <label>
        Date
        <input className="date" type="date" value={draftDate} onChange={(event) => setDraftDate(event.target.value)} />
      </label>
      <button className="solid" type="button" disabled={sectionId === "" || !subject} onClick={continueFromPicker}>Continue</button>
    </section>
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

export function FamilyHome({
  home,
  token,
  date,
  onDate,
  pane,
}: {
  home: Home;
  token: string;
  date: string;
  onDate: (date: string) => void;
  pane: string;
}) {
  const files = (
    <section className="card stack">
      <div className="row">
        <h3>Today's files</h3>
        <label className="date-label">
          Day
          <input className="date" type="date" value={date} onChange={(event) => onDate(event.target.value)} />
        </label>
      </div>
      <p className="tiny muted">{home.sectionLabel || home.user.schoolName}</p>
      {home.todayWork.length === 0 ? (
        <p className="muted">Nothing was uploaded for this day.</p>
      ) : (
        home.todayWork.map((file) => <DailyRow key={file.id} file={file} token={token} />)
      )}
    </section>
  );
  if (pane === "home") {
    return (
      <div className="work-grid">
        <div className="stat-row">
          <article className="stat"><span>Files today</span><strong>{home.todayWork.length}</strong></article>
          <article className="stat"><span>Papers</span><strong>{home.classPapers.length}</strong></article>
          <article className="stat"><span>Notices</span><strong>{home.notices.length}</strong></article>
        </div>
        <div className="span-2">{files}</div>
      </div>
    );
  }
  if (pane === "work") return files;
  if (pane === "papers") return <PaperList papers={home.classPapers} token={token} />;
  if (pane === "notices") return <NoticeList notices={home.notices} />;
  if (pane === "city") return <CityBoard items={home.cityNews} />;
  return null;
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
  if (papers.length === 0) {
    return (
      <section className="card">
        <h3>Papers</h3>
        <p className="muted">No paper has been sent to this class.</p>
      </section>
    );
  }
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

export function AdminHome({ home, token, onChanged, pane }: { home: Home; token: string; onChanged: () => void; pane: string }) {
  const grades = home.grades.map((grade) => [grade.gradeId, grade.level] as [number, number]);
  if (pane === "home") {
    const withFiles = home.grades.reduce((sum, grade) => sum + grade.sections.filter((section) => section.filesToday > 0).length, 0);
    return (
      <div className="work-grid">
        <div className="stat-row">
          <article className="stat"><span>Classes</span><strong>{home.grades.length}</strong></article>
          <article className="stat"><span>Sections with a file</span><strong>{withFiles}</strong></article>
          <article className="stat"><span>Notices</span><strong>{home.notices.length}</strong></article>
        </div>
        <section className="card span-2">
          <h3>Classes</h3>
          <p className="tiny muted">Classes 1–12, sections A–E.</p>
          {home.grades.map((grade) => (
            <GradeDetails key={grade.gradeId} grade={grade} />
          ))}
        </section>
      </div>
    );
  }
  if (pane === "classes") {
    return (
      <section className="card">
        <h3>Classes</h3>
        {home.grades.map((grade) => (
          <GradeDetails key={grade.gradeId} grade={grade} />
        ))}
      </section>
    );
  }
  if (pane === "papers") {
    return (
      <div className="stack">
        <PaperList papers={home.classPapers} token={token} />
        <ClassPaperForm grades={grades} token={token} onChanged={onChanged} />
      </div>
    );
  }
  if (pane === "notices") {
    return (
      <div className="stack">
        <NoticeComposer home={home} token={token} onChanged={onChanged} />
        <NoticeList
          notices={home.notices}
          showReceipts
          canDelete={(notice) => notice.scope !== "CITY"}
          onDelete={(id) => removeNotice(token, id, onChanged)}
        />
      </div>
    );
  }
  if (pane === "city") return <CityBoard items={home.cityNews} />;
  return null;
}

function GradeDetails({ grade }: { grade: GradeBlock }) {
  const active = grade.sections.some((section) => section.teachers.length > 0 || section.studentCount > 0);
  return (
    <details open={active}>
      <summary>Class {grade.level}</summary>
      {grade.sections.map((section) => (
        <div className="section-line" key={section.sectionId}>
          <strong>{section.name}</strong>
          <span>{section.teachers.length ? section.teachers.join(", ") : "No teacher yet"}{section.studentCount ? ` · ${section.studentCount} ${section.studentCount === 1 ? "student" : "students"}` : ""}</span>
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

export function PlatformHome({ home, token, onChanged, pane }: { home: Home; token: string; onChanged: () => void; pane: string }) {
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

  const schools = (
    <div className="work-grid">
      {home.schools.map((school) => (
        <article className="card stack" key={school.id}>
          <h3>{school.name}</h3>
          <p>{school.classes} classes · {school.sections} sections</p>
          <p className="tiny muted">{school.plan}</p>
        </article>
      ))}
    </div>
  );
  const eventForm = (
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
  );
  if (pane === "home" || pane === "schools") return schools;
  if (pane === "notices") {
    return (
      <div className="stack">
        {eventForm}
        <NoticeList
          notices={home.notices}
          showReceipts
          canDelete={(notice) => notice.scope === "CITY"}
          onDelete={(id) => removeNotice(token, id, onChanged)}
        />
      </div>
    );
  }
  if (pane === "city") return <CityBoard items={home.cityNews} />;
  return null;
}

async function removeNotice(token: string, id: number, onChanged: () => void) {
  if (!window.confirm("Delete this notice? It will disappear for everyone who can see it.")) return;
  try {
    await deleteNotice(token, id);
    onChanged();
  } catch (error) {
    window.alert(error instanceof Error ? error.message : "Could not delete this notice.");
  }
}

function NoticeList({
  notices,
  showReceipts = false,
  canDelete,
  onDelete,
}: {
  notices: Notice[];
  showReceipts?: boolean;
  canDelete?: (notice: Notice) => boolean;
  onDelete?: (id: number) => void;
}) {
  if (notices.length === 0) {
    return (
      <section className="card">
        <h3>Notices</h3>
        <p className="muted">No notices today.</p>
      </section>
    );
  }
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
          {canDelete?.(notice) && onDelete ? (
            <button className="textish" type="button" onClick={() => onDelete(notice.id)}>Delete</button>
          ) : null}
        </article>
      ))}
    </section>
  );
}

export function NewsRail({ items }: { items: NewsItem[] }) {
  return (
    <aside className="news-rail">
      <h2>Saharanpur</h2>
      <p className="tiny muted">Last 24 hours</p>
      {items.length === 0 ? <p className="muted">No headlines in the last day.</p> : null}
      {items.map((item) => (
        <article className="headline" key={item.id}>
          <a href={item.sourceUrl} target="_blank" rel="noreferrer">{item.title.replace(/^Sample:\s*/i, "")}</a>
          <span className="tiny muted">{item.sourceName} · {clock(item.publishedAt)}</span>
        </article>
      ))}
    </aside>
  );
}

export function LaterDesk({ title }: { title: string }) {
  return (
    <section className="card stack">
      <h2>{title}</h2>
      <p className="muted">This section is not open for the school yet.</p>
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
          <a href={item.sourceUrl} target="_blank" rel="noreferrer">{item.title.replace(/^Sample:\s*/i, "")}</a>
          <div className="tiny muted">{item.sourceName} · {clock(item.publishedAt)}</div>
          <p>{item.summary}</p>
        </article>
      ))}
    </section>
  );
}
