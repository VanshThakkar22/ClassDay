# ClassDay

A school diary for Saharanpur. Two fictional schools, Rainbow and Pinewood, share one product. Each school’s students stay private. Classes run from 1 to 12, with sections A to E.

A teacher uploads one PDF for a section and subject, or uploads nothing when the day was quiet. A sample paper is sent once to a class, so every section of that class can open it. The school office assigns who teaches which subject, and which one section that teacher is class teacher of. Pending fees are entered by that class teacher. Notices inside a school stay on Notices. A notice sent to every school appears on City.

Attendance and exams are still buttons only. They are not built yet.

## Start the services

Install [Docker Desktop](https://www.docker.com/products/docker-desktop/), Java 17, Maven, and Node.js. Then start the three pieces in this order, each in its own terminal. The API needs MySQL, and the screens need the API.

1. Database, from this folder:

```powershell
docker compose up -d
```

MySQL 8 listens on port 3307. Wait until the container is healthy before the next step. Check with `docker compose ps`.

2. API, from `backend`:

```powershell
mvn spring-boot:run
```

The API listens on port 8080. The first start creates the demo schools and accounts.

3. Screens, from `web`:

```powershell
npm install
npm run dev
```

`npm install` is only needed the first time. Open http://localhost:5173

Stop the API and the screens with Ctrl+C. Stop MySQL with `docker compose down` from this folder. Adding `-v` also deletes the database.

## Sign in

Every demo password is `demo1234`.

| Desk | Email |
|---|---|
| Mathematics, Class 8-A and 8-B | meera.sharma@classday.local |
| English, Class 8-A | arjun.verma@classday.local |
| Student in Class 8-A | aarav@classday.local |
| That student's parent | parent.aarav@classday.local |
| Rainbow School office | admin.rainbow@classday.local |
| Pinewood School office | admin.pinewood@classday.local |
| Science, Pinewood Class 10-A | neha.kapoor@classday.local |
| Student in Pinewood Class 10-A | diya@classday.local |
| City desk | platform@classday.local |

Each section also has a class teacher. The id is `Teacher`, then `C`, the class, `S`, and the section, at `Rainbow` or `Pinewood`. Class 8-A at Rainbow is `TeacherC8SA@Rainbow`. The password is the same.

Meera’s mathematics PDF for today is already there. Arjun’s English slot is empty, which is a valid day. Class 8 and Pinewood Class 10 each have one sample paper.

## City headlines

After the API is running, a sample Saharanpur headline can be added from Python:

```powershell
cd news-ingest
python -m pip install -r requirements.txt
python ingest_city_news.py
```

The script stores a headline, source, link, and one-line summary. It does not copy a newspaper article. Headlines from the last 24 hours show in the sidebar.
