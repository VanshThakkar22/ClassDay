package school.openbell.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import school.openbell.domain.ClassPaper;
import school.openbell.domain.DailyWork;
import school.openbell.domain.Grade;
import school.openbell.domain.TeacherSection;
import school.openbell.domain.UserAccount;
import school.openbell.repo.ClassPaperRepo;
import school.openbell.repo.DailyWorkRepo;
import school.openbell.repo.GradeRepo;
import school.openbell.repo.SectionRepo;
import school.openbell.repo.TeacherSectionRepo;
import school.openbell.repo.UserRepo;
import school.openbell.security.UserPrincipal;
import school.openbell.support.AppException;
import school.openbell.support.SchoolClock;
import school.openbell.web.ApiModels.Colleague;
import school.openbell.web.ApiModels.GradeOption;
import school.openbell.web.ApiModels.PaperView;
import school.openbell.web.ApiModels.TodayDesk;
import school.openbell.web.ApiModels.TodaySection;
import school.openbell.web.ApiModels.WorkFile;

@Service
public class DeskService {
  private final UserRepo users;
  private final TeacherSectionRepo teaching;
  private final DailyWorkRepo work;
  private final ClassPaperRepo papers;
  private final SectionRepo sections;
  private final GradeRepo grades;
  private final FileStorage files;

  public DeskService(
      UserRepo users,
      TeacherSectionRepo teaching,
      DailyWorkRepo work,
      ClassPaperRepo papers,
      SectionRepo sections,
      GradeRepo grades,
      FileStorage files) {
    this.users = users;
    this.teaching = teaching;
    this.work = work;
    this.papers = papers;
    this.sections = sections;
    this.grades = grades;
    this.files = files;
  }

  @Transactional(readOnly = true)
  public TodayDesk today(UserPrincipal principal) {
    LocalDate date = SchoolClock.today();
    List<TeacherSection> mine = assignments(principal.id());
    List<TodaySection> rows = new ArrayList<>();
    for (TeacherSection assignment : mine) {
      var section = assignment.getSection();
      List<DailyWork> sent = work.findBySectionIdAndWorkDateOrderBySubjectAsc(section.getId(), date);
      WorkFile own = null;
      List<Colleague> colleagues = new ArrayList<>();
      for (TeacherSection other : teaching.findBySectionId(section.getId())) {
        DailyWork match = sent.stream().filter(item -> item.getTeacher().getId().equals(other.getTeacher().getId())).findFirst().orElse(null);
        if (other.getTeacher().getId().equals(principal.id()) && other.getSubject().equals(assignment.getSubject())) {
          if (match != null) {
            own = file(match);
          }
        } else if (!other.getTeacher().getId().equals(principal.id())) {
          colleagues.add(new Colleague(
              other.getTeacher().getName(),
              other.getSubject(),
              match == null ? null : match.getId(),
              match == null ? null : match.getOriginalName(),
              match == null ? null : match.getNote()));
        }
      }
      rows.add(new TodaySection(
          section.getId(),
          section.getGrade().getLabel(),
          section.getName(),
          assignment.getSubject(),
          own,
          colleagues));
    }
    return new TodayDesk(date.toString(), SchoolClock.label(date), rows);
  }

  @Transactional
  public WorkFile uploadToday(UserPrincipal principal, Long sectionId, String note, MultipartFile file) {
    if (teaching.assignmentIds(principal.id(), sectionId).isEmpty()) {
      throw AppException.forbidden("You do not teach that section.");
    }
    TeacherSection assignment = teaching.findByTeacherId(principal.id()).stream()
        .filter(row -> row.getSection().getId().equals(sectionId))
        .findFirst()
        .orElseThrow(() -> AppException.forbidden("You do not teach that section."));
    LocalDate date = SchoolClock.today();
    String stored = files.store(file);
    DailyWork row = work.findByTeacherIdAndSectionIdAndWorkDate(principal.id(), sectionId, date).orElseGet(DailyWork::new);
    if (row.getId() != null) {
      files.delete(row.getStoredName());
    }
    row.setSection(sections.getReferenceById(sectionId));
    row.setTeacher(users.getReferenceById(principal.id()));
    row.setWorkDate(date);
    row.setSubject(assignment.getSubject());
    row.setNote(clip(note, 2000, "The note"));
    row.setStoredName(stored);
    row.setOriginalName(safeName(file.getOriginalFilename(), assignment.getSubject()));
    row.setCreatedAt(Instant.now());
    return file(work.save(row));
  }

  @Transactional(readOnly = true)
  public List<GradeOption> grades(UserPrincipal principal) {
    Map<Long, GradeOption> options = new LinkedHashMap<>();
    for (TeacherSection row : assignments(principal.id())) {
      Grade grade = row.getSection().getGrade();
      options.putIfAbsent(grade.getId(), new GradeOption(grade.getId(), grade.getLabel()));
    }
    return new ArrayList<>(options.values());
  }

  @Transactional(readOnly = true)
  public List<PaperView> papers(UserPrincipal principal) {
    return papers.findByTeacherIdOrderByCreatedAtDesc(principal.id()).stream().map(this::paper).toList();
  }

  @Transactional
  public PaperView uploadPaper(UserPrincipal principal, Long gradeId, String title, String note, MultipartFile file) {
    if (teaching.gradeAssignmentIds(principal.id(), gradeId).isEmpty()) {
      throw AppException.forbidden("You can send a paper to a class you teach.");
    }
    Grade grade = grades.findById(gradeId).orElseThrow(() -> AppException.missing("That class is not here."));
    if (principal.schoolId() == null || !principal.schoolId().equals(grade.getSchool().getId())) {
      throw AppException.forbidden("That class belongs to another school.");
    }
    String cleanTitle = required(title, 160, "The title");
    UserAccount teacher = users.findById(principal.id()).orElseThrow();
    ClassPaper paper = new ClassPaper();
    paper.setGrade(grade);
    paper.setTeacher(teacher);
    paper.setTitle(cleanTitle);
    paper.setNote(clip(note, 2000, "The note"));
    paper.setStoredName(files.store(file));
    paper.setOriginalName(safeName(file.getOriginalFilename(), "paper"));
    paper.setCreatedAt(Instant.now());
    return paper(papers.save(paper));
  }

  private List<TeacherSection> assignments(Long teacherId) {
    List<TeacherSection> rows = new ArrayList<>(teaching.findByTeacherId(teacherId));
    rows.sort(Comparator.comparing((TeacherSection row) -> row.getSection().getGrade().getLevel())
        .thenComparing(row -> row.getSection().getName()));
    return rows;
  }

  private WorkFile file(DailyWork row) {
    return new WorkFile(row.getId(), row.getSubject(), row.getTeacher().getName(), row.getNote(), row.getOriginalName());
  }

  private PaperView paper(ClassPaper row) {
    return new PaperView(
        row.getId(),
        row.getGrade().getId(),
        row.getGrade().getLabel(),
        row.getTitle(),
        row.getNote(),
        row.getOriginalName(),
        row.getTeacher().getName(),
        row.getCreatedAt());
  }

  private static String clip(String value, int max, String label) {
    if (value == null || value.isBlank()) {
      return null;
    }
    String clean = value.trim();
    if (clean.length() > max) {
      throw AppException.bad(label + " is too long.");
    }
    return clean;
  }

  private static String required(String value, int max, String label) {
    String clean = clip(value, max, label);
    if (clean == null) {
      throw AppException.bad(label + " needs a few words.");
    }
    return clean;
  }

  private static String safeName(String original, String fallback) {
    if (original == null || original.isBlank()) {
      return fallback + ".pdf";
    }
    String name = original.replace("\\", "/");
    int slash = name.lastIndexOf('/');
    name = slash >= 0 ? name.substring(slash + 1) : name;
    name = name.replaceAll("[^A-Za-z0-9._ -]", "");
    if (name.isBlank()) {
      return fallback + ".pdf";
    }
    return name.length() > 120 ? name.substring(name.length() - 120) : name;
  }
}
