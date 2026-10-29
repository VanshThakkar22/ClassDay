package school.openbell.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import school.openbell.domain.School;

public interface SchoolRepo extends JpaRepository<School, Long> {}
