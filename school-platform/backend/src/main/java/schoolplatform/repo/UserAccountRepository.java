package schoolplatform.repo;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import schoolplatform.domain.Enums;
import schoolplatform.domain.UserAccount;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByEmail(String email);

  List<UserAccount> findBySchoolIdAndRoleIn(Long schoolId, List<Enums.Role> roles);

  List<UserAccount> findByRoleIn(List<Enums.Role> roles);

  long countByEmail(String email);
}
