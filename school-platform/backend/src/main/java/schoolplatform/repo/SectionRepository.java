package schoolplatform.repo;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import schoolplatform.domain.Section;

public interface SectionRepository extends JpaRepository<Section, Long> {
  List<Section> findByGradeIdOrderByNameAsc(Long gradeId);

  List<Section> findByGrade_School_Id(Long schoolId);
}
