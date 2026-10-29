import { createContext, useContext, useEffect, useState } from "react";
import { api, clearToken, saveToken, token, type User } from "./api";

type AuthValue = {
  user: User | null;
  ready: boolean;
  signIn: (email: string, password: string) => Promise<User>;
  signOut: () => void;
};

const AuthContext = createContext<AuthValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [ready, setReady] = useState(false);

  useEffect(() => {
    if (!token()) {
      setReady(true);
      return;
    }
    api<User>("/api/me")
      .then(setUser)
      .catch(() => clearToken())
      .finally(() => setReady(true));
  }, []);

  async function signIn(email: string, password: string) {
    const result = await api<{ token: string; user: User }>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password })
    });
    saveToken(result.token);
    setUser(result.user);
    return result.user;
  }

  function signOut() {
    clearToken();
    setUser(null);
  }

  return <AuthContext.Provider value={{ user, ready, signIn, signOut }}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error("Auth is missing");
  return value;
}
