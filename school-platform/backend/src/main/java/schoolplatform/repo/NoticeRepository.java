package schoolplatform.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import schoolplatform.domain.Enums;
import schoolplatform.domain.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
  @Query(
      """
      select n from Notice n
      where n.scope = :city or n.school.id = :schoolId
      order by n.createdAt desc
      """)
  List<Notice> findForSchool(@Param("schoolId") Long schoolId, @Param("city") Enums.Scope city);

  List<Notice> findByScopeOrderByCreatedAtDesc(schoolplatform.domain.Enums.Scope scope);
}
