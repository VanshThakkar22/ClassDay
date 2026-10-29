import { Navigate, Route, Routes } from "react-router-dom";
import { useAuth } from "./auth";
import type { Role } from "./api";
import { Shell } from "./shell";
import { Login } from "./pages/Login";
import { Today } from "./pages/Today";
import { Papers } from "./pages/Papers";
import { Notes } from "./pages/Notes";
import { Diary } from "./pages/Diary";
import { Office } from "./pages/Office";
import { City } from "./pages/City";
import { Later } from "./pages/Later";
import { Schools } from "./pages/Schools";

function home(role: Role) {
  if (role === "TEACHER") return "/today";
  if (role === "SCHOOL_ADMIN") return "/office";
  if (role === "PLATFORM_ADMIN") return "/schools";
  return "/diary";
}

function Gate() {
  const { user, ready } = useAuth();
  if (!ready) return <p className="booting">Opening the book…</p>;
  if (!user) return <Navigate to="/login" replace />;
  return <Shell />;
}

export function App() {
  const { user, ready } = useAuth();
  return (
    <Routes>
      <Route path="/login" element={ready && user ? <Navigate to={home(user.role)} replace /> : <Login />} />
      <Route element={<Gate />}>
        <Route path="/" element={user ? <Navigate to={home(user.role)} replace /> : <Navigate to="/login" replace />} />
        <Route path="/today" element={<Today />} />
        <Route path="/class" element={<Papers />} />
        <Route path="/notes" element={<Notes />} />
        <Route path="/diary" element={<Diary />} />
        <Route path="/office" element={<Office />} />
        <Route path="/city" element={<City />} />
        <Route path="/later" element={<Later />} />
        <Route path="/schools" element={<Schools />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
