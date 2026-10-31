package schoolplatform.repo;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import schoolplatform.domain.NoticeReceipt;

public interface NoticeReceiptRepository extends JpaRepository<NoticeReceipt, Long> {
  Optional<NoticeReceipt> findByNoticeIdAndUserId(Long noticeId, Long userId);

  long countByNoticeId(Long noticeId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("delete from NoticeReceipt receipt where receipt.notice.id = :noticeId")
  void deleteByNoticeId(@Param("noticeId") Long noticeId);
}
