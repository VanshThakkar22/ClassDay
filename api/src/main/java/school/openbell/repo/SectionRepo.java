package school.openbell.repo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import school.openbell.domain.Section;

public interface SectionRepo extends JpaRepository<Section, Long> {
  List<Section> findByGradeIdOrderByNameAsc(Long gradeId);

  Optional<Section> findByGradeIdAndName(Long gradeId, String name);
}
