package schoolplatform.repo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import schoolplatform.domain.StudentProfile;

public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
  Optional<StudentProfile> findByUserId(Long userId);

  List<StudentProfile> findBySectionId(Long sectionId);

  List<StudentProfile> findBySection_Grade_Id(Long gradeId);

  List<StudentProfile> findByUserIdIn(List<Long> userIds);
}
