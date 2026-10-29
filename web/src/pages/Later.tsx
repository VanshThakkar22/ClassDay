import { useState } from "react";

const PLANS = [
  { name: "Section plan", line: "A smaller school. Daily sheets and notes to a section.", here: false },
  { name: "Campus plan", line: "Both demo schools sit here. Whole-school notes, and one paper for every section of a class.", here: true },
  { name: "City plan", line: "Post an inter-school event to every school on Openbell.", here: false }
];

const MODULES = [
  { name: "Fees and receipts", line: "Families pay on the same login. The office counter, not the teacher’s evening." },
  { name: "Attendance", line: "The morning register. It does not ask a teacher to stay online at night." },
  { name: "Exams and report cards", line: "Marks, and the card that goes home at the end of term." },
  { name: "Transport", line: "Routes, and whether the afternoon bus has left." },
  { name: "Timetable", line: "The week’s periods, once the diary is already a habit." },
  { name: "Library", line: "Books out, books back." }
];

export function Later() {
  const [held, setHeld] = useState<string | null>(null);
  return (
    <div>
      <p className="eyebrow">Same book, later chapters</p>
      <h1>Pull these in later</h1>
      <p className="deck">
        They stay on this login and these schools. Nothing here opens a second product. The button holds the place.
      </p>
      <h2>Plans</h2>
      <ul className="drawers">
        {PLANS.map((item) => (
          <li key={item.name}>
            <div>
              <strong>{item.name}</strong>
              <p>{item.line}</p>
            </div>
            {item.here ? (
              <span className="stamp live">This demo</span>
            ) : (
              <button type="button" className="quiet" onClick={() => setHeld(item.name)}>
                Pull in later
              </button>
            )}
          </li>
        ))}
      </ul>
      <h2>Office modules</h2>
      <ul className="drawers">
        {MODULES.map((item) => (
          <li key={item.name}>
            <div>
              <strong>{item.name}</strong>
              <p>{item.line}</p>
            </div>
            <button type="button" className="quiet" onClick={() => setHeld(item.name)}>
              Pull in later
            </button>
          </li>
        ))}
      </ul>
      {held && <p className="held">{held} stays on the shelf. It will open on this same book when you build it.</p>}
    </div>
  );
}
