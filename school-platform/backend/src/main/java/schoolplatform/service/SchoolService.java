package schoolplatform.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import schoolplatform.config.AppProperties;
import schoolplatform.domain.CityNews;
import schoolplatform.domain.ClassPaper;
import schoolplatform.domain.DailyWork;
import schoolplatform.domain.Enums;
import schoolplatform.domain.Grade;
import schoolplatform.domain.GuardianLink;
import schoolplatform.domain.Notice;
import schoolplatform.domain.NoticeReceipt;
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
import schoolplatform.repo.NoticeReceiptRepository;
import schoolplatform.repo.NoticeRepository;
import schoolplatform.repo.SchoolRepository;
import schoolplatform.repo.SectionRepository;
import schoolplatform.repo.StudentProfileRepository;
import schoolplatform.repo.TeacherAssignmentRepository;
import schoolplatform.repo.UserAccountRepository;
import schoolplatform.security.JwtService;
import schoolplatform.support.ForbiddenException;
import schoolplatform.web.Dto;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class SchoolService {
  public static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
  public static final String CITY = "Saharanpur";

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
  private final NoticeReceiptRepository receipts;
  private final CityNewsRepository cityNews;
  private final FileStorageService files;
  private final PasswordEncoder passwords;
  private final JwtService jwt;
  private final AppProperties properties;

  public SchoolService(
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
      NoticeReceiptRepository receipts,
      CityNewsRepository cityNews,
      FileStorageService files,
      PasswordEncoder passwords,
      JwtService jwt,
      AppProperties properties) {
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
    this.receipts = receipts;
    this.cityNews = cityNews;
    this.files = files;
    this.passwords = passwords;
    this.jwt = jwt;
    this.properties = properties;
  }

  @Transactional
  public Dto.LoginResponse login(String email, String password) {
    UserAccount user =
        users
            .findByEmail(email == null ? "" : email.trim().toLowerCase())
            .orElseThrow(() -> new IllegalArgumentException("Those details do not match."));
    if (!passwords.matches(password == null ? "" : password, user.getPasswordHash())) {
      throw new IllegalArgumentException("Those details do not match.");
    }
    return new Dto.LoginResponse(jwt.create(user), view(user));
  }

  @Transactional
  public Dto.HomeResponse home(UserAccount current, LocalDate requestedDate) {
    UserAccount user = fresh(current);
    LocalDate date = requestedDate == null ? LocalDate.now(ZONE) : requestedDate;
    Enums.Role role = user.getRole();

    List<Dto.TeacherCard> teacherCards = List.of();
    List<Dto.DailyFile> todayWork = List.of();
    List<Dto.PaperFile> classPapers = List.of();
    List<Dto.GradeBlock> gradeBlocks = List.of();
    List<Dto.SchoolCard> schoolCards = List.of();
    String sectionLabel = "";
    String focus = "";

    if (role == Enums.Role.TEACHER) {
      focus = "Upload today's class PDF, or skip the day if nothing happened.";
      teacherCards = teacherCards(user, date);
      classPapers = papersForGrades(taughtGradeIds(user));
    } else if (role == Enums.Role.STUDENT || role == Enums.Role.PARENT) {
      focus = "If you were absent, open the PDF. If a subject has no file, nothing was shared.";
      List<StudentProfile> kids = childrenOf(user);
      sectionLabel =
          kids.stream().map(profile -> classLabel(profile.getSection())).distinct().reduce((a, b) -> a + ", " + b).orElse("");
      todayWork = workForProfiles(kids, date);
      classPapers = papersForGrades(kids.stream().map(profile -> profile.getSection().getGrade().getId()).distinct().toList());
    } else if (role == Enums.Role.SCHOOL_ADMIN) {
      focus = "An empty section is a normal day. Teachers are not chased for a file.";
      gradeBlocks = structure(user.getSchool().getId(), date);
      classPapers = papers.findByGrade_School_IdOrderByCreatedAtDesc(user.getSchool().getId()).stream()
          .limit(20)
          .map(this::paperView)
          .toList();
    } else {
      focus = "One system for every school. Each school's students stay private.";
      schoolCards = schools.findAll().stream().map(this::schoolCard).toList();
    }

    List<Dto.NoticeView> noticeViews = visibleNotices(user);
    List<Dto.NewsItem> news =
        role == Enums.Role.TEACHER ? List.of() : recentNews();

    return new Dto.HomeResponse(
        view(user),
        date.toString(),
        focus,
        teacherCards,
        todayWork,
        classPapers,
        noticeViews,
        gradeBlocks,
        schoolCards,
        news,
        sectionLabel);
  }

  @Transactional
  public Dto.DailyFile uploadDaily(
      UserAccount current, Long sectionId, String subject, String note, LocalDate workDate, MultipartFile file) {
    UserAccount user = fresh(current);
    if (user.getRole() != Enums.Role.TEACHER) {
      throw new ForbiddenException("Only the subject teacher uploads the day's work.");
    }
    String cleanSubject = required(subject, "Subject");
    TeacherAssignment assignment =
        assignments.findByTeacherId(user.getId()).stream()
            .filter(item -> item.getSection().getId().equals(sectionId) && item.getSubject().equals(cleanSubject))
            .findFirst()
            .orElseThrow(() -> new ForbiddenException("You can upload only for a section you teach."));
    LocalDate date = workDate == null ? LocalDate.now(ZONE) : workDate;
    String stored = files.store(file);
    DailyWork work =
        dailyWork
            .findByTeacherIdAndSectionIdAndWorkDateAndSubject(
                user.getId(), assignment.getSection().getId(), date, cleanSubject)
            .orElseGet(DailyWork::new);
    work.setTeacher(user);
    work.setSection(assignment.getSection());
    work.setWorkDate(date);
    work.setSubject(cleanSubject);
    work.setNote(shortNote(note));
    work.setOriginalFileName(file.getOriginalFilename());
    work.setStoredPath(stored);
    work.setUploadedAt(Instant.now());
    return dailyView(dailyWork.save(work));
  }

  @Transactional
  public Dto.PaperFile uploadPaper(UserAccount current, Long gradeId, String title, MultipartFile file) {
    UserAccount user = fresh(current);
    Grade grade = grades.findById(gradeId).orElseThrow(() -> new IllegalArgumentException("Class not found."));
    if (user.getRole() == Enums.Role.SCHOOL_ADMIN) {
      sameSchool(user, grade.getSchool());
    } else if (user.getRole() == Enums.Role.TEACHER) {
      boolean teaches =
          assignments.findByTeacherId(user.getId()).stream()
              .anyMatch(item -> item.getSection().getGrade().getId().equals(grade.getId()));
      if (!teaches) {
        throw new ForbiddenException("Send a sample paper only to a class you teach.");
      }
    } else {
      throw new ForbiddenException("Only a teacher or the school office can send a class paper.");
    }
    ClassPaper paper = new ClassPaper();
    paper.setGrade(grade);
    paper.setUploadedBy(user);
    paper.setTitle(required(title, "Title"));
    paper.setOriginalFileName(file.getOriginalFilename());
    paper.setStoredPath(files.store(file));
    paper.setCreatedAt(Instant.now());
    return paperView(papers.save(paper));
  }

  @Transactional
  public Dto.NoticeView postNotice(UserAccount current, Dto.NoticeRequest request) {
    UserAccount user = fresh(current);
    Enums.Scope scope = parseScope(request.scope());
    Notice notice = new Notice();
    notice.setAuthor(user);
    notice.setScope(scope);
    notice.setTitle(limit(required(request.title(), "Title"), 140, "Title"));
    notice.setBody(limit(required(request.body(), "Message"), 2000, "Message"));
    notice.setCreatedAt(Instant.now());

    if (scope == Enums.Scope.CITY) {
      if (user.getRole() != Enums.Role.PLATFORM_ADMIN) {
        throw new ForbiddenException("City events are posted from the platform desk.");
      }
    } else if (user.getRole() == Enums.Role.PLATFORM_ADMIN) {
      throw new ForbiddenException("School notices stay inside that school.");
    } else if (scope == Enums.Scope.SCHOOL) {
      if (user.getRole() != Enums.Role.SCHOOL_ADMIN) {
        throw new ForbiddenException("Whole-school notices are sent by the school office.");
      }
      notice.setSchool(user.getSchool());
    } else if (scope == Enums.Scope.GRADE) {
      Grade grade = grades.findById(request.gradeId()).orElseThrow(() -> new IllegalArgumentException("Class not found."));
      assertCanAddressGrade(user, grade);
      notice.setSchool(grade.getSchool());
      notice.setGrade(grade);
    } else {
      Section section =
          sections.findById(request.sectionId()).orElseThrow(() -> new IllegalArgumentException("Section not found."));
      assertCanAddressSection(user, section);
      notice.setSchool(section.getGrade().getSchool());
      notice.setSection(section);
      notice.setGrade(section.getGrade());
    }
    return noticeView(notices.save(notice), user);
  }

  @Transactional
  public void deleteNotice(UserAccount current, Long id) {
    UserAccount user = fresh(current);
    Notice notice = notices.findById(id).orElseThrow(() -> new IllegalArgumentException("Notice not found."));
    if (user.getRole() == Enums.Role.PLATFORM_ADMIN) {
      if (notice.getScope() != Enums.Scope.CITY) {
        throw new ForbiddenException("The city desk can delete a city notice only.");
      }
    } else if (user.getRole() == Enums.Role.SCHOOL_ADMIN) {
      if (notice.getScope() == Enums.Scope.CITY
          || notice.getSchool() == null
          || user.getSchool() == null
          || !user.getSchool().getId().equals(notice.getSchool().getId())) {
        throw new ForbiddenException("The school office can delete a notice from this school only.");
      }
    } else {
      throw new ForbiddenException("Only the school office or the city desk can delete a notice.");
    }
    receipts.deleteByNoticeId(notice.getId());
    notices.delete(notice);
  }

  @Transactional
  public Dto.NewsItem ingest(String key, Dto.CityNewsRequest request) {
    if (key == null || !key.equals(properties.getIngestKey())) {
      throw new ForbiddenException("The news ingest key does not match.");
    }
    String city = request.city() == null || request.city().isBlank() ? CITY : request.city().trim();
    String title = required(request.title(), "Title");
    if (cityNews.existsByCityAndTitle(city, title)) {
      CityNews existing =
          cityNews.findByCityAndPublishedAtAfterOrderByPublishedAtDesc(city, Instant.EPOCH).stream()
              .filter(item -> item.getTitle().equals(title))
              .findFirst()
              .orElseThrow();
      return newsView(existing);
    }
    CityNews item = new CityNews();
    item.setCity(city);
    item.setTitle(title);
    item.setSourceName(required(request.sourceName(), "Source"));
    item.setSourceUrl(request.sourceUrl() == null ? "" : request.sourceUrl().trim());
    item.setSummary(limit(request.summary() == null ? "" : request.summary().trim(), 500, "Summary"));
    item.setPublishedAt(request.publishedAt() == null ? Instant.now() : request.publishedAt());
    return newsView(cityNews.save(item));
  }

  @Transactional(readOnly = true)
  public Resource openDaily(UserAccount current, Long id) {
    UserAccount user = fresh(current);
    DailyWork work = dailyWork.findById(id).orElseThrow(() -> new IllegalArgumentException("File not found."));
    if (!canReadDaily(user, work)) {
      throw new ForbiddenException("This class file is private to that section.");
    }
    return files.load(work.getStoredPath());
  }

  @Transactional(readOnly = true)
  public String dailyName(Long id) {
    return dailyWork.findById(id).map(DailyWork::getOriginalFileName).orElse("class-work.pdf");
  }

  @Transactional(readOnly = true)
  public Resource openPaper(UserAccount current, Long id) {
    UserAccount user = fresh(current);
    ClassPaper paper = papers.findById(id).orElseThrow(() -> new IllegalArgumentException("File not found."));
    if (!canReadPaper(user, paper)) {
      throw new ForbiddenException("This paper is private to that class.");
    }
    return files.load(paper.getStoredPath());
  }

  @Transactional(readOnly = true)
  public String paperName(Long id) {
    return papers.findById(id).map(ClassPaper::getOriginalFileName).orElse("sample-paper.pdf");
  }

  private List<Dto.TeacherCard> teacherCards(UserAccount user, LocalDate date) {
    return assignments.findByTeacherId(user.getId()).stream()
        .sorted(Comparator.comparing((TeacherAssignment item) -> item.getSection().getGrade().getLevel()).thenComparing(item -> item.getSection().getName()))
        .map(
            item -> {
              DailyWork existing =
                  dailyWork
                      .findByTeacherIdAndSectionIdAndWorkDateAndSubject(
                          user.getId(), item.getSection().getId(), date, item.getSubject())
                      .orElse(null);
              return new Dto.TeacherCard(
                  item.getSection().getId(),
                  item.getSection().getGrade().getId(),
                  item.getSection().getGrade().getLevel(),
                  item.getSection().getName(),
                  classLabel(item.getSection()),
                  item.getSubject(),
                  existing == null ? null : dailyView(existing));
            })
        .toList();
  }

  private List<Long> taughtGradeIds(UserAccount user) {
    return assignments.findByTeacherId(user.getId()).stream()
        .map(item -> item.getSection().getGrade().getId())
        .distinct()
        .toList();
  }

  private List<Dto.DailyFile> workForProfiles(List<StudentProfile> kids, LocalDate date) {
    List<Dto.DailyFile> files = new ArrayList<>();
    for (StudentProfile profile : kids) {
      dailyWork.findBySectionIdAndWorkDateOrderBySubjectAsc(profile.getSection().getId(), date).stream()
          .map(this::dailyView)
          .forEach(files::add);
    }
    return files;
  }

  private List<Dto.PaperFile> papersForGrades(List<Long> gradeIds) {
    List<Dto.PaperFile> result = new ArrayList<>();
    for (Long gradeId : gradeIds) {
      papers.findByGradeIdOrderByCreatedAtDesc(gradeId).stream().map(this::paperView).forEach(result::add);
    }
    return result;
  }

  private List<Dto.GradeBlock> structure(Long schoolId, LocalDate date) {
    List<Dto.GradeBlock> blocks = new ArrayList<>();
    for (Grade grade : grades.findBySchoolIdOrderByLevelAsc(schoolId)) {
      List<Dto.SectionLine> lines = new ArrayList<>();
      for (Section section : sections.findByGradeIdOrderByNameAsc(grade.getId())) {
        List<String> teacherNames = new ArrayList<>();
        if (section.getClassTeacher() != null) {
          teacherNames.add(section.getClassTeacher().getFullName() + " · Class teacher");
        }
        assignments.findBySectionId(section.getId()).stream()
            .map(item -> item.getTeacher().getFullName() + " · " + item.getSubject())
            .sorted()
            .forEach(teacherNames::add);
        int filesToday = dailyWork.findBySectionIdAndWorkDateOrderBySubjectAsc(section.getId(), date).size();
        int studentCount = students.findBySectionId(section.getId()).size();
        lines.add(new Dto.SectionLine(section.getId(), section.getName(), teacherNames, filesToday, studentCount));
      }
      blocks.add(new Dto.GradeBlock(grade.getId(), grade.getLevel(), lines));
    }
    return blocks;
  }

  private Dto.SchoolCard schoolCard(School school) {
    int classCount = grades.findBySchoolIdOrderByLevelAsc(school.getId()).size();
    int sectionCount = sections.findByGrade_School_Id(school.getId()).size();
    return new Dto.SchoolCard(school.getId(), school.getName(), school.getPlan().name(), classCount, sectionCount);
  }

  private List<Dto.NoticeView> visibleNotices(UserAccount user) {
    List<Notice> source =
        user.getRole() == Enums.Role.PLATFORM_ADMIN
            ? notices.findByScopeOrderByCreatedAtDesc(Enums.Scope.CITY)
            : notices.findForSchool(user.getSchool().getId(), Enums.Scope.CITY);
    List<Dto.NoticeView> views = new ArrayList<>();
    for (Notice notice : source) {
      if (!canSee(user, notice)) {
        continue;
      }
      if (user.getRole() == Enums.Role.PARENT || user.getRole() == Enums.Role.STUDENT) {
        markSeen(notice, user);
      }
      views.add(noticeView(notice, user));
      if (views.size() == 30) {
        break;
      }
    }
    return views;
  }

  private void markSeen(Notice notice, UserAccount user) {
    if (receipts.findByNoticeIdAndUserId(notice.getId(), user.getId()).isPresent()) {
      return;
    }
    NoticeReceipt receipt = new NoticeReceipt();
    receipt.setNotice(notice);
    receipt.setUser(user);
    receipt.setSeenAt(Instant.now());
    receipts.save(receipt);
  }

  private boolean canSee(UserAccount user, Notice notice) {
    if (notice.getScope() == Enums.Scope.CITY) {
      return true;
    }
    if (user.getRole() == Enums.Role.PLATFORM_ADMIN || user.getSchool() == null || notice.getSchool() == null) {
      return false;
    }
    if (!user.getSchool().getId().equals(notice.getSchool().getId())) {
      return false;
    }
    if (user.getRole() == Enums.Role.SCHOOL_ADMIN || notice.getScope() == Enums.Scope.SCHOOL) {
      return true;
    }
    if (user.getRole() == Enums.Role.TEACHER) {
      List<TeacherAssignment> mine = assignments.findByTeacherId(user.getId());
      if (notice.getScope() == Enums.Scope.SECTION) {
        return mine.stream().anyMatch(item -> item.getSection().getId().equals(notice.getSection().getId()));
      }
      return mine.stream().anyMatch(item -> item.getSection().getGrade().getId().equals(notice.getGrade().getId()));
    }
    for (StudentProfile profile : childrenOf(user)) {
      if (notice.getScope() == Enums.Scope.SECTION
          && profile.getSection().getId().equals(notice.getSection().getId())) {
        return true;
      }
      if (notice.getScope() == Enums.Scope.GRADE
          && profile.getSection().getGrade().getId().equals(notice.getGrade().getId())) {
        return true;
      }
    }
    return false;
  }

  private List<StudentProfile> childrenOf(UserAccount user) {
    if (user.getRole() == Enums.Role.STUDENT) {
      return students.findByUserId(user.getId()).stream().toList();
    }
    if (user.getRole() == Enums.Role.PARENT) {
      List<Long> ids = guardians.findByParentId(user.getId()).stream().map(link -> link.getStudent().getId()).toList();
      if (ids.isEmpty()) {
        return List.of();
      }
      return students.findByUserIdIn(ids);
    }
    return List.of();
  }

  private boolean canReadDaily(UserAccount user, DailyWork work) {
    if (user.getRole() == Enums.Role.PLATFORM_ADMIN) {
      return true;
    }
    School school = work.getSection().getGrade().getSchool();
    if (user.getRole() == Enums.Role.SCHOOL_ADMIN) {
      return user.getSchool() != null && user.getSchool().getId().equals(school.getId());
    }
    if (user.getRole() == Enums.Role.TEACHER) {
      return assignments.findByTeacherId(user.getId()).stream()
          .anyMatch(item -> item.getSection().getId().equals(work.getSection().getId()));
    }
    return childrenOf(user).stream().anyMatch(profile -> profile.getSection().getId().equals(work.getSection().getId()));
  }

  private boolean canReadPaper(UserAccount user, ClassPaper paper) {
    if (user.getRole() == Enums.Role.PLATFORM_ADMIN) {
      return true;
    }
    School school = paper.getGrade().getSchool();
    if (user.getRole() == Enums.Role.SCHOOL_ADMIN) {
      return user.getSchool() != null && user.getSchool().getId().equals(school.getId());
    }
    if (user.getRole() == Enums.Role.TEACHER) {
      return assignments.findByTeacherId(user.getId()).stream()
          .anyMatch(item -> item.getSection().getGrade().getId().equals(paper.getGrade().getId()));
    }
    return childrenOf(user).stream()
        .anyMatch(profile -> profile.getSection().getGrade().getId().equals(paper.getGrade().getId()));
  }

  private void assertCanAddressSection(UserAccount user, Section section) {
    sameSchool(user, section.getGrade().getSchool());
    if (user.getRole() == Enums.Role.SCHOOL_ADMIN) {
      return;
    }
    if (user.getRole() == Enums.Role.TEACHER
        && assignments.findByTeacherId(user.getId()).stream()
            .anyMatch(item -> item.getSection().getId().equals(section.getId()))) {
      return;
    }
    throw new ForbiddenException("You can write only to a section you teach.");
  }

  private void assertCanAddressGrade(UserAccount user, Grade grade) {
    sameSchool(user, grade.getSchool());
    if (user.getRole() == Enums.Role.SCHOOL_ADMIN) {
      return;
    }
    if (user.getRole() == Enums.Role.TEACHER
        && assignments.findByTeacherId(user.getId()).stream()
            .anyMatch(item -> item.getSection().getGrade().getId().equals(grade.getId()))) {
      return;
    }
    throw new ForbiddenException("You can write only to a class you teach.");
  }

  private void sameSchool(UserAccount user, School school) {
    if (user.getSchool() == null || !user.getSchool().getId().equals(school.getId())) {
      throw new ForbiddenException("That class belongs to another school.");
    }
  }

  private List<Dto.NewsItem> recentNews() {
    Instant after = Instant.now().minus(24, ChronoUnit.HOURS);
    return cityNews.findByCityAndPublishedAtAfterOrderByPublishedAtDesc(CITY, after).stream()
        .limit(20)
        .map(this::newsView)
        .toList();
  }

  private int audienceCount(Notice notice) {
    if (notice.getScope() == Enums.Scope.CITY) {
      return users.findByRoleIn(List.of(Enums.Role.PARENT, Enums.Role.STUDENT)).size();
    }
    if (notice.getScope() == Enums.Scope.SCHOOL) {
      return users.findBySchoolIdAndRoleIn(notice.getSchool().getId(), List.of(Enums.Role.PARENT, Enums.Role.STUDENT)).size();
    }
    List<StudentProfile> profiles =
        notice.getScope() == Enums.Scope.SECTION
            ? students.findBySectionId(notice.getSection().getId())
            : students.findBySection_Grade_Id(notice.getGrade().getId());
    if (profiles.isEmpty()) {
      return 0;
    }
    List<Long> ids = profiles.stream().map(profile -> profile.getUser().getId()).toList();
    return profiles.size() + guardians.findByStudentIdIn(ids).size();
  }

  private Dto.NoticeView noticeView(Notice notice, UserAccount viewer) {
    Integer seen = null;
    Integer audience = null;
    if (viewer.getRole() == Enums.Role.SCHOOL_ADMIN || viewer.getRole() == Enums.Role.PLATFORM_ADMIN) {
      seen = (int) receipts.countByNoticeId(notice.getId());
      audience = audienceCount(notice);
    }
    return new Dto.NoticeView(
        notice.getId(),
        notice.getTitle(),
        notice.getBody(),
        notice.getScope().name(),
        targetLabel(notice),
        notice.getCreatedAt().toString(),
        seen,
        audience);
  }

  private String targetLabel(Notice notice) {
    if (notice.getScope() == Enums.Scope.CITY) {
      return "All schools in " + CITY;
    }
    if (notice.getScope() == Enums.Scope.SCHOOL) {
      return notice.getSchool().getName();
    }
    if (notice.getScope() == Enums.Scope.GRADE) {
      return "Class " + notice.getGrade().getLevel() + ", every section";
    }
    return classLabel(notice.getSection());
  }

  private Dto.DailyFile dailyView(DailyWork work) {
    return new Dto.DailyFile(
        work.getId(),
        work.getSubject(),
        work.getTeacher().getFullName(),
        work.getOriginalFileName(),
        work.getNote() == null ? "" : work.getNote(),
        work.getUploadedAt().toString());
  }

  private Dto.PaperFile paperView(ClassPaper paper) {
    return new Dto.PaperFile(
        paper.getId(),
        paper.getTitle(),
        paper.getOriginalFileName(),
        "Class " + paper.getGrade().getLevel(),
        paper.getUploadedBy().getFullName(),
        paper.getCreatedAt().toString());
  }

  private Dto.NewsItem newsView(CityNews item) {
    return new Dto.NewsItem(
        item.getId(),
        item.getTitle(),
        item.getSourceName(),
        item.getSourceUrl(),
        item.getSummary(),
        item.getPublishedAt().toString());
  }

  private Dto.UserView view(UserAccount user) {
    return new Dto.UserView(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getRole().name(),
        user.getSchool() == null ? CITY : user.getSchool().getName(),
        user.getSchool() == null ? null : user.getSchool().getId());
  }

  private UserAccount fresh(UserAccount current) {
    return users.findById(current.getId()).orElseThrow(() -> new ForbiddenException("Sign in required."));
  }

  private static String classLabel(Section section) {
    return "Class " + section.getGrade().getLevel() + "-" + section.getName();
  }

  private static Enums.Scope parseScope(String raw) {
    try {
      return Enums.Scope.valueOf(raw == null ? "" : raw.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw new IllegalArgumentException("Choose a section, class, school, or the city.");
    }
  }

  private static String required(String value, String label) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(label + " is required.");
    }
    return value.trim();
  }

  private static String limit(String value, int max, String label) {
    if (value.length() > max) {
      throw new IllegalArgumentException(label + " must be " + max + " characters or fewer.");
    }
    return value;
  }

  private static String shortNote(String note) {
    if (note == null || note.isBlank()) {
      return "";
    }
    String trimmed = note.trim();
    if (trimmed.length() > 280) {
      throw new IllegalArgumentException("The note must be 280 characters or fewer.");
    }
    return trimmed;
  }
}
