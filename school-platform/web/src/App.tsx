import { useEffect, useState } from "react";
import { Home, loadHome, todayInSaharanpur } from "./api";
import { AdminHome, FamilyHome, Login, PlatformHome, TeacherHome } from "./views";

export default function App() {
  const [token, setToken] = useState<string | null>(localStorage.getItem("classday-token"));
  const [home, setHome] = useState<Home | null>(null);
  const [date, setDate] = useState(todayInSaharanpur());
  const [revision, setRevision] = useState(0);
  const [error, setError] = useState("");

  function signIn(next: string) {
    localStorage.setItem("classday-token", next);
    setToken(next);
  }

  function signOut() {
    localStorage.removeItem("classday-token");
    setToken(null);
    setHome(null);
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
      <main className="app">
        <Login onSuccess={signIn} />
      </main>
    );
  }

  const role = home?.user.role;
  return (
    <main className="app stack">
      <header className="top">
        <div>
          <p className="brand">Classday</p>
          <p className="tiny muted">Saharanpur prototype</p>
        </div>
        <div className="who">
          <strong>{home?.user.fullName ?? "…"}</strong>
          <span className="tiny muted">{home?.user.schoolName}</span>
          <div>
            <button className="textish" type="button" onClick={signOut}>Sign out</button>
          </div>
        </div>
      </header>
      {error ? <p className="error">{error}</p> : null}
      {!home ? <p>Loading the school day…</p> : null}
      {home && role === "TEACHER" ? (
        <TeacherHome home={home} token={token} date={date} onDate={setDate} onChanged={() => setRevision((value) => value + 1)} />
      ) : null}
      {home && (role === "STUDENT" || role === "PARENT") ? (
        <FamilyHome home={home} token={token} date={date} onDate={setDate} />
      ) : null}
      {home && role === "SCHOOL_ADMIN" ? (
        <AdminHome home={home} token={token} onChanged={() => setRevision((value) => value + 1)} />
      ) : null}
      {home && role === "PLATFORM_ADMIN" ? (
        <PlatformHome home={home} token={token} onChanged={() => setRevision((value) => value + 1)} />
      ) : null}
    </main>
  );
}
