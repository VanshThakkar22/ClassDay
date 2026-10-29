package school.openbell.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import school.openbell.domain.Enums;
import school.openbell.domain.Notice;

public interface NoticeRepo extends JpaRepository<Notice, Long> {
  List<Notice> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

  List<Notice> findBySchoolIdOrderByCreatedAtDesc(Long schoolId);

  List<Notice> findByScopeOrderByCreatedAtDesc(Enums.Scope scope);

  @Query("""
      select n from Notice n
      where n.scope = :city
         or (n.scope = :schoolScope and n.school.id = :schoolId)
         or (n.scope = :gradeScope and n.grade.id = :gradeId)
         or (n.scope = :sectionScope and n.section.id = :sectionId)
      order by n.createdAt desc
      """)
  List<Notice> forDesk(
      Long schoolId,
      Long gradeId,
      Long sectionId,
      Enums.Scope city,
      Enums.Scope schoolScope,
      Enums.Scope gradeScope,
      Enums.Scope sectionScope);
}
