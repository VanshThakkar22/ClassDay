package schoolplatform.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import schoolplatform.domain.Grade;

public interface GradeRepository extends JpaRepository<Grade, Long> {
  List<Grade> findBySchoolIdOrderByLevelAsc(Long schoolId);
}
