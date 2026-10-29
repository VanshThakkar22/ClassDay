package school.openbell.repo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import school.openbell.domain.Student;

public interface StudentRepo extends JpaRepository<Student, Long> {
  List<Student> findBySectionId(Long sectionId);

  Optional<Student> findByUserId(Long userId);

  @Query("select s.section.id, count(s) from Student s where s.school.id = :schoolId group by s.section.id")
  List<Object[]> countsBySchool(Long schoolId);
}
