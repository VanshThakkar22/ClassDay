export type User = {
  id: number;
  fullName: string;
  email: string;
  role: string;
  schoolName: string;
  schoolId: number | null;
};

export type DailyFile = {
  id: number;
  subject: string;
  teacherName: string;
  fileName: string;
  note: string;
  uploadedAt: string;
};

export type TeacherCard = {
  sectionId: number;
  gradeId: number;
  classLevel: number;
  sectionName: string;
  classLabel: string;
  subject: string;
  today: DailyFile | null;
};

export type PaperFile = {
  id: number;
  title: string;
  fileName: string;
  classLabel: string;
  uploadedBy: string;
  createdAt: string;
  uploadedById: number;
};

export type Notice = {
  id: number;
  title: string;
  body: string;
  scope: string;
  targetLabel: string;
  createdAt: string;
  seenCount: number | null;
  audienceCount: number | null;
  authorId: number;
};

export type Duty = {
  id: number;
  teacherId: number;
  teacherName: string;
  subject: string;
};

export type SectionLine = {
  sectionId: number;
  name: string;
  teachers: string[];
  filesToday: number;
  studentCount: number;
  classTeacherId: number | null;
  duties: Duty[];
  classTeacherName: string;
  uploads: { subject: string; teacherName: string }[];
};

export type Staff = {
  id: number;
  fullName: string;
};

export type GradeBlock = {
  gradeId: number;
  level: number;
  sections: SectionLine[];
};

export type SchoolCard = {
  id: number;
  name: string;
  plan: string;
  classes: number;
  sections: number;
};

export type NewsItem = {
  id: number;
  title: string;
  sourceName: string;
  sourceUrl: string;
  summary: string;
  publishedAt: string;
  scope: string;
};

export type FeeLine = {
  studentId: number;
  studentName: string;
  classLabel: string;
  schoolFee: number;
  busFee: number;
  otherFee: number;
  otherReason: string;
};

export type FeeBoard = {
  lines: FeeLine[];
  canEdit: boolean;
  canRemind: boolean;
  note: string;
};

export type Home = {
  user: User;
  date: string;
  focus: string;
  teacherCards: TeacherCard[];
  todayWork: DailyFile[];
  classPapers: PaperFile[];
  notices: Notice[];
  grades: GradeBlock[];
  schools: SchoolCard[];
  cityNews: NewsItem[];
  sectionLabel: string;
  teachers: Staff[];
};

async function readError(response: Response): Promise<string> {
  try {
    const body = (await response.json()) as { error?: string };
    return body.error || "Something went wrong.";
  } catch {
    return "Something went wrong.";
  }
}

export async function login(email: string, password: string): Promise<{ token: string; user: User }> {
  const response = await fetch("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password }),
  });
  if (!response.ok) throw new Error(await readError(response));
  return response.json();
}

export async function loadHome(token: string, date: string): Promise<Home> {
  const response = await fetch(`/api/home?date=${date}`, {
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) throw new Error(await readError(response));
  return response.json();
}

export async function uploadDaily(
  token: string,
  sectionId: number,
  subject: string,
  note: string,
  workDate: string,
  file: File,
): Promise<void> {
  const body = new FormData();
  body.set("sectionId", String(sectionId));
  body.set("subject", subject);
  body.set("note", note);
  body.set("workDate", workDate);
  body.set("file", file);
  const response = await fetch("/api/daily-work", {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
    body,
  });
  if (!response.ok) throw new Error(await readError(response));
}

export async function uploadPaper(token: string, gradeId: number, title: string, file: File): Promise<void> {
  const body = new FormData();
  body.set("gradeId", String(gradeId));
  body.set("title", title);
  body.set("file", file);
  const response = await fetch("/api/class-papers", {
    method: "POST",
    headers: { Authorization: `Bearer ${token}` },
    body,
  });
  if (!response.ok) throw new Error(await readError(response));
}

export async function deletePaper(token: string, id: number): Promise<void> {
  const response = await fetch(`/api/class-papers/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) throw new Error(await readError(response));
}

export async function deleteNotice(token: string, id: number): Promise<void> {
  const response = await fetch(`/api/notices/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) throw new Error(await readError(response));
}

export async function assignTeacher(
  token: string,
  payload: { sectionId: number; teacherId: number; subject: string; classTeacher: boolean },
): Promise<void> {
  const response = await fetch("/api/assignments", {
    method: "POST",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) throw new Error(await readError(response));
}

export async function removeAssignment(token: string, id: number): Promise<void> {
  const response = await fetch(`/api/assignments/${id}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${token}` },
  });
  if (!response.ok) throw new Error(await readError(response));
}

export async function postNotice(
  token: string,
  payload: { scope: string; sectionId?: number; gradeId?: number; title: string; body: string },
): Promise<void> {
  const response = await fetch("/api/notices", {
    method: "POST",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!response.ok) throw new Error(await readError(response));
}

export async function loadFees(token: string): Promise<FeeBoard> {
  const response = await fetch("/api/fees", { headers: { Authorization: `Bearer ${token}` } });
  if (!response.ok) throw new Error(await readError(response));
  return response.json();
}

export async function saveFee(token: string, line: FeeLine): Promise<FeeLine> {
  const response = await fetch(`/api/fees/${line.studentId}`, {
    method: "PUT",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    body: JSON.stringify({
      schoolFee: line.schoolFee,
      busFee: line.busFee,
      otherFee: line.otherFee,
      otherReason: line.otherReason,
    }),
  });
  if (!response.ok) throw new Error(await readError(response));
  return response.json();
}

export async function remindFees(token: string, title: string, body: string): Promise<number> {
  const response = await fetch("/api/fees/remind", {
    method: "POST",
    headers: { Authorization: `Bearer ${token}`, "Content-Type": "application/json" },
    body: JSON.stringify({ title, body }),
  });
  if (!response.ok) throw new Error(await readError(response));
  const payload = (await response.json()) as { sent: number };
  return payload.sent;
}

export async function openPdf(token: string, path: string): Promise<void> {
  const response = await fetch(path, { headers: { Authorization: `Bearer ${token}` } });
  if (!response.ok) throw new Error(await readError(response));
  const blob = await response.blob();
  const url = URL.createObjectURL(blob);
  window.open(url, "_blank", "noopener");
  window.setTimeout(() => URL.revokeObjectURL(url), 60_000);
}

export function todayInSaharanpur(): string {
  return new Intl.DateTimeFormat("en-CA", { timeZone: "Asia/Kolkata" }).format(new Date());
}

export function clock(iso: string): string {
  return new Intl.DateTimeFormat("en-IN", {
    hour: "numeric",
    minute: "2-digit",
    timeZone: "Asia/Kolkata",
  }).format(new Date(iso));
}
