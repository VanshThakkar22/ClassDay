package schoolplatform.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import schoolplatform.domain.School;

public interface SchoolRepository extends JpaRepository<School, Long> {}
