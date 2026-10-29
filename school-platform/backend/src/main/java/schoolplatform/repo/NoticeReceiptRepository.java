package schoolplatform.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import schoolplatform.domain.NoticeReceipt;

public interface NoticeReceiptRepository extends JpaRepository<NoticeReceipt, Long> {
  Optional<NoticeReceipt> findByNoticeIdAndUserId(Long noticeId, Long userId);

  long countByNoticeId(Long noticeId);
}
