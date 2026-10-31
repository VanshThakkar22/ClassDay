package schoolplatform.config;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import schoolplatform.domain.CityNews;
import schoolplatform.domain.ClassPaper;
import schoolplatform.domain.DailyWork;
import schoolplatform.domain.Enums;
import schoolplatform.domain.Grade;
import schoolplatform.domain.GuardianLink;
import schoolplatform.domain.Notice;
import schoolplatform.domain.School;
import schoolplatform.domain.Section;
import schoolplatform.domain.StudentProfile;
import schoolplatform.domain.TeacherAssignment;
import schoolplatform.domain.UserAccount;
import schoolplatform.repo.CityNewsRepository;
import schoolplatform.repo.ClassPaperRepository;
import schoolplatform.repo.DailyWorkRepository;
import schoolplatform.repo.GradeRepository;
import schoolplatform.repo.GuardianLinkRepository;
import schoolplatform.repo.NoticeRepository;
import schoolplatform.repo.SchoolRepository;
import schoolplatform.repo.SectionRepository;
import schoolplatform.repo.StudentProfileRepository;
import schoolplatform.repo.TeacherAssignmentRepository;
import schoolplatform.repo.UserAccountRepository;
import schoolplatform.service.FileStorageService;
import schoolplatform.service.SchoolService;
import schoolplatform.support.SimplePdf;

@Component
public class DataSeeder implements ApplicationRunner {
  private final UserAccountRepository users;
  private final SchoolRepository schools;
  private final GradeRepository grades;
  private final SectionRepository sections;
  private final TeacherAssignmentRepository assignments;
  private final StudentProfileRepository students;
  private final GuardianLinkRepository guardians;
  private final DailyWorkRepository dailyWork;
  private final ClassPaperRepository papers;
  private final NoticeRepository notices;
  private final CityNewsRepository cityNews;
  private final FileStorageService files;
  private final PasswordEncoder passwords;

  public DataSeeder(
      UserAccountRepository users,
      SchoolRepository schools,
      GradeRepository grades,
      SectionRepository sections,
      TeacherAssignmentRepository assignments,
      StudentProfileRepository students,
      GuardianLinkRepository guardians,
      DailyWorkRepository dailyWork,
      ClassPaperRepository papers,
      NoticeRepository notices,
      CityNewsRepository cityNews,
      FileStorageService files,
      PasswordEncoder passwords) {
    this.users = users;
    this.schools = schools;
    this.grades = grades;
    this.sections = sections;
    this.assignments = assignments;
    this.students = students;
    this.guardians = guardians;
    this.dailyWork = dailyWork;
    this.papers = papers;
    this.notices = notices;
    this.cityNews = cityNews;
    this.files = files;
    this.passwords = passwords;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (users.countByEmail("platform@classday.local") == 0) {
      seedSchools();
    }
    if (users.countByEmail("Student1C1SA@Rainbow") == 0) {
      seedRoster();
    }
  }

  private void seedSchools() {
    School north = school("Rainbow School");
    School river = school("Pinewood School");
    Map<String, Section> sectionByKey = new HashMap<>();
    Map<String, Grade> gradeByKey = new HashMap<>();
    for (School school : List.of(north, river)) {
      for (int level = 1; level <= 12; level++) {
        Grade grade = new Grade();
        grade.setSchool(school);
        grade.setLevel(level);
        grade = grades.save(grade);
        gradeByKey.put(key(school, level), grade);
        for (String name : List.of("A", "B", "C", "D", "E")) {
          Section section = new Section();
          section.setGrade(grade);
          section.setName(name);
          section = sections.save(section);
          sectionByKey.put(key(school, level) + "-" + name, section);
        }
      }
    }

    user("platform@classday.local", "Platform desk", Enums.Role.PLATFORM_ADMIN, null);
    user("admin.rainbow@classday.local", "Rainbow School office", Enums.Role.SCHOOL_ADMIN, north);
    user("admin.pinewood@classday.local", "Pinewood School office", Enums.Role.SCHOOL_ADMIN, river);
    UserAccount meera = user("meera.sharma@classday.local", "Meera Sharma", Enums.Role.TEACHER, north);
    UserAccount arjun = user("arjun.verma@classday.local", "Arjun Verma", Enums.Role.TEACHER, north);
    UserAccount neha = user("neha.kapoor@classday.local", "Neha Kapoor", Enums.Role.TEACHER, river);
    UserAccount aarav = user("aarav@classday.local", "Aarav Gupta", Enums.Role.STUDENT, north);
    UserAccount aaravParent = user("parent.aarav@classday.local", "Aarav's parent", Enums.Role.PARENT, north);
    UserAccount diya = user("diya@classday.local", "Diya Khan", Enums.Role.STUDENT, river);
    UserAccount diyaParent = user("parent.diya@classday.local", "Diya's parent", Enums.Role.PARENT, river);

    teach(meera, sectionByKey.get(key(north, 8) + "-A"), "Mathematics");
    teach(meera, sectionByKey.get(key(north, 8) + "-B"), "Mathematics");
    teach(arjun, sectionByKey.get(key(north, 8) + "-A"), "English");
    teach(neha, sectionByKey.get(key(river, 10) + "-A"), "Science");

    enroll(aarav, sectionByKey.get(key(north, 8) + "-A"), "RA-8A-01");
    enroll(diya, sectionByKey.get(key(river, 10) + "-A"), "PW-10A-01");
    link(aaravParent, aarav);
    link(diyaParent, diya);

    DailyWork maths = new DailyWork();
    maths.setTeacher(meera);
    maths.setSection(sectionByKey.get(key(north, 8) + "-A"));
    maths.setWorkDate(LocalDate.now(SchoolService.ZONE));
    maths.setSubject("Mathematics");
    maths.setNote("Exercise 6.2, questions 1 to 4. Linear equations.");
    maths.setOriginalFileName("class-8a-mathematics-today.pdf");
    maths.setStoredPath(
        files.storeBytes(
            maths.getOriginalFileName(),
            SimplePdf.text("Class 8A Mathematics - linear equations, exercise 6.2")));
    maths.setUploadedAt(Instant.now());
    dailyWork.save(maths);

    savePaper(
        gradeByKey.get(key(north, 8)),
        meera,
        "Class 8 mathematics sample paper",
        "class-8-mathematics-sample.pdf",
        "Sample paper for every section of Class 8.");
    savePaper(
        gradeByKey.get(key(river, 10)),
        neha,
        "Class 10 science sample paper",
        "class-10-science-sample.pdf",
        "Sample paper for every section of Class 10.");

    Notice schoolNotice = new Notice();
    schoolNotice.setSchool(north);
    schoolNotice.setAuthor(users.findByEmail("admin.rainbow@classday.local").orElseThrow());
    schoolNotice.setScope(Enums.Scope.SCHOOL);
    schoolNotice.setTitle("Friday PTM for Classes 6 to 8");
    schoolNotice.setBody("The meeting is at 10:00 in the hall. This notice is the record. No reply is needed.");
    schoolNotice.setCreatedAt(Instant.now());
    notices.save(schoolNotice);

    Notice cityEvent = new Notice();
    cityEvent.setAuthor(users.findByEmail("platform@classday.local").orElseThrow());
    cityEvent.setScope(Enums.Scope.CITY);
    cityEvent.setTitle("Inter-school quiz, 12 December");
    cityEvent.setBody("One team per school. Names can be sent to the school office. This is a city event, not a class circular.");
    cityEvent.setCreatedAt(Instant.now());
    notices.save(cityEvent);

    news("Sample: city science fair entries are open", Instant.now().minus(2, ChronoUnit.HOURS));
    news("Sample: district athletics list goes up this week", Instant.now().minus(8, ChronoUnit.HOURS));
    news("Old item that should stay off the 24 hour board", Instant.now().minus(36, ChronoUnit.HOURS));
  }

  private void seedRoster() {
    String passwordHash = passwords.encode("demo1234");
    List<School> ordered = schools.findAll().stream().sorted(Comparator.comparing(School::getId)).toList();
    for (int index = 0; index < ordered.size(); index++) {
      School school = ordered.get(index);
      int schoolNo = index + 1;
      String schoolTag = schoolNo == 1 ? "Rainbow" : "Pinewood";
      for (Grade grade : grades.findBySchoolIdOrderByLevelAsc(school.getId())) {
        for (Section section : sections.findByGradeIdOrderByNameAsc(grade.getId())) {
          String place = "C" + grade.getLevel() + "S" + section.getName();
          String teacherId = "Teacher" + place;
          UserAccount classTeacher = account(teacherId + "@" + schoolTag, teacherId, Enums.Role.TEACHER, school, passwordHash);
          section.setClassTeacher(classTeacher);
          sections.save(section);
          for (int number = 1; number <= 5; number++) {
            String studentId = "Student" + number + place;
            UserAccount student = account(studentId + "@" + schoolTag, studentId, Enums.Role.STUDENT, school, passwordHash);
            enroll(student, section, studentId);
            String parentId = "Parent" + number + place;
            UserAccount parent = account(parentId + "@" + schoolTag, parentId, Enums.Role.PARENT, school, passwordHash);
            link(parent, student);
          }
        }
      }
    }
  }

  private School school(String name) {
    School school = new School();
    school.setName(name);
    school.setCity(SchoolService.CITY);
    school.setPlan(Enums.Plan.CAMPUS);
    return schools.save(school);
  }

  private UserAccount user(String email, String name, Enums.Role role, School school) {
    return account(email, name, role, school, passwords.encode("demo1234"));
  }

  private UserAccount account(String email, String name, Enums.Role role, School school, String passwordHash) {
    UserAccount user = new UserAccount();
    user.setEmail(email);
    user.setFullName(name);
    user.setRole(role);
    user.setSchool(school);
    user.setPasswordHash(passwordHash);
    return users.save(user);
  }

  private void teach(UserAccount teacher, Section section, String subject) {
    TeacherAssignment assignment = new TeacherAssignment();
    assignment.setTeacher(teacher);
    assignment.setSection(section);
    assignment.setSubject(subject);
    assignments.save(assignment);
  }

  private void enroll(UserAccount student, Section section, String admissionNo) {
    StudentProfile profile = new StudentProfile();
    profile.setUser(student);
    profile.setSection(section);
    profile.setAdmissionNo(admissionNo);
    students.save(profile);
  }

  private void link(UserAccount parent, UserAccount student) {
    GuardianLink link = new GuardianLink();
    link.setParent(parent);
    link.setStudent(student);
    guardians.save(link);
  }

  private void savePaper(Grade grade, UserAccount uploader, String title, String filename, String line) {
    ClassPaper paper = new ClassPaper();
    paper.setGrade(grade);
    paper.setUploadedBy(uploader);
    paper.setTitle(title);
    paper.setOriginalFileName(filename);
    paper.setStoredPath(files.storeBytes(filename, SimplePdf.text(line)));
    paper.setCreatedAt(Instant.now());
    papers.save(paper);
  }

  private void news(String title, Instant publishedAt) {
    CityNews item = new CityNews();
    item.setCity(SchoolService.CITY);
    item.setTitle(title);
    item.setSourceName("Demo wire");
    item.setSourceUrl("https://example.com/saharanpur");
    item.setSummary("Placeholder until a licensed Saharanpur news feed is connected. The article itself stays on the source site.");
    item.setPublishedAt(publishedAt);
    cityNews.save(item);
  }

  private static String key(School school, int level) {
    return school.getId() + "-" + level;
  }
}
