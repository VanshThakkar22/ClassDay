package school.openbell.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import school.openbell.domain.DailyWork;

public interface DailyWorkRepo extends JpaRepository<DailyWork, Long> {
  List<DailyWork> findBySectionIdAndWorkDateOrderBySubjectAsc(Long sectionId, LocalDate workDate);

  Optional<DailyWork> findByTeacherIdAndSectionIdAndWorkDate(Long teacherId, Long sectionId, LocalDate workDate);
}
