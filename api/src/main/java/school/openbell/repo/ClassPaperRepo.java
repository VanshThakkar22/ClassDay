package school.openbell.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import school.openbell.domain.ClassPaper;

public interface ClassPaperRepo extends JpaRepository<ClassPaper, Long> {
  List<ClassPaper> findByGradeIdOrderByCreatedAtDesc(Long gradeId);

  List<ClassPaper> findByTeacherIdOrderByCreatedAtDesc(Long teacherId);
}
