export type Role = "PLATFORM_ADMIN" | "SCHOOL_ADMIN" | "TEACHER" | "PARENT" | "STUDENT";

export type User = {
  id: number;
  name: string;
  email: string;
  role: Role;
  schoolId: number | null;
  schoolName: string;
  plan: string | null;
  serverDate: string;
  dateLabel: string;
};

export type WorkFile = {
  id: number;
  subject: string;
  teacherName: string;
  note: string | null;
  filename: string;
};

export type Colleague = {
  teacherName: string;
  subject: string;
  fileId: number | null;
  filename: string | null;
  note: string | null;
};

export type TodaySection = {
  sectionId: number;
  gradeLabel: string;
  sectionName: string;
  subject: string;
  mine: WorkFile | null;
  colleagues: Colleague[];
};

export type TodayDesk = { date: string; dateLabel: string; sections: TodaySection[] };

export type GradeOption = { gradeId: number; label: string };

export type Paper = {
  id: number;
  gradeId: number;
  gradeLabel: string;
  title: string;
  note: string | null;
  filename: string;
  teacherName: string;
  createdAt: string;
};

export type Notice = {
  id: number;
  title: string;
  body: string;
  scope: string;
  targetLabel: string;
  authorName: string;
  createdAt: string;
  seenCount: number;
  recipientCount: number;
  seen: boolean;
};

export type StudentChip = {
  id: number;
  name: string;
  gradeLabel: string;
  sectionName: string;
  schoolName: string;
};

export type Diary = {
  date: string;
  dateLabel: string;
  students: StudentChip[];
  selectedStudentId: number;
  work: WorkFile[];
  papers: Paper[];
  notices: Notice[];
};

export type SectionFolder = { id: number; name: string; teachers: string[]; students: number };
export type GradeFolder = { id: number; level: number; label: string; sections: SectionFolder[] };
export type SchoolTree = {
  id: number;
  name: string;
  area: string;
  city: string;
  plan: string;
  grades: GradeFolder[];
};
export type SchoolCard = { id: number; name: string; area: string; plan: string; grades: number; sections: number };
export type NewsItem = {
  id: number;
  title: string;
  sourceName: string;
  summary: string;
  publishedAt: string;
  sample: boolean;
};
export type CityPage = { news: NewsItem[]; events: Notice[] };

export class ApiError extends Error {}

const TOKEN = "openbell_token";

export function token() {
  return localStorage.getItem(TOKEN);
}

export function saveToken(value: string) {
  localStorage.setItem(TOKEN, value);
}

export function clearToken() {
  localStorage.removeItem(TOKEN);
}

export async function api<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);
  const current = token();
  if (current) headers.set("Authorization", `Bearer ${current}`);
  if (options.body && !(options.body instanceof FormData) && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }
  const response = await fetch(path, { ...options, headers });
  if (response.status === 401 && !path.startsWith("/api/auth/")) {
    clearToken();
    if (!window.location.pathname.startsWith("/login")) window.location.assign("/login");
    throw new ApiError("Please sign in again.");
  }
  if (!response.ok) {
    const data = (await response.json().catch(() => null)) as { message?: string } | null;
    throw new ApiError(data?.message || "Something went wrong.");
  }
  if (response.status === 204) return undefined as T;
  return response.json() as Promise<T>;
}

export async function fetchPdf(path: string) {
  const headers = new Headers();
  const current = token();
  if (current) headers.set("Authorization", `Bearer ${current}`);
  const response = await fetch(path, { headers });
  if (!response.ok) throw new ApiError("That sheet could not be opened.");
  return URL.createObjectURL(await response.blob());
}

export function shiftDate(iso: string, days: number) {
  const [year, month, day] = iso.split("-").map(Number);
  const date = new Date(Date.UTC(year, month - 1, day));
  date.setUTCDate(date.getUTCDate() + days);
  return date.toISOString().slice(0, 10);
}

export function clock(iso: string) {
  return new Intl.DateTimeFormat("en-IN", {
    timeZone: "Asia/Kolkata",
    hour: "numeric",
    minute: "2-digit"
  }).format(new Date(iso));
}

export function planName(plan: string | null) {
  if (!plan) return "";
  return plan.charAt(0) + plan.slice(1).toLowerCase();
}
