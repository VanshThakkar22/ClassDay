package school.openbell.repo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import school.openbell.domain.Grade;

public interface GradeRepo extends JpaRepository<Grade, Long> {
  List<Grade> findBySchoolIdOrderByLevelAsc(Long schoolId);

  Optional<Grade> findBySchoolIdAndLevel(Long schoolId, int level);
}
