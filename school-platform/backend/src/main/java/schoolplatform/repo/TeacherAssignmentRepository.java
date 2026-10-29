package schoolplatform.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import schoolplatform.domain.TeacherAssignment;

public interface TeacherAssignmentRepository extends JpaRepository<TeacherAssignment, Long> {
  List<TeacherAssignment> findByTeacherId(Long teacherId);

  List<TeacherAssignment> findBySectionId(Long sectionId);

  List<TeacherAssignment> findBySection_Grade_Id(Long gradeId);
}
