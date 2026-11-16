import { FormEvent, useEffect, useState } from "react";
import {
  DailyFile,
  FeeBoard,
  FeeLine,
  GradeBlock,
  Home,
  NewsItem,
  Notice,
  PaperFile,
  TeacherCard,
  assignTeacher,
  clock,
  loadFees,
  login,
  removeAssignment,
  openPdf,
  deleteNotice,
  deletePaper,
  postNotice,
  remindFees,
  saveFee,
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
  const [sectionsOpen, setSectionsOpen] = useState(false);
  if (pane === "home") {
    return (
      <div className="work-grid">
        <div className="stat-row">
          <button type="button" className={sectionsOpen ? "stat tip on" : "stat tip"} onClick={() => setSectionsOpen((open) => !open)}>
            <span>Sections</span>
            <strong>{home.teacherCards.length}</strong>
            <span className="tip-box">
              {home.teacherCards.length === 0
                ? <span>No section yet</span>
                : home.teacherCards.map((card) => (
                    <span key={`${card.sectionId}-${card.subject}`}>{card.classLabel} · {card.subject}</span>
                  ))}
            </span>
          </button>
          <article className="stat"><span>Notices</span><strong>{schoolNotices(home.notices).length}</strong></article>
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
        <PaperList
          papers={home.classPapers}
          token={token}
          canDelete={(paper) => paper.uploadedById === home.user.id}
          onDelete={(id) => removePaper(token, id, onChanged)}
        />
        <ClassPaperForm grades={grades} token={token} onChanged={onChanged} />
      </div>
    );
  }
  if (pane === "notices") return <NoticeList notices={schoolNotices(home.notices)} />;
  if (pane === "city") return <CityBoard notices={cityNotices(home.notices)} />;
  return null;
}

function schoolNotices(notices: Notice[]) {
  return notices.filter((notice) => notice.scope !== "CITY");
}

function cityNotices(notices: Notice[]) {
  return notices.filter((notice) => notice.scope === "CITY");
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
          <article className="stat"><span>Notices</span><strong>{schoolNotices(home.notices).length}</strong></article>
        </div>
        <div className="span-2">{files}</div>
      </div>
    );
  }
  if (pane === "work") return files;
  if (pane === "papers") return <PaperList papers={home.classPapers} token={token} />;
  if (pane === "notices") return <NoticeList notices={schoolNotices(home.notices)} />;
  if (pane === "city") return <CityBoard notices={cityNotices(home.notices)} />;
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

function PaperList({
  papers,
  token,
  canDelete,
  onDelete,
}: {
  papers: PaperFile[];
  token: string;
  canDelete?: (paper: PaperFile) => boolean;
  onDelete?: (id: number) => void;
}) {
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
          <div className="row">
            <button className="ghost" type="button" onClick={() => openPdf(token, `/api/files/papers/${paper.id}`)}>Open PDF</button>
            {canDelete?.(paper) && onDelete ? (
              <button className="textish" type="button" onClick={() => onDelete(paper.id)}>Delete</button>
            ) : null}
          </div>
        </div>
      ))}
    </section>
  );
}

function AssignDesk({ home, token, onChanged }: { home: Home; token: string; onChanged: () => void }) {
  const sections = home.grades.flatMap((grade) =>
    grade.sections.map((section) => ({ ...section, label: `Class ${grade.level} · ${section.name}` })),
  );
  const [sectionId, setSectionId] = useState<number | "">("");
  const [teacherId, setTeacherId] = useState<number | "">("");
  const [subject, setSubject] = useState("");
  const [classTeacher, setClassTeacher] = useState(false);
  const [error, setError] = useState("");
  const picked = sections.find((section) => section.sectionId === sectionId);

  function pickSection(next: number | "") {
    setSectionId(next);
    const section = sections.find((item) => item.sectionId === next);
    setClassTeacher(teacherId !== "" && section?.classTeacherId === teacherId);
  }

  function pickTeacher(next: number | "") {
    setTeacherId(next);
    setClassTeacher(next !== "" && picked?.classTeacherId === next);
  }

  async function submit(event: FormEvent) {
    event.preventDefault();
    if (sectionId === "" || teacherId === "" || !subject.trim()) {
      setError("Choose the section, the teacher, and a subject.");
      return;
    }
    setError("");
    try {
      await assignTeacher(token, { sectionId, teacherId, subject: subject.trim(), classTeacher });
      setSubject("");
      onChanged();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not save");
    }
  }

  async function drop(id: number) {
    setError("");
    try {
      await removeAssignment(token, id);
      onChanged();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not remove");
    }
  }

  return (
    <form className="card stack" onSubmit={submit}>
      <h3>Assign a teacher</h3>
      <p className="muted">Choose the section, the subject they teach, and whether they are its class teacher.</p>
      <label>
        Section
        <select value={sectionId} onChange={(event) => pickSection(event.target.value ? Number(event.target.value) : "")}>
          <option value="">Choose a section</option>
          {sections.map((section) => (
            <option key={section.sectionId} value={section.sectionId}>{section.label}</option>
          ))}
        </select>
      </label>
      <label>
        Teacher
        <select value={teacherId} onChange={(event) => pickTeacher(event.target.value ? Number(event.target.value) : "")}>
          <option value="">Choose a teacher</option>
          {(home.teachers ?? []).map((teacher) => (
            <option key={teacher.id} value={teacher.id}>{teacher.fullName}</option>
          ))}
        </select>
      </label>
      <label>
        Subject
        <input value={subject} onChange={(event) => setSubject(event.target.value)} />
      </label>
      <label className="check">
        Class teacher of this section
        <input type="checkbox" checked={classTeacher} onChange={(event) => setClassTeacher(event.target.checked)} />
      </label>
      {picked?.duties?.length ? (
        <div>
          {picked.duties.map((duty) => (
            <p className="row between" key={duty.id}>
              <span>{duty.teacherName} · {duty.subject}{picked.classTeacherId === duty.teacherId ? " · Class teacher" : ""}</span>
              <button className="textish" type="button" onClick={() => drop(duty.id)}>Remove</button>
            </p>
          ))}
        </div>
      ) : null}
      {error ? <p className="error">{error}</p> : null}
      <button className="solid" type="submit">Save</button>
    </form>
  );
}

export function AdminHome({ home, token, onChanged, pane }: { home: Home; token: string; onChanged: () => void; pane: string }) {
  const grades = home.grades.map((grade) => [grade.gradeId, grade.level] as [number, number]);
  if (pane === "home") {
    return (
      <div className="work-grid">
        <div className="stat-row">
          <article className="stat"><span>Classes</span><strong>{home.grades.length}</strong></article>
          <article className="stat"><span>Notices</span><strong>{schoolNotices(home.notices).length}</strong></article>
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
  if (pane === "classes") return <AssignDesk home={home} token={token} onChanged={onChanged} />;
  if (pane === "papers") {
    return (
      <div className="stack">
        <ClassPaperForm grades={grades} token={token} onChanged={onChanged} />
        <PaperList
          papers={home.classPapers}
          token={token}
          canDelete={() => true}
          onDelete={(id) => removePaper(token, id, onChanged)}
        />
      </div>
    );
  }
  if (pane === "notices") {
    return (
      <div className="stack">
        <NoticeComposer home={home} token={token} onChanged={onChanged} />
        <NoticeList
          notices={schoolNotices(home.notices)}
          showReceipts
          canDelete={() => true}
          onDelete={(id) => removeNotice(token, id, onChanged)}
        />
      </div>
    );
  }
  if (pane === "city") {
    return (
      <CityBoard
        notices={cityNotices(home.notices)}
        showReceipts
        canDelete={(notice) => notice.authorId === home.user.id}
        onDelete={(id) => removeNotice(token, id, onChanged)}
      />
    );
  }
  return null;
}

function GradeDetails({ grade }: { grade: GradeBlock }) {
  return (
    <details>
      <summary>Class {grade.level}</summary>
      {grade.sections.map((section) => {
        const students = section.studentCount;
        const uploads = section.uploads ?? [];
        const files = uploads.length;
        return (
          <div className="section-line" key={section.sectionId}>
            <strong>{section.name}</strong>
            <span>
              {section.classTeacherName || "No class teacher"}
              {" · "}
              <span className="tip">
                Teachers
                <span className="tip-box">
                  {section.duties?.length
                    ? section.duties.map((duty) => <span key={duty.id}>{duty.teacherName} · {duty.subject}</span>)
                    : <span>No subject teacher yet</span>}
                </span>
              </span>
              {` · ${students} ${students === 1 ? "student" : "students"}`}
            </span>
            <span className="tip end">
              {files === 1 ? "1 file uploaded today" : `${files} files uploaded today`}
              <span className="tip-box">
                {uploads.length
                  ? uploads.map((file, index) => <span key={`${file.subject}-${file.teacherName}-${index}`}>{file.subject} · {file.teacherName}</span>)
                  : <span>No files yet</span>}
              </span>
            </span>
          </div>
        );
      })}
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
          <option value="CITY">All schools</option>
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
      </div>
    );
  }
  if (pane === "city") {
    return (
      <CityBoard
        notices={cityNotices(home.notices)}
        showReceipts
        canDelete={() => true}
        onDelete={(id) => removeNotice(token, id, onChanged)}
      />
    );
  }
  return null;
}

async function removePaper(token: string, id: number, onChanged: () => void) {
  if (!window.confirm("Delete this sample paper? It will disappear for that class.")) return;
  try {
    await deletePaper(token, id);
    onChanged();
  } catch (error) {
    window.alert(error instanceof Error ? error.message : "Could not delete this paper.");
  }
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

function seenCount(count: number) {
  if (count < 1000) return String(count);
  const thousands = count / 1000;
  const rounded = thousands >= 10 ? Math.round(thousands) : Math.round(thousands * 10) / 10;
  return String(rounded).replace(/\.0$/, "") + "K";
}

function NoticeList({
  notices,
  title = "Notices",
  empty = "No notices today.",
  showReceipts = false,
  canDelete,
  onDelete,
}: {
  notices: Notice[];
  title?: string;
  empty?: string;
  showReceipts?: boolean;
  canDelete?: (notice: Notice) => boolean;
  onDelete?: (id: number) => void;
}) {
  if (notices.length === 0) {
    return (
      <section className="card">
        <h3>{title}</h3>
        <p className="muted">{empty}</p>
      </section>
    );
  }
  return (
    <section className="card stack">
      <h3>{title}</h3>
      {notices.map((notice) => (
        <article key={notice.id}>
          <strong>{notice.title}</strong>
          <div className="tiny muted">{notice.scope === "CITY" ? notice.targetLabel : `${notice.targetLabel} · ${notice.scope === "FEE" ? "Fee reminder" : notice.scope}`}</div>
          <p>{notice.body}</p>
          {showReceipts && notice.audienceCount !== null ? (
            <p className="tiny muted">{notice.scope === "CITY" ? `Seen by ${seenCount(notice.seenCount ?? 0)}` : `Seen by ${notice.seenCount} of ${notice.audienceCount} students and parents`}</p>
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

function rs(amount: number) {
  return "₹" + amount.toLocaleString("en-IN");
}

function owed(line: FeeLine) {
  return line.schoolFee + line.busFee + line.otherFee;
}

function byClass(lines: FeeLine[]) {
  const groups: { label: string; lines: FeeLine[] }[] = [];
  for (const line of lines) {
    const last = groups[groups.length - 1];
    if (last?.label === line.classLabel) last.lines.push(line);
    else groups.push({ label: line.classLabel, lines: [line] });
  }
  return groups;
}

function classTotal(group: { lines: FeeLine[] }) {
  return group.lines.reduce((sum, line) => sum + owed(line), 0);
}

function classRank(label: string) {
  const match = /^Class (\d+)-(.+)$/.exec(label);
  return match ? Number(match[1]) * 100 + match[2].charCodeAt(0) : 0;
}

function sortSections(groups: { label: string; lines: FeeLine[] }[], sort: "class" | "high" | "low") {
  return [...groups].sort((a, b) => {
    const byClass = classRank(a.label) - classRank(b.label);
    if (sort === "class") return byClass;
    const diff = classTotal(a) - classTotal(b);
    if (diff === 0) return byClass;
    return sort === "high" ? -diff : diff;
  });
}

export function FeesDesk({ token, onChanged }: { token: string; onChanged: () => void }) {
  const [board, setBoard] = useState<FeeBoard | null>(null);
  const [draft, setDraft] = useState<Record<number, FeeLine>>({});
  const [title, setTitle] = useState("");
  const [body, setBody] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [sort, setSort] = useState<"class" | "high" | "low">("class");
  const [picked, setPicked] = useState<number | null>(null);

  useEffect(() => {
    let cancelled = false;
    loadFees(token)
      .then((next) => {
        if (cancelled) return;
        setBoard(next);
        setDraft(Object.fromEntries(next.lines.map((line) => [line.studentId, line])));
        setError("");
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(err instanceof Error ? err.message : "Could not load fees");
      });
    return () => {
      cancelled = true;
    };
  }, [token]);

  function patch(studentId: number, next: Partial<FeeLine>) {
    setDraft((current) => ({ ...current, [studentId]: { ...current[studentId], ...next } }));
  }

  async function save(event: FormEvent, studentId: number) {
    event.preventDefault();
    const line = draft[studentId];
    setError("");
    setMessage("");
    try {
      const saved = await saveFee(token, line);
      setBoard((current) => {
        if (!current) return current;
        const lines = current.lines.map((item) => (item.studentId === saved.studentId ? saved : item));
        return { ...current, lines, canRemind: lines.some((item) => owed(item) > 0) };
      });
      setDraft((current) => ({ ...current, [saved.studentId]: saved }));
      setMessage(`Saved for ${saved.studentName}.`);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not save");
    }
  }

  async function send(event: FormEvent) {
    event.preventDefault();
    setError("");
    setMessage("");
    try {
      const sent = await remindFees(token, title, body);
      setTitle("");
      setBody("");
      setMessage(`Sent to ${sent} ${sent === 1 ? "student" : "students"} and their parents.`);
      onChanged();
    } catch (err) {
      setError(err instanceof Error ? err.message : "Could not send");
    }
  }

  if (!board) return error ? <p className="error">{error}</p> : <p>Loading fees…</p>;
  const groups = byClass(board.lines);
  const office = !board.canEdit && groups.length > 1;
  const lead = board.canEdit
    ? "Choose a student, then enter what is still unpaid. Set a box to 0 when it is paid. The other fee needs a reason."
    : office
      ? "Every class section is here. Sort by the total still left. A reminder goes only to students whose total is still pending, and to their parents."
      : board.canRemind
        ? "Pending fees for each class. A reminder goes only to students who still owe, and to their parents."
        : board.note || "What is still unpaid.";

  return (
    <div className="stack">
      <section className="card stack">
        <h2>Fees</h2>
        <p className="muted">{lead}</p>
        {error ? <p className="error">{error}</p> : null}
        {message ? <p className="ok">{message}</p> : null}
        {board.canEdit && !board.canRemind ? (
          <p className="tiny muted">Save a pending amount, then you can remind that student and their parent.</p>
        ) : null}
        {office && !board.canRemind ? (
          <p className="tiny muted">Nobody has a total left, so a reminder is not sent.</p>
        ) : null}
        {board.canRemind ? (
          <form className="stack" onSubmit={send}>
            <h3>Remind students who still owe</h3>
            <p className="tiny muted">
              {office
                ? "Sent only to students whose total is still pending, and to their parents. Each notice adds that student's amount."
                : "They receive your words, then their own pending amount. The message uses the amounts already saved."}
            </p>
            <label>
              Title
              <input value={title} onChange={(event) => setTitle(event.target.value)} required />
            </label>
            <label>
              Message
              <textarea value={body} onChange={(event) => setBody(event.target.value)} required />
            </label>
            <button className="solid" type="submit">Send reminder</button>
          </form>
        ) : null}
      </section>
      {office ? (
        <section className="card stack">
          <div className="row between">
            <h3>Class sections</h3>
            <label className="fee-sort">
              Sort
              <select value={sort} onChange={(event) => setSort(event.target.value as "class" | "high" | "low")}>
                <option value="class">Class order</option>
                <option value="high">Highest pending</option>
                <option value="low">Lowest pending</option>
              </select>
            </label>
          </div>
          {sortSections(groups, sort).map((group) => {
            const total = classTotal(group);
            const owing = group.lines.filter((line) => owed(line) > 0);
            return (
              <details key={group.label}>
                <summary>
                  {group.label} · {rs(total)} still left
                  {owing.length ? ` · ${owing.length} ${owing.length === 1 ? "student" : "students"}` : ""}
                </summary>
                {owing.length === 0 ? <p className="muted">Nothing pending in this section.</p> : <div className="fee-people">{owing.map((line) => <FeeRead key={line.studentId} line={line} />)}</div>}
              </details>
            );
          })}
        </section>
      ) : board.canEdit ? (
        groups.map((group) => {
          const chosen = draft[picked ?? -1];
          const here = chosen && group.lines.some((line) => line.studentId === chosen.studentId);
          return (
            <section className="card stack" key={group.label}>
              <div className="row between">
                <h3>{group.label}</h3>
                <button className="ghost" type="button" onClick={() => downloadFees(group.lines)}>Download Excel</button>
              </div>
              <p className="tiny muted">{rs(classTotal(group))} still left</p>
              <div className="fee-people">
                {group.lines.map((line) => (
                  <article key={line.studentId} className={picked === line.studentId ? "fee-person on" : "fee-person"}>
                    <button type="button" className="fee-line" onClick={() => setPicked(line.studentId)}>
                      <strong>{line.studentName}</strong><span>{rs(owed(draft[line.studentId] ?? line))}</span>
                    </button>
                  </article>
                ))}
              </div>
              {here && chosen ? (
                <FeeEditor line={chosen} onChange={(next) => patch(chosen.studentId, next)} onSave={(event) => save(event, chosen.studentId)} />
              ) : (
                <p className="muted">Select a student to update their fees.</p>
              )}
            </section>
          );
        })
      ) : (
        groups.map((group) => (
          <section className="card stack" key={group.label}>
            <h3>{group.label}</h3>
            <p className="tiny muted">{rs(classTotal(group))} still left</p>
            {group.lines.map((line) => <FeeRead key={line.studentId} line={line} />)}
          </section>
        ))
      )}
    </div>
  );
}

function downloadFees(lines: FeeLine[]) {
  const cell = (value: string | number) => {
    const text = String(value).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    const type = typeof value === "number" ? "Number" : "String";
    return `<Cell><Data ss:Type="${type}">${text}</Data></Cell>`;
  };
  const rows = [["Name", "School Fee", "Bus Fee", "Other Fee", "Reason of other fee", "Total Fee"], ...lines.filter((line) => owed(line) > 0).map((line) => [line.studentName, line.schoolFee, line.busFee, line.otherFee, line.otherReason, owed(line)])];
  const xml = `<?xml version="1.0"?><?mso-application progid="Excel.Sheet"?><Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet" xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"><Worksheet ss:Name="Fees"><Table>${rows.map((row) => `<Row>${row.map(cell).join("")}</Row>`).join("")}</Table></Worksheet></Workbook>`;
  const url = URL.createObjectURL(new Blob([xml], { type: "application/vnd.ms-excel" }));
  const link = document.createElement("a");
  link.href = url;
  link.download = "pending-fees.xls";
  link.click();
  URL.revokeObjectURL(url);
}

function FeeEditor({
  line,
  onChange,
  onSave,
}: {
  line: FeeLine;
  onChange: (next: Partial<FeeLine>) => void;
  onSave: (event: FormEvent) => void;
}) {
  function amount(key: "schoolFee" | "busFee" | "otherFee", raw: string) {
    const value = Number(raw);
    onChange({ [key]: Number.isFinite(value) && value > 0 ? Math.min(1_000_000, Math.round(value)) : 0 });
  }
  return (
    <form className="fee-row" onSubmit={onSave}>
      <strong>{line.studentName}</strong>
      <div className="fee-grid">
        <label>
          School fee
          <input type="number" min={0} value={line.schoolFee} onChange={(event) => amount("schoolFee", event.target.value)} />
        </label>
        <label>
          Bus fee
          <input type="number" min={0} value={line.busFee} onChange={(event) => amount("busFee", event.target.value)} />
        </label>
        <label>
          Other fee
          <input type="number" min={0} value={line.otherFee} onChange={(event) => amount("otherFee", event.target.value)} />
        </label>
        <label>
          Other fee is for
          <input value={line.otherReason} placeholder="Trip, annual day, program" onChange={(event) => onChange({ otherReason: event.target.value })} />
        </label>
        <button className="solid" type="submit">Save</button>
      </div>
    </form>
  );
}

function FeeRead({ line }: { line: FeeLine }) {
  const [open, setOpen] = useState(false);
  const total = owed(line);
  if (!total) return <p className="muted">{line.studentName}: nothing is pending.</p>;
  return (
    <article className="fee-person">
      <button type="button" className="fee-line" onClick={() => setOpen((value) => !value)}>
        <strong>{line.studentName}</strong><span>{rs(total)}</span>
      </button>
      {open ? <p>School {rs(line.schoolFee)} · Bus {rs(line.busFee)} · Other{line.otherReason ? ` (${line.otherReason})` : ""} {rs(line.otherFee)}</p> : null}
    </article>
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

function CityBoard({
  notices = [],
  showReceipts = false,
  canDelete,
  onDelete,
}: {
  notices?: Notice[];
  showReceipts?: boolean;
  canDelete?: (notice: Notice) => boolean;
  onDelete?: (id: number) => void;
}) {
  return (
    <NoticeList
      notices={notices}
      title="Inter-school notices"
      empty="No inter-school notices."
      showReceipts={showReceipts}
      canDelete={canDelete}
      onDelete={onDelete}
    />
  );
}
