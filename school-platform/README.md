# Classday prototype

One system for many Saharanpur schools. This first build has two schools, Class 1 to Class 12, and sections A to E. Each school's students stay private. The shared screen is the city board.

A section can have several teachers. At the end of the day a teacher may upload one PDF of what happened in class. If nothing happened, they upload nothing. An absent student opens that day and reads whatever was filed. A sample paper is sent once to a class, so it reaches every section without five uploads.

There is no class chat.

## Stack

- Screens: TypeScript, React, Vite
- School API: Java 17, Spring Boot
- City feed job: Python
- Database: MySQL 8, published on port 3307 because 3306 was already in use on this machine

## Run

From `school-platform`:

```powershell
docker compose up -d
cd backend
mvn spring-boot:run
```

In a second terminal:

```powershell
cd web
npm install
npm run dev
```

Open http://localhost:5173

Password for every demo account is `demo1234`.

| Desk | Email |
|---|---|
| Mathematics teacher, Class 8-A and 8-B | meera.sharma@classday.local |
| English teacher, same Class 8-A | arjun.verma@classday.local |
| Student in Class 8-A | aarav@classday.local |
| That student's parent | parent.aarav@classday.local |
| North Ridge office | admin.north@classday.local |
| Science teacher, Riverbank Class 10-A | neha.kapoor@classday.local |
| City desk | platform@classday.local |

Meera's mathematics PDF for today is already there. Arjun's English slot is empty, which is a valid day. Class 8 has one sample paper for every section. Riverbank Class 10 has one too. Diya (`diya@classday.local`) is the student who sees that paper and an empty day.

To add a city headline from Python:

```powershell
cd news-ingest
python -m pip install -r requirements.txt
python ingest_city_news.py
```

The script stores a headline, source, link, and one-line summary. It does not copy a newspaper article. The real Saharanpur feed can replace the sample item in that script later.

## What is left for a later build

Fees, attendance, exams, and a phone-store app are not in this prototype. The same schools, users, and classes are where those modules would attach.
