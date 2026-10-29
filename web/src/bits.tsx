import { useState } from "react";
import { fetchPdf } from "./api";

export function Bell({ small = false }: { small?: boolean }) {
  return (
    <svg className={small ? "bell bell-small" : "bell"} viewBox="0 0 48 48" aria-hidden="true">
      <path d="M24 6a10 10 0 0 0-10 10c0 7-3.2 9.2-3.2 14h26.4c0-4.8-3.2-7-3.2-14A10 10 0 0 0 24 6z" />
      <path d="M20 33a4 4 0 0 0 8 0" />
    </svg>
  );
}

export function Problem({ message }: { message: string }) {
  if (!message) return null;
  return <p className="problem">{message}</p>;
}

export function useReader() {
  const [sheet, setSheet] = useState<{ url: string; title: string } | null>(null);
  const [problem, setProblem] = useState("");

  async function open(path: string, title: string) {
    setProblem("");
    try {
      const url = await fetchPdf(path);
      setSheet((current) => {
        if (current) URL.revokeObjectURL(current.url);
        return { url, title };
      });
    } catch (error) {
      setProblem(error instanceof Error ? error.message : "That sheet could not be opened.");
    }
  }

  function close() {
    setSheet((current) => {
      if (current) URL.revokeObjectURL(current.url);
      return null;
    });
  }

  const reader = sheet ? (
    <div className="reader" role="dialog" aria-modal="true" aria-label={sheet.title}>
      <div className="reader-bar">
        <strong>{sheet.title}</strong>
        <button type="button" className="quiet" onClick={close}>
          Close the sheet
        </button>
      </div>
      <iframe title={sheet.title} src={sheet.url} />
    </div>
  ) : null;

  return { open, reader, problem };
}

export function PdfDrop({
  file,
  onFile,
  idle
}: {
  file: File | null;
  onFile: (file: File | null) => void;
  idle: string;
}) {
  return (
    <label
      className="drop"
      onDragOver={(event) => event.preventDefault()}
      onDrop={(event) => {
        event.preventDefault();
        const next = event.dataTransfer.files?.[0];
        if (next) onFile(next);
      }}
    >
      <input
        type="file"
        accept="application/pdf,.pdf"
        onChange={(event) => onFile(event.target.files?.[0] ?? null)}
      />
      <span>{file ? file.name : idle}</span>
    </label>
  );
}
