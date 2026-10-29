import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { ApiError, type Role } from "../api";
import { useAuth } from "../auth";
import { Bell, Problem } from "../bits";

const PEOPLE: { email: string; name: string; meta: string; role: Role }[] = [
  { email: "meera@northline.school", name: "Meera Sharma", meta: "Maths teacher · already sent today", role: "TEACHER" },
  { email: "arjun@northline.school", name: "Arjun Malhotra", meta: "English · nothing to send today", role: "TEACHER" },
  { email: "aman@northline.school", name: "Aman Sharma", meta: "Student · Class 8 A · was out", role: "STUDENT" },
  { email: "leela@northline.school", name: "Leela Sharma", meta: "Aman's mother", role: "PARENT" },
  { email: "sana@northline.school", name: "Sana Siddiqui", meta: "Northline office", role: "SCHOOL_ADMIN" },
  { email: "kabir@riverbend.school", name: "Kabir Qureshi", meta: "Hindi · Riverbend", role: "TEACHER" },
  { email: "desk@openbell.school", name: "City desk", meta: "Both schools, and Saharanpur", role: "PLATFORM_ADMIN" }
];

const MORE = [
  ["riya@northline.school", "Riya · science sample for all of Class 10"],
  ["neel@northline.school", "Neel · Class 10 A"],
  ["pooja@northline.school", "Pooja · Neel's mother"],
  ["nabil@northline.school", "Nabil · Sara's father, Class 8 A"],
  ["harish@riverbend.school", "Harish · Riverbend office"],
  ["isha@riverbend.school", "Isha · Class 5 C"],
  ["farah@riverbend.school", "Farah · Isha's mother"]
];

function home(role: Role) {
  if (role === "TEACHER") return "/today";
  if (role === "SCHOOL_ADMIN") return "/office";
  if (role === "PLATFORM_ADMIN") return "/schools";
  return "/diary";
}

export function Login() {
  const { signIn, ready } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("meera@northline.school");
  const [password, setPassword] = useState("openbell");
  const [problem, setProblem] = useState("");
  const [busy, setBusy] = useState(false);
  const [more, setMore] = useState(false);

  async function submit(event: React.FormEvent) {
    event.preventDefault();
    setBusy(true);
    setProblem("");
    try {
      const user = await signIn(email, password);
      navigate(home(user.role));
    } catch (error) {
      setProblem(error instanceof ApiError ? error.message : "The book did not open.");
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="split">
      <section className="night">
        <Bell />
        <p className="eyebrow">Saharanpur</p>
        <h1>The day is over. The class is on paper.</h1>
        <p className="lede">
          Teachers send one sheet home, or nothing at all. An absent student catches up without a night of messages.
        </p>
      </section>
      <section className="day">
        <h2>Step inside</h2>
        <p className="hint">Demo only. Every password is openbell. The two schools are fictional.</p>
        <form onSubmit={submit}>
          <label>
            Email
            <input value={email} onChange={(event) => setEmail(event.target.value)} autoComplete="username" />
          </label>
          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
            />
          </label>
          <Problem message={problem} />
          <button className="primary" type="submit" disabled={busy || !ready}>
            {busy ? "Opening…" : "Open the school"}
          </button>
        </form>
        <ul className="people">
          {PEOPLE.map((person) => (
            <li key={person.email}>
              <button type="button" className={email === person.email ? "person on" : "person"} onClick={() => setEmail(person.email)}>
                <span>{person.name}</span>
                <small>{person.meta}</small>
              </button>
            </li>
          ))}
        </ul>
        <button type="button" className="texty" onClick={() => setMore((value) => !value)}>
          {more ? "Hide the other logins" : "Other people in the demo"}
        </button>
        {more && (
          <ul className="more">
            {MORE.map(([mail, label]) => (
              <li key={mail}>
                <button type="button" onClick={() => setEmail(mail)}>
                  {label}
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  );
}
