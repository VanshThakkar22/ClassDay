package schoolplatform.repo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import schoolplatform.domain.DailyWork;

public interface DailyWorkRepository extends JpaRepository<DailyWork, Long> {
  List<DailyWork> findBySectionIdAndWorkDateOrderBySubjectAsc(Long sectionId, LocalDate workDate);

  Optional<DailyWork> findByTeacherIdAndSectionIdAndWorkDateAndSubject(
      Long teacherId, Long sectionId, LocalDate workDate, String subject);
}
