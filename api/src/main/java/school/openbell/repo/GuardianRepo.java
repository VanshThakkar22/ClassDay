package school.openbell.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import school.openbell.domain.Guardian;

public interface GuardianRepo extends JpaRepository<Guardian, Long> {
  List<Guardian> findByParentId(Long parentId);

  List<Guardian> findByStudentId(Long studentId);
}
