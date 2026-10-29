# Openbell

A school diary for Saharanpur. Teachers send the day’s sheet, or nothing. Absent students catch up without a night of messages. Two fictional schools share one product, and each school’s families stay private.

This is a prototype. Fees, attendance, exams, transport, timetable, and library are buttons on **Later**. They are not built yet.

## Run it

Install [Docker Desktop](https://www.docker.com/products/docker-desktop/), then from this folder:

```bash
docker compose up --build
```

The first build takes a few minutes. Then open http://localhost:3000

On another laptop: install Docker Desktop, copy or clone this folder, and run the same command. You do not need Java, Python, or Node installed on that machine.

Stop it with Ctrl+C, then `docker compose down`. To wipe the demo and start clean:

```bash
docker compose down -v
```

## Sign in

Every demo password is `openbell`.

| Person | Email | What you should see |
|---|---|---|
| Meera Sharma | meera@northline.school | Maths sent today for Class 8 A and 8 B. Arjun has sent nothing, and that is allowed. |
| Arjun Malhotra | arjun@northline.school | English teacher on 8 A. Yesterday’s sheet is on the diary, not forced onto today. |
| Aman Sharma | aman@northline.school | Student. Today shows Meera’s PDF. Yesterday shows Maths and English. |
| Leela Sharma | leela@northline.school | Aman’s mother. Same diary. Opening it marks the school notes as seen. |
| Sana Siddiqui | sana@northline.school | Office. Classes 1–12, five sections each. Notes to a section, a class, or the school. |
| Kabir Qureshi | kabir@riverbend.school | The second school. Hindi for Class 5 C. |
| City desk | desk@openbell.school | Both schools, plus a Saharanpur event. |

Also in the login list: Riya (Class 10 sample paper for every section), Neel and Pooja, Nabil, Harish, Isha, and Farah.

The schools are invented. They are not the real schools of Saharanpur.

## What is running

| Piece | Role |
|---|---|
| Web | The screens, on port 3000 |
| Java API | Schools, logins, sheets, notes. Port 8080 |
| Python | Keeps the sample city items inside the last 24 hours |
| MySQL 8 | The database, on port 3306 |

School dates use Asia/Kolkata. PDFs live in a Docker volume, and MySQL only stores the path.

The city lines are samples, marked on the page. A newspaper you are allowed to use can replace them later in `ingest/keep_fresh.py`. The screen stays the same.
