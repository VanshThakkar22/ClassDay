package school.openbell.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.openbell.domain.ClassPaper;
import school.openbell.domain.DailyWork;
import school.openbell.domain.Enums;
import school.openbell.domain.Guardian;
import school.openbell.domain.Student;
import school.openbell.repo.ClassPaperRepo;
import school.openbell.repo.DailyWorkRepo;
import school.openbell.repo.GuardianRepo;
import school.openbell.repo.StudentRepo;
import school.openbell.security.UserPrincipal;
import school.openbell.support.AppException;
import school.openbell.support.SchoolClock;
import school.openbell.web.ApiModels.DiaryView;
import school.openbell.web.ApiModels.PaperView;
import school.openbell.web.ApiModels.StudentChip;
import school.openbell.web.ApiModels.WorkFile;

@Service
public class DiaryService {
  private final StudentRepo students;
  private final GuardianRepo guardians;
  private final DailyWorkRepo work;
  private final ClassPaperRepo papers;
  private final NoticeService notices;

  public DiaryService(
      StudentRepo students,
      GuardianRepo guardians,
      DailyWorkRepo work,
      ClassPaperRepo papers,
      NoticeService notices) {
    this.students = students;
    this.guardians = guardians;
    this.work = work;
    this.papers = papers;
    this.notices = notices;
  }

  @Transactional
  public DiaryView open(UserPrincipal principal, Long studentId, String dateText) {
    if (principal.role() != Enums.Role.PARENT && principal.role() != Enums.Role.STUDENT) {
      throw AppException.forbidden("The diary is for a student and their parent.");
    }
    List<Student> family = family(principal);
    if (family.isEmpty()) {
      throw AppException.missing("No student is linked to this login yet.");
    }
    Student selected = family.stream()
        .filter(student -> student.getId().equals(studentId))
        .findFirst()
        .orElse(family.getFirst());
    if (studentId != null && family.stream().noneMatch(student -> student.getId().equals(studentId))) {
      throw AppException.forbidden("That diary belongs to another family.");
    }
    LocalDate date = parse(dateText);
    if (date.isAfter(SchoolClock.today())) {
      throw AppException.bad("That day has not happened.");
    }
    List<WorkFile> sheets = new ArrayList<>();
    for (DailyWork row : work.findBySectionIdAndWorkDateOrderBySubjectAsc(selected.getSection().getId(), date)) {
      sheets.add(new WorkFile(row.getId(), row.getSubject(), row.getTeacher().getName(), row.getNote(), row.getOriginalName()));
    }
    List<PaperView> shared = new ArrayList<>();
    for (ClassPaper paper : papers.findByGradeIdOrderByCreatedAtDesc(selected.getSection().getGrade().getId())) {
      shared.add(new PaperView(
          paper.getId(),
          paper.getGrade().getId(),
          paper.getGrade().getLabel(),
          paper.getTitle(),
          paper.getNote(),
          paper.getOriginalName(),
          paper.getTeacher().getName(),
          paper.getCreatedAt()));
    }
    List<StudentChip> chips = family.stream().map(this::chip).toList();
    return new DiaryView(
        date.toString(),
        SchoolClock.label(date),
        chips,
        selected.getId(),
        sheets,
        shared,
        notices.forStudent(
            selected.getSchool().getId(),
            selected.getSection().getGrade().getId(),
            selected.getSection().getId(),
            principal.id()));
  }

  private List<Student> family(UserPrincipal principal) {
    if (principal.role() == Enums.Role.STUDENT) {
      return students.findByUserId(principal.id()).map(List::of).orElse(List.of());
    }
    List<Student> rows = new ArrayList<>();
    for (Guardian guardian : guardians.findByParentId(principal.id())) {
      rows.add(guardian.getStudent());
    }
    return rows;
  }

  private LocalDate parse(String dateText) {
    if (dateText == null || dateText.isBlank()) {
      return SchoolClock.today();
    }
    try {
      return LocalDate.parse(dateText);
    } catch (DateTimeParseException ex) {
      throw AppException.bad("That date is not one I can read.");
    }
  }

  private StudentChip chip(Student student) {
    return new StudentChip(
        student.getId(),
        student.getName(),
        student.getSection().getGrade().getLabel(),
        student.getSection().getName(),
        student.getSchool().getName());
  }
}
