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
import java.time.LocalDate;

@Entity
@Table(
    name = "daily_work",
    uniqueConstraints = @UniqueConstraint(columnNames = {"section_id", "teacher_id", "work_date"}))
public class DailyWork {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "section_id")
  private Section section;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "teacher_id")
  private UserAccount teacher;

  @Column(nullable = false)
  private LocalDate workDate;

  @Column(nullable = false)
  private String subject;

  @Column(length = 2000)
  private String note;

  @Column(nullable = false)
  private String storedName;

  @Column(nullable = false)
  private String originalName;

  @Column(nullable = false)
  private Instant createdAt;

  public Long getId() { return id; }
  public Section getSection() { return section; }
  public UserAccount getTeacher() { return teacher; }
  public LocalDate getWorkDate() { return workDate; }
  public String getSubject() { return subject; }
  public String getNote() { return note; }
  public String getStoredName() { return storedName; }
  public String getOriginalName() { return originalName; }
  public Instant getCreatedAt() { return createdAt; }

  public void setSection(Section section) { this.section = section; }
  public void setTeacher(UserAccount teacher) { this.teacher = teacher; }
  public void setWorkDate(LocalDate workDate) { this.workDate = workDate; }
  public void setSubject(String subject) { this.subject = subject; }
  public void setNote(String note) { this.note = note; }
  public void setStoredName(String storedName) { this.storedName = storedName; }
  public void setOriginalName(String originalName) { this.originalName = originalName; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
