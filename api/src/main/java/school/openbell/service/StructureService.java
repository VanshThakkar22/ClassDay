package school.openbell.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.openbell.domain.Grade;
import school.openbell.domain.School;
import school.openbell.domain.Section;
import school.openbell.domain.TeacherSection;
import school.openbell.repo.GradeRepo;
import school.openbell.repo.SchoolRepo;
import school.openbell.repo.SectionRepo;
import school.openbell.repo.StudentRepo;
import school.openbell.repo.TeacherSectionRepo;
import school.openbell.security.UserPrincipal;
import school.openbell.support.AppException;
import school.openbell.web.ApiModels.GradeFolder;
import school.openbell.web.ApiModels.SchoolCard;
import school.openbell.web.ApiModels.SchoolTree;
import school.openbell.web.ApiModels.SectionFolder;

@Service
public class StructureService {
  private final SchoolRepo schools;
  private final GradeRepo grades;
  private final SectionRepo sections;
  private final TeacherSectionRepo teaching;
  private final StudentRepo students;

  public StructureService(
      SchoolRepo schools,
      GradeRepo grades,
      SectionRepo sections,
      TeacherSectionRepo teaching,
      StudentRepo students) {
    this.schools = schools;
    this.grades = grades;
    this.sections = sections;
    this.teaching = teaching;
    this.students = students;
  }

  @Transactional(readOnly = true)
  public List<SchoolCard> cards() {
    List<SchoolCard> cards = new ArrayList<>();
    for (School school : schools.findAll()) {
      List<Grade> gradeRows = grades.findBySchoolIdOrderByLevelAsc(school.getId());
      int sectionCount = 0;
      for (Grade grade : gradeRows) {
        sectionCount += sections.findByGradeIdOrderByNameAsc(grade.getId()).size();
      }
      cards.add(new SchoolCard(
          school.getId(), school.getName(), school.getArea(), school.getPlan().name(), gradeRows.size(), sectionCount));
    }
    return cards;
  }

  @Transactional(readOnly = true)
  public SchoolTree tree(UserPrincipal principal, Long schoolId) {
    Long id = schoolId == null ? principal.schoolId() : schoolId;
    if (id == null) {
      throw AppException.bad("Choose a school.");
    }
    if (principal.schoolId() != null && !principal.schoolId().equals(id)) {
      throw AppException.forbidden("That school keeps its own book.");
    }
    School school = schools.findById(id).orElseThrow(() -> AppException.missing("That school is not here."));
    Map<Long, List<String>> teachers = new HashMap<>();
    for (TeacherSection row : teaching.findForSchool(id)) {
      teachers.computeIfAbsent(row.getSection().getId(), key -> new ArrayList<>())
          .add(row.getTeacher().getName() + " · " + row.getSubject());
    }
    Map<Long, Long> counts = new HashMap<>();
    for (Object[] row : students.countsBySchool(id)) {
      counts.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
    }
    List<GradeFolder> folders = new ArrayList<>();
    for (Grade grade : grades.findBySchoolIdOrderByLevelAsc(id)) {
      List<SectionFolder> sectionFolders = new ArrayList<>();
      for (Section section : sections.findByGradeIdOrderByNameAsc(grade.getId())) {
        sectionFolders.add(new SectionFolder(
            section.getId(),
            section.getName(),
            teachers.getOrDefault(section.getId(), List.of()),
            counts.getOrDefault(section.getId(), 0L)));
      }
      folders.add(new GradeFolder(grade.getId(), grade.getLevel(), grade.getLabel(), sectionFolders));
    }
    return new SchoolTree(school.getId(), school.getName(), school.getArea(), school.getCity(), school.getPlan().name(), folders);
  }
}
