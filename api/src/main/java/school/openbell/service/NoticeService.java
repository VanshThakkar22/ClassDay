package school.openbell.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.openbell.domain.Enums;
import school.openbell.domain.Grade;
import school.openbell.domain.Guardian;
import school.openbell.domain.Notice;
import school.openbell.domain.NoticeReceipt;
import school.openbell.domain.School;
import school.openbell.domain.Section;
import school.openbell.domain.Student;
import school.openbell.domain.UserAccount;
import school.openbell.repo.GradeRepo;
import school.openbell.repo.GuardianRepo;
import school.openbell.repo.NoticeReceiptRepo;
import school.openbell.repo.NoticeRepo;
import school.openbell.repo.SchoolRepo;
import school.openbell.repo.SectionRepo;
import school.openbell.repo.StudentRepo;
import school.openbell.repo.TeacherSectionRepo;
import school.openbell.repo.UserRepo;
import school.openbell.security.UserPrincipal;
import school.openbell.support.AppException;
import school.openbell.web.ApiModels.EventRequest;
import school.openbell.web.ApiModels.NoticeRequest;
import school.openbell.web.ApiModels.NoticeView;

@Service
public class NoticeService {
  private final NoticeRepo notices;
  private final NoticeReceiptRepo receipts;
  private final SectionRepo sections;
  private final GradeRepo grades;
  private final SchoolRepo schools;
  private final StudentRepo students;
  private final GuardianRepo guardians;
  private final TeacherSectionRepo teaching;
  private final UserRepo users;

  public NoticeService(
      NoticeRepo notices,
      NoticeReceiptRepo receipts,
      SectionRepo sections,
      GradeRepo grades,
      SchoolRepo schools,
      StudentRepo students,
      GuardianRepo guardians,
      TeacherSectionRepo teaching,
      UserRepo users) {
    this.notices = notices;
    this.receipts = receipts;
    this.sections = sections;
    this.grades = grades;
    this.schools = schools;
    this.students = students;
    this.guardians = guardians;
    this.teaching = teaching;
    this.users = users;
  }

  @Transactional
  public NoticeView create(UserPrincipal principal, NoticeRequest request) {
    Enums.Scope scope = parse(request.scope());
    String title = required(request.title(), 160, "The title");
    String body = required(request.body(), 4000, "The note");
    Notice notice = new Notice();
    notice.setAuthor(users.getReferenceById(principal.id()));
    notice.setTitle(title);
    notice.setBody(body);
    notice.setCreatedAt(Instant.now());
    notice.setScope(scope);

    if (principal.role() == Enums.Role.TEACHER) {
      if (scope != Enums.Scope.SECTION) {
        throw AppException.forbidden("A teacher sends a note to their own section. A paper for the whole class has its own page.");
      }
      Section section = section(request.sectionId());
      if (teaching.assignmentIds(principal.id(), section.getId()).isEmpty()) {
        throw AppException.forbidden("You do not teach that section.");
      }
      notice.setSection(section);
      notice.setSchool(section.getGrade().getSchool());
    } else if (principal.role() == Enums.Role.SCHOOL_ADMIN) {
      School school = schools.getReferenceById(principal.schoolId());
      notice.setSchool(school);
      if (scope == Enums.Scope.SECTION) {
        Section section = section(request.sectionId());
        sameSchool(section.getGrade().getSchool().getId(), principal.schoolId());
        notice.setSection(section);
      } else if (scope == Enums.Scope.GRADE) {
        Grade grade = grade(request.gradeId());
        sameSchool(grade.getSchool().getId(), principal.schoolId());
        notice.setGrade(grade);
      } else if (scope == Enums.Scope.SCHOOL) {
        // The school link above is the whole audience.
      } else {
        throw AppException.forbidden("A note to every school waits for the City plan.");
      }
    } else {
      throw AppException.forbidden("That desk belongs to someone else.");
    }
    return present(notices.save(notice), principal.id(), false);
  }

  @Transactional
  public NoticeView cityEvent(UserPrincipal principal, EventRequest request) {
    if (principal.role() != Enums.Role.PLATFORM_ADMIN) {
      throw AppException.forbidden("Only the city desk can speak to every school.");
    }
    Notice notice = new Notice();
    notice.setAuthor(users.getReferenceById(principal.id()));
    notice.setScope(Enums.Scope.CITY);
    notice.setTitle(required(request.title(), 160, "The title"));
    notice.setBody(required(request.body(), 4000, "The note"));
    notice.setCreatedAt(Instant.now());
    return present(notices.save(notice), principal.id(), false);
  }

  @Transactional(readOnly = true)
  public List<NoticeView> sent(UserPrincipal principal) {
    List<Notice> rows = switch (principal.role()) {
      case TEACHER -> notices.findByAuthorIdOrderByCreatedAtDesc(principal.id());
      case SCHOOL_ADMIN -> notices.findBySchoolIdOrderByCreatedAtDesc(principal.schoolId());
      case PLATFORM_ADMIN -> notices.findByScopeOrderByCreatedAtDesc(Enums.Scope.CITY);
      default -> List.of();
    };
    return rows.stream().map(row -> present(row, principal.id(), false)).toList();
  }

  @Transactional
  public List<NoticeView> forStudent(Long schoolId, Long gradeId, Long sectionId, Long readerId) {
    List<Notice> rows = notices.forDesk(
        schoolId, gradeId, sectionId, Enums.Scope.CITY, Enums.Scope.SCHOOL, Enums.Scope.GRADE, Enums.Scope.SECTION);
    List<NoticeView> views = new ArrayList<>();
    UserAccount reader = users.getReferenceById(readerId);
    for (Notice notice : rows) {
      if (!receipts.existsByNoticeIdAndUserId(notice.getId(), readerId)) {
        NoticeReceipt receipt = new NoticeReceipt();
        receipt.setNotice(notice);
        receipt.setUser(reader);
        receipt.setSeenAt(Instant.now());
        receipts.save(receipt);
      }
      views.add(present(notice, readerId, true));
    }
    return views;
  }

  private NoticeView present(Notice notice, Long readerId, boolean readerSaw) {
    List<Long> recipients = recipients(notice);
    int seen = recipients.isEmpty() ? 0 : (int) receipts.countByNoticeIdAndUserIdIn(notice.getId(), recipients);
    boolean saw = readerSaw || receipts.existsByNoticeIdAndUserId(notice.getId(), readerId);
    return new NoticeView(
        notice.getId(),
        notice.getTitle(),
        notice.getBody(),
        notice.getScope().name(),
        target(notice),
        notice.getAuthor().getName(),
        notice.getCreatedAt(),
        seen,
        recipients.size(),
        saw);
  }

  private List<Long> recipients(Notice notice) {
    List<Student> audience = switch (notice.getScope()) {
      case SECTION -> students.findBySectionId(notice.getSection().getId());
      case GRADE -> studentsInGrade(notice.getGrade().getId());
      case SCHOOL -> studentsInSchool(notice.getSchool().getId());
      case CITY -> students.findAll();
    };
    Set<Long> ids = new LinkedHashSet<>();
    for (Student student : audience) {
      if (student.getUser() != null) {
        ids.add(student.getUser().getId());
      }
      for (Guardian guardian : guardians.findByStudentId(student.getId())) {
        ids.add(guardian.getParent().getId());
      }
    }
    return new ArrayList<>(ids);
  }

  private List<Student> studentsInGrade(Long gradeId) {
    List<Student> rows = new ArrayList<>();
    grades.findById(gradeId).ifPresent(grade -> {
      for (Section section : sections.findByGradeIdOrderByNameAsc(grade.getId())) {
        rows.addAll(students.findBySectionId(section.getId()));
      }
    });
    return rows;
  }

  private List<Student> studentsInSchool(Long schoolId) {
    List<Student> rows = new ArrayList<>();
    for (Grade grade : grades.findBySchoolIdOrderByLevelAsc(schoolId)) {
      rows.addAll(studentsInGrade(grade.getId()));
    }
    return rows;
  }

  private String target(Notice notice) {
    return switch (notice.getScope()) {
      case SECTION -> notice.getSection().getGrade().getLabel() + " · " + notice.getSection().getName();
      case GRADE -> notice.getGrade().getLabel() + ", every section";
      case SCHOOL -> "Whole school";
      case CITY -> "Saharanpur";
    };
  }

  private Section section(Long id) {
    if (id == null) {
      throw AppException.bad("Choose a section.");
    }
    return sections.findById(id).orElseThrow(() -> AppException.missing("That section is not here."));
  }

  private Grade grade(Long id) {
    if (id == null) {
      throw AppException.bad("Choose a class.");
    }
    return grades.findById(id).orElseThrow(() -> AppException.missing("That class is not here."));
  }

  private static void sameSchool(Long actual, Long expected) {
    if (expected == null || !expected.equals(actual)) {
      throw AppException.forbidden("That class belongs to another school.");
    }
  }

  private static Enums.Scope parse(String raw) {
    try {
      return Enums.Scope.valueOf(raw == null ? "" : raw.trim().toUpperCase());
    } catch (IllegalArgumentException ex) {
      throw AppException.bad("Choose where this note should go.");
    }
  }

  private static String required(String value, int max, String label) {
    String clean = value == null ? "" : value.trim();
    if (clean.isEmpty()) {
      throw AppException.bad(label + " needs a few words.");
    }
    if (clean.length() > max) {
      throw AppException.bad(label + " is too long.");
    }
    return clean;
  }
}
