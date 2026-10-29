package school.openbell.service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.openbell.domain.CityNews;
import school.openbell.domain.ClassPaper;
import school.openbell.domain.DailyWork;
import school.openbell.domain.Enums;
import school.openbell.domain.Grade;
import school.openbell.domain.Guardian;
import school.openbell.domain.Notice;
import school.openbell.domain.School;
import school.openbell.domain.Section;
import school.openbell.domain.Student;
import school.openbell.domain.TeacherSection;
import school.openbell.domain.UserAccount;
import school.openbell.repo.CityNewsRepo;
import school.openbell.repo.ClassPaperRepo;
import school.openbell.repo.DailyWorkRepo;
import school.openbell.repo.GradeRepo;
import school.openbell.repo.GuardianRepo;
import school.openbell.repo.NoticeRepo;
import school.openbell.repo.SchoolRepo;
import school.openbell.repo.SectionRepo;
import school.openbell.repo.StudentRepo;
import school.openbell.repo.TeacherSectionRepo;
import school.openbell.repo.UserRepo;
import school.openbell.support.PlainPdf;
import school.openbell.support.SchoolClock;

@Service
public class SeedService {
  private static final Logger log = LoggerFactory.getLogger(SeedService.class);

  private final UserRepo users;
  private final SchoolRepo schools;
  private final GradeRepo grades;
  private final SectionRepo sections;
  private final TeacherSectionRepo teaching;
  private final StudentRepo students;
  private final GuardianRepo guardians;
  private final DailyWorkRepo work;
  private final ClassPaperRepo papers;
  private final NoticeRepo notices;
  private final CityNewsRepo news;
  private final PasswordEncoder passwords;
  private final FileStorage files;

  public SeedService(
      UserRepo users,
      SchoolRepo schools,
      GradeRepo grades,
      SectionRepo sections,
      TeacherSectionRepo teaching,
      StudentRepo students,
      GuardianRepo guardians,
      DailyWorkRepo work,
      ClassPaperRepo papers,
      NoticeRepo notices,
      CityNewsRepo news,
      PasswordEncoder passwords,
      FileStorage files) {
    this.users = users;
    this.schools = schools;
    this.grades = grades;
    this.sections = sections;
    this.teaching = teaching;
    this.students = students;
    this.guardians = guardians;
    this.work = work;
    this.papers = papers;
    this.notices = notices;
    this.news = news;
    this.passwords = passwords;
    this.files = files;
  }

  @Transactional
  public void seed() {
    if (users.count() > 0) {
      return;
    }
    School north = school("Northline Public School", "Civil Lines");
    School river = school("Riverbend Academy", "Delhi Road");
    Map<String, Section> n = structure(north);
    Map<String, Section> r = structure(river);

    UserAccount meera = person(north, "Meera Sharma", "meera@northline.school", Enums.Role.TEACHER);
    UserAccount arjun = person(north, "Arjun Malhotra", "arjun@northline.school", Enums.Role.TEACHER);
    UserAccount riya = person(north, "Riya Kapoor", "riya@northline.school", Enums.Role.TEACHER);
    UserAccount sana = person(north, "Sana Siddiqui", "sana@northline.school", Enums.Role.SCHOOL_ADMIN);
    UserAccount amanLogin = person(north, "Aman Sharma", "aman@northline.school", Enums.Role.STUDENT);
    UserAccount leela = person(north, "Leela Sharma", "leela@northline.school", Enums.Role.PARENT);
    UserAccount nabil = person(north, "Nabil Ali", "nabil@northline.school", Enums.Role.PARENT);
    UserAccount neelLogin = person(north, "Neel Verma", "neel@northline.school", Enums.Role.STUDENT);
    UserAccount pooja = person(north, "Pooja Verma", "pooja@northline.school", Enums.Role.PARENT);

    UserAccount kabir = person(river, "Kabir Qureshi", "kabir@riverbend.school", Enums.Role.TEACHER);
    person(river, "Harish Tomar", "harish@riverbend.school", Enums.Role.SCHOOL_ADMIN);
    UserAccount ishaLogin = person(river, "Isha Khan", "isha@riverbend.school", Enums.Role.STUDENT);
    UserAccount farah = person(river, "Farah Khan", "farah@riverbend.school", Enums.Role.PARENT);
    person(null, "Openbell desk", "desk@openbell.school", Enums.Role.PLATFORM_ADMIN);

    teach(meera, n.get("8A"), "Maths");
    teach(meera, n.get("8B"), "Maths");
    teach(arjun, n.get("8A"), "English");
    teach(riya, n.get("10A"), "Science");
    teach(kabir, r.get("5C"), "Hindi");

    Student aman = child(north, n.get("8A"), "Aman Sharma", amanLogin);
    parentOf(aman, leela);
    Student sara = child(north, n.get("8A"), "Sara Ali", null);
    parentOf(sara, nabil);
    child(north, n.get("8B"), "Rohan Gupta", null);
    Student neel = child(north, n.get("10A"), "Neel Verma", neelLogin);
    parentOf(neel, pooja);
    Student isha = child(river, r.get("5C"), "Isha Khan", ishaLogin);
    parentOf(isha, farah);

    LocalDate today = SchoolClock.today();
    sheet(n.get("8A"), meera, "Maths", today,
        "Exercise 4.2, questions 1 to 8. If you were away, the worked example on page 72 is enough.",
        "Maths, Class 8 A", "class-8a-maths.pdf");
    sheet(n.get("8B"), meera, "Maths", today,
        "Same exercise as 8 A. Stop after question 6.",
        "Maths, Class 8 B", "class-8b-maths.pdf");
    sheet(n.get("8A"), meera, "Maths", today.minusDays(1),
        "We finished the examples on pages 68 and 69.",
        "Maths, yesterday", "class-8a-maths-yesterday.pdf");
    sheet(n.get("8A"), arjun, "English", today.minusDays(1),
        "Read the passage on page 40 and answer the three questions in the margin.",
        "English, Class 8 A", "class-8a-english.pdf");
    sheet(r.get("5C"), kabir, "Hindi", today,
        "The two paragraphs we wrote in class are on this sheet.",
        "Hindi, Class 5 C", "class-5c-hindi.pdf");

    ClassPaper paper = new ClassPaper();
    paper.setGrade(n.get("10A").getGrade());
    paper.setTeacher(riya);
    paper.setTitle("Science sample paper");
    paper.setNote("One paper for every section of Class 10. One hour, no calculator.");
    paper.setOriginalName("class-10-science-sample.pdf");
    paper.setStoredName(files.store(PlainPdf.of("Class 10 science sample", "Section A and every other section get this same paper.")));
    paper.setCreatedAt(Instant.now());
    papers.save(paper);

    note(meera, Enums.Scope.SECTION, north, n.get("8A"), null,
        "Bring a geometry box tomorrow",
        "We start constructions in the first period. If you were away today, the maths sheet is enough for tonight.");
    note(sana, Enums.Scope.SCHOOL, north, null, null,
        "Friday is a half day",
        "Classes end at noon. There will not be another reminder in the evening.");
    note(sana, Enums.Scope.GRADE, north, null, n.get("10A").getGrade(),
        "The science sample is with every section",
        "Class 10 does not need a separate file for A, B, C, D, and E.");
    UserAccount desk = users.findByEmail("desk@openbell.school").orElseThrow();
    note(desk, Enums.Scope.CITY, null, null, null,
        "Inter-school debate, Saturday",
        "Two students from a school. City library, 10 in the morning. Send names to your own office, not into a group chat.");

    wire("A calmer evening on Delhi Road", "Sample brief for the city board. A newspaper feed can replace this line later.", 2);
    wire("Shops in Civil Lines stayed open past seven", "Sample brief. This is not a report from a paper.", 5);
    wire("Practice laps at the school sports ground", "Sample brief, standing in for a local sports note.", 9);
    wire("The night is turning cooler", "Sample brief. Weather copy will come from a source you choose.", 15);
    wire("City library opens at nine on Saturday", "Sample brief, beside the debate the schools can already see.", 20);

    log.info("Openbell demo is ready. Password for every person: openbell");
  }

  private School school(String name, String area) {
    School school = new School();
    school.setName(name);
    school.setCity("Saharanpur");
    school.setArea(area);
    school.setPlan(Enums.Plan.CAMPUS);
    return schools.save(school);
  }

  private Map<String, Section> structure(School school) {
    Map<String, Section> map = new HashMap<>();
    for (int level = 1; level <= 12; level++) {
      Grade grade = new Grade();
      grade.setSchool(school);
      grade.setLevel(level);
      grade.setLabel("Class " + level);
      grade = grades.save(grade);
      for (String name : List.of("A", "B", "C", "D", "E")) {
        Section section = new Section();
        section.setGrade(grade);
        section.setName(name);
        map.put(level + name, sections.save(section));
      }
    }
    return map;
  }

  private UserAccount person(School school, String name, String email, Enums.Role role) {
    UserAccount user = new UserAccount();
    user.setSchool(school);
    user.setName(name);
    user.setEmail(email);
    user.setRole(role);
    user.setPasswordHash(passwords.encode("openbell"));
    return users.save(user);
  }

  private void teach(UserAccount teacher, Section section, String subject) {
    TeacherSection row = new TeacherSection();
    row.setTeacher(teacher);
    row.setSection(section);
    row.setSubject(subject);
    teaching.save(row);
  }

  private Student child(School school, Section section, String name, UserAccount login) {
    Student student = new Student();
    student.setSchool(school);
    student.setSection(section);
    student.setName(name);
    student.setUser(login);
    return students.save(student);
  }

  private void parentOf(Student student, UserAccount parent) {
    Guardian guardian = new Guardian();
    guardian.setStudent(student);
    guardian.setParent(parent);
    guardians.save(guardian);
  }

  private void sheet(Section section, UserAccount teacher, String subject, LocalDate date, String note, String pdfTitle, String filename) {
    DailyWork row = new DailyWork();
    row.setSection(section);
    row.setTeacher(teacher);
    row.setSubject(subject);
    row.setWorkDate(date);
    row.setNote(note);
    row.setOriginalName(filename);
    row.setStoredName(files.store(PlainPdf.of(pdfTitle, note)));
    row.setCreatedAt(Instant.now());
    work.save(row);
  }

  private void note(UserAccount author, Enums.Scope scope, School school, Section section, Grade grade, String title, String body) {
    Notice notice = new Notice();
    notice.setAuthor(author);
    notice.setScope(scope);
    notice.setSchool(school);
    notice.setSection(section);
    notice.setGrade(grade);
    notice.setTitle(title);
    notice.setBody(body);
    notice.setCreatedAt(Instant.now());
    notices.save(notice);
  }

  private void wire(String title, String summary, int hoursAgo) {
    CityNews row = new CityNews();
    row.setTitle(title);
    row.setSourceName("Openbell city desk");
    row.setSourceUrl("https://example.com/openbell/saharanpur");
    row.setSummary(summary);
    row.setPublishedAt(Instant.now().minus(Duration.ofHours(hoursAgo)));
    row.setCity("Saharanpur");
    row.setSample(true);
    news.save(row);
  }
}

@Component
class StartupSeed implements CommandLineRunner {
  private final SeedService seed;

  StartupSeed(SeedService seed) {
    this.seed = seed;
  }

  @Override
  public void run(String... args) {
    seed.seed();
  }
}
