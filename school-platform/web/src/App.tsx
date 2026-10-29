import { useEffect, useState } from "react";
import { Home, loadHome, todayInSaharanpur } from "./api";
import { AdminHome, FamilyHome, LaterDesk, Login, NewsRail, PlatformHome, SchoolCrest, TeacherHome } from "./views";

function roleLabel(role: string | undefined) {
  if (role === "TEACHER") return "Teacher";
  if (role === "STUDENT") return "Student";
  if (role === "PARENT") return "Parent";
  if (role === "SCHOOL_ADMIN") return "School office";
  if (role === "PLATFORM_ADMIN") return "City office";
  return "";
}

const LATER: Record<string, string> = {
  attendance: "Attendance",
  exams: "Exams",
  fees: "Fees",
};

function navFor(role: string | undefined) {
  const later = [
    { id: "attendance", label: "Attendance" },
    { id: "exams", label: "Exams" },
    { id: "fees", label: "Fees" },
  ];
  if (role === "PLATFORM_ADMIN") {
    return [
      { id: "home", label: "Home" },
      { id: "schools", label: "Schools" },
      { id: "notices", label: "Notices" },
      { id: "city", label: "City" },
    ];
  }
  if (role === "SCHOOL_ADMIN") {
    return [
      { id: "home", label: "Home" },
      { id: "classes", label: "Classes" },
      { id: "papers", label: "Papers" },
      { id: "notices", label: "Notices" },
      { id: "city", label: "City" },
      ...later,
    ];
  }
  return [
    { id: "home", label: "Home" },
    { id: "work", label: "Class work" },
    { id: "papers", label: "Papers" },
    { id: "notices", label: "Notices" },
    { id: "city", label: "City" },
    ...later,
  ];
}

export default function App() {
  const [token, setToken] = useState<string | null>(localStorage.getItem("classday-token"));
  const [home, setHome] = useState<Home | null>(null);
  const [date, setDate] = useState(todayInSaharanpur());
  const [revision, setRevision] = useState(0);
  const [error, setError] = useState("");
  const [pane, setPane] = useState("home");

  function signIn(next: string) {
    localStorage.setItem("classday-token", next);
    setToken(next);
    setPane("home");
  }

  function signOut() {
    localStorage.removeItem("classday-token");
    setToken(null);
    setHome(null);
    setPane("home");
  }

  useEffect(() => {
    if (!token) return;
    let cancelled = false;
    loadHome(token, date)
      .then((next) => {
        if (!cancelled) {
          setHome(next);
          setError("");
        }
      })
      .catch((err: unknown) => {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Could not load");
          if (String(err).includes("Sign in")) signOut();
        }
      });
    return () => {
      cancelled = true;
    };
  }, [token, date, revision]);

  if (!token) {
    return (
      <main className="gate-page">
        <Login onSuccess={signIn} />
      </main>
    );
  }

  const role = home?.user.role;
  const schoolName = home?.user.schoolName || "ClassDay";
  const links = navFor(role);
  const changed = () => setRevision((value) => value + 1);
  return (
    <div className="desk">
      <header className="topbar">
        <p className="gate-brand">ClassDay</p>
        {home ? <SchoolCrest name={schoolName} compact /> : <div />}
        <div className="who">
          <strong>{home?.user.fullName ?? "Signing in"}</strong>
          <span className="tiny muted">{roleLabel(role)}</span>
          <button className="textish" type="button" onClick={signOut}>Sign out</button>
        </div>
      </header>
      <nav className="nav">
        {links.map((link) => (
          <button key={link.id} type="button" className={pane === link.id ? "on" : ""} onClick={() => setPane(link.id)}>
            {link.label}
          </button>
        ))}
      </nav>
      <div className="workspace">
        <div className="stage">
          {error ? <p className="error">{error}</p> : null}
          {!home ? <p>Loading the school day…</p> : null}
          {LATER[pane] ? <LaterDesk title={LATER[pane]} /> : null}
          {home && !LATER[pane] && role === "TEACHER" ? (
            <TeacherHome home={home} token={token} date={date} onDate={setDate} onChanged={changed} pane={pane} />
          ) : null}
          {home && !LATER[pane] && (role === "STUDENT" || role === "PARENT") ? (
            <FamilyHome home={home} token={token} date={date} onDate={setDate} pane={pane} />
          ) : null}
          {home && !LATER[pane] && role === "SCHOOL_ADMIN" ? (
            <AdminHome home={home} token={token} onChanged={changed} pane={pane} />
          ) : null}
          {home && !LATER[pane] && role === "PLATFORM_ADMIN" ? (
            <PlatformHome home={home} token={token} onChanged={changed} pane={pane} />
          ) : null}
        </div>
        {home ? <NewsRail items={home.cityNews} /> : null}
      </div>
    </div>
  );
}
