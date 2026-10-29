import type { SchoolTree } from "./api";
import { planName } from "./api";

export function Ladder({
  tree,
  gradeId,
  sectionId,
  onGrade,
  onSection
}: {
  tree: SchoolTree;
  gradeId: number | null;
  sectionId: number | null;
  onGrade?: (id: number) => void;
  onSection?: (id: number) => void;
}) {
  const grade = tree.grades.find((item) => item.id === gradeId) ?? tree.grades[0];
  return (
    <div>
      <p className="plan-line">
        {tree.area}, {tree.city} · {planName(tree.plan)} plan · {tree.grades.length * 5} sections
      </p>
      <div className="numbers">
        {tree.grades.map((item) => (
          <button
            key={item.id}
            type="button"
            className={item.id === grade?.id ? "num on" : "num"}
            onClick={() => onGrade?.(item.id)}
          >
            {item.level}
          </button>
        ))}
      </div>
      <ul className="folders">
        {grade?.sections.map((section) => (
          <li key={section.id}>
            <button
              type="button"
              className={section.id === sectionId ? "folder on" : "folder"}
              onClick={() => onSection?.(section.id)}
            >
              <strong>Section {section.name}</strong>
              <span>{section.teachers.length ? section.teachers.join("  ·  ") : "No teacher on the demo yet"}</span>
              <small>
                {section.students === 0 ? "No demo family in this room" : `${section.students} demo student${section.students === 1 ? "" : "s"}`}
              </small>
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}
