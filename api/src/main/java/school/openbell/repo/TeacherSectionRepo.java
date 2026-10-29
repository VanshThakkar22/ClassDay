package school.openbell.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import school.openbell.domain.TeacherSection;

public interface TeacherSectionRepo extends JpaRepository<TeacherSection, Long> {
  List<TeacherSection> findByTeacherId(Long teacherId);

  List<TeacherSection> findBySectionId(Long sectionId);

  @Query("select t from TeacherSection t where t.section.grade.school.id = :schoolId")
  List<TeacherSection> findForSchool(Long schoolId);

  @Query("select t.id from TeacherSection t where t.teacher.id = :teacherId and t.section.id = :sectionId")
  List<Long> assignmentIds(Long teacherId, Long sectionId);

  @Query("select t.id from TeacherSection t where t.teacher.id = :teacherId and t.section.grade.id = :gradeId")
  List<Long> gradeAssignmentIds(Long teacherId, Long gradeId);
}
