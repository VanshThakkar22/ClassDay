package school.openbell.repo;

import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import school.openbell.domain.NoticeReceipt;

public interface NoticeReceiptRepo extends JpaRepository<NoticeReceipt, Long> {
  boolean existsByNoticeIdAndUserId(Long noticeId, Long userId);

  long countByNoticeIdAndUserIdIn(Long noticeId, Collection<Long> userIds);
}
