package school.openbell.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import school.openbell.domain.UserAccount;

public interface UserRepo extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByEmail(String email);
}
