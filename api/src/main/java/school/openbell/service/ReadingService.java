package school.openbell.service;

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
import school.openbell.repo.TeacherSectionRepo;
import school.openbell.security.UserPrincipal;
import school.openbell.support.AppException;

@Service
public class ReadingService {
  private final DailyWorkRepo work;
  private final ClassPaperRepo papers;
  private final TeacherSectionRepo teaching;
  private final StudentRepo students;
  private final GuardianRepo guardians;

  public ReadingService(
      DailyWorkRepo work,
      ClassPaperRepo papers,
      TeacherSectionRepo teaching,
      StudentRepo students,
      GuardianRepo guardians) {
    this.work = work;
    this.papers = papers;
    this.teaching = teaching;
    this.students = students;
    this.guardians = guardians;
  }

  @Transactional(readOnly = true)
  public DailyWork daily(UserPrincipal principal, Long id) {
    DailyWork row = work.findById(id).orElseThrow(() -> AppException.missing("That sheet is not here."));
    Long schoolId = row.getSection().getGrade().getSchool().getId();
    Long sectionId = row.getSection().getId();
    if (canSeeSection(principal, schoolId, sectionId)) {
      return row;
    }
    throw AppException.forbidden("That sheet belongs to another class.");
  }

  @Transactional(readOnly = true)
  public ClassPaper paper(UserPrincipal principal, Long id) {
    ClassPaper row = papers.findById(id).orElseThrow(() -> AppException.missing("That paper is not here."));
    Long schoolId = row.getGrade().getSchool().getId();
    Long gradeId = row.getGrade().getId();
    if (canSeeGrade(principal, schoolId, gradeId)) {
      return row;
    }
    throw AppException.forbidden("That paper belongs to another class.");
  }

  private boolean canSeeSection(UserPrincipal principal, Long schoolId, Long sectionId) {
    if (principal.role() == Enums.Role.PLATFORM_ADMIN) {
      return true;
    }
    if (principal.role() == Enums.Role.SCHOOL_ADMIN) {
      return schoolId.equals(principal.schoolId());
    }
    if (principal.role() == Enums.Role.TEACHER) {
      return !teaching.assignmentIds(principal.id(), sectionId).isEmpty();
    }
    return familyInSection(principal, sectionId);
  }

  private boolean canSeeGrade(UserPrincipal principal, Long schoolId, Long gradeId) {
    if (principal.role() == Enums.Role.PLATFORM_ADMIN) {
      return true;
    }
    if (principal.role() == Enums.Role.SCHOOL_ADMIN) {
      return schoolId.equals(principal.schoolId());
    }
    if (principal.role() == Enums.Role.TEACHER) {
      return !teaching.gradeAssignmentIds(principal.id(), gradeId).isEmpty();
    }
    return familyInGrade(principal, gradeId);
  }

  private boolean familyInSection(UserPrincipal principal, Long sectionId) {
    for (Student student : household(principal)) {
      if (student.getSection().getId().equals(sectionId)) {
        return true;
      }
    }
    return false;
  }

  private boolean familyInGrade(UserPrincipal principal, Long gradeId) {
    for (Student student : household(principal)) {
      if (student.getSection().getGrade().getId().equals(gradeId)) {
        return true;
      }
    }
    return false;
  }

  private List<Student> household(UserPrincipal principal) {
    if (principal.role() == Enums.Role.STUDENT) {
      return students.findByUserId(principal.id()).map(List::of).orElse(List.of());
    }
    if (principal.role() == Enums.Role.PARENT) {
      List<Student> rows = new ArrayList<>();
      for (Guardian guardian : guardians.findByParentId(principal.id())) {
        rows.add(guardian.getStudent());
      }
      return rows;
    }
    return List.of();
  }
}
