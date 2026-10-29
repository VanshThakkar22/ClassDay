package school.openbell.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;

@Entity
@Table(name = "notice_receipts", uniqueConstraints = @UniqueConstraint(columnNames = {"notice_id", "user_id"}))
public class NoticeReceipt {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "notice_id")
  private Notice notice;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id")
  private UserAccount user;

  @Column(nullable = false)
  private Instant seenAt;

  public Long getId() { return id; }
  public Notice getNotice() { return notice; }
  public UserAccount getUser() { return user; }
  public Instant getSeenAt() { return seenAt; }

  public void setNotice(Notice notice) { this.notice = notice; }
  public void setUser(UserAccount user) { this.user = user; }
  public void setSeenAt(Instant seenAt) { this.seenAt = seenAt; }
}
