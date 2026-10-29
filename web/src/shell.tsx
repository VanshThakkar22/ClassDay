import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "./auth";
import type { Role } from "./api";
import { Bell } from "./bits";

const NAV: Record<Role, { to: string; label: string }[]> = {
  TEACHER: [
    { to: "/today", label: "Today" },
    { to: "/class", label: "For the class" },
    { to: "/notes", label: "Notes" },
    { to: "/city", label: "City" },
    { to: "/later", label: "Later" }
  ],
  PARENT: [
    { to: "/diary", label: "Diary" },
    { to: "/city", label: "City" },
    { to: "/later", label: "Later" }
  ],
  STUDENT: [
    { to: "/diary", label: "Diary" },
    { to: "/city", label: "City" },
    { to: "/later", label: "Later" }
  ],
  SCHOOL_ADMIN: [
    { to: "/office", label: "Office" },
    { to: "/city", label: "City" },
    { to: "/later", label: "Later" }
  ],
  PLATFORM_ADMIN: [
    { to: "/schools", label: "Schools" },
    { to: "/city", label: "City" },
    { to: "/later", label: "Later" }
  ]
};

export function Shell() {
  const { user, signOut } = useAuth();
  if (!user) return null;
  return (
    <div className="sheet">
      <header className="mast">
        <div className="brand">
          <Bell small />
          <div>
            <strong>Openbell</strong>
            <span>{user.schoolName}</span>
          </div>
        </div>
        <nav>
          {NAV[user.role].map((item) => (
            <NavLink key={item.to} to={item.to}>
              {item.label}
            </NavLink>
          ))}
        </nav>
        <div className="who">
          <span>{user.dateLabel}</span>
          <button type="button" className="quiet" onClick={signOut}>
            {user.name.split(" ")[0]}, leave
          </button>
        </div>
      </header>
      <main>
        <Outlet />
      </main>
      <footer>Made for the hour after the last bell.</footer>
    </div>
  );
}
