package schoolplatform.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import schoolplatform.domain.ClassPaper;

public interface ClassPaperRepository extends JpaRepository<ClassPaper, Long> {
  List<ClassPaper> findByGradeIdOrderByCreatedAtDesc(Long gradeId);

  List<ClassPaper> findByGrade_School_IdOrderByCreatedAtDesc(Long schoolId);
}
