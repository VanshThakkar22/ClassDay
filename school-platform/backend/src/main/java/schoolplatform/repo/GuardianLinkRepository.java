package schoolplatform.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import schoolplatform.domain.GuardianLink;

public interface GuardianLinkRepository extends JpaRepository<GuardianLink, Long> {
  List<GuardianLink> findByParentId(Long parentId);

  List<GuardianLink> findByStudentIdIn(List<Long> studentIds);
}
