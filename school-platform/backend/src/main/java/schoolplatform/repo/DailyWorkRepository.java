package schoolplatform.repo;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import schoolplatform.domain.DailyWork;

public interface DailyWorkRepository extends JpaRepository<DailyWork, Long> {
  List<DailyWork> findBySectionIdAndWorkDateOrderBySubjectAsc(Long sectionId, LocalDate workDate);

  List<DailyWork> findBySectionIdAndUploadedAtGreaterThanEqualAndUploadedAtLessThanOrderBySubjectAsc(
      Long sectionId, Instant from, Instant to);

  Optional<DailyWork> findByTeacherIdAndSectionIdAndWorkDateAndSubject(
      Long teacherId, Long sectionId, LocalDate workDate, String subject);
}
