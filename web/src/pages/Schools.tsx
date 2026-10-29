import { useEffect, useState } from "react";
import { api, ApiError, type SchoolCard, type SchoolTree } from "../api";
import { useAuth } from "../auth";
import { Problem } from "../bits";
import { Ladder } from "../ladder";

export function Schools() {
  const { user } = useAuth();
  const [cards, setCards] = useState<SchoolCard[]>([]);
  const [tree, setTree] = useState<SchoolTree | null>(null);
  const [gradeId, setGradeId] = useState<number | null>(null);
  const [problem, setProblem] = useState("");

  useEffect(() => {
    api<SchoolCard[]>("/api/platform/schools")
      .then(setCards)
      .catch((error: unknown) => setProblem(error instanceof ApiError ? error.message : "The schools did not load."));
  }, []);

  async function openSchool(id: number) {
    setProblem("");
    try {
      const next = await api<SchoolTree>(`/api/platform/schools/${id}`);
      setTree(next);
      const preferred = next.grades.find((grade) => grade.level === 8) ?? next.grades[0];
      setGradeId(preferred?.id ?? null);
    } catch (error) {
      setProblem(error instanceof ApiError ? error.message : "That school did not open.");
    }
  }

  if (user && user.role !== "PLATFORM_ADMIN") {
    return <p className="quiet-line">The city desk is the only login that sees both schools.</p>;
  }

  return (
    <div>
      <p className="eyebrow">One book, two schools</p>
      <h1>Saharanpur</h1>
      <p className="deck">Each school keeps its own students and sheets. You can see the shape of the building, not their family files.</p>
      <Problem message={problem} />
      <div className="school-row">
        {cards.map((school) => (
          <button key={school.id} type="button" className={tree?.id === school.id ? "school on" : "school"} onClick={() => openSchool(school.id)}>
            <strong>{school.name}</strong>
            <span>{school.area}</span>
            <small>
              {school.grades} classes · {school.sections} sections
            </small>
          </button>
        ))}
      </div>
      {tree && (
        <Ladder
          tree={tree}
          gradeId={gradeId}
          sectionId={null}
          onGrade={setGradeId}
        />
      )}
    </div>
  );
}
