package schoolplatform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    uniqueConstraints =
        @UniqueConstraint(columnNames = {"teacher_id", "section_id", "work_date", "subject"}))
public class DailyWork {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "teacher_id")
  private UserAccount teacher;

  @ManyToOne(optional = false)
  @JoinColumn(name = "section_id")
  private Section section;

  @Column(name = "work_date")
  private LocalDate workDate;

  private String subject;

  @Column(length = 280)
  private String note;

  private String originalFileName;
  private String storedPath;
  private Instant uploadedAt;

  public Long getId() {
    return id;
  }

  public UserAccount getTeacher() {
    return teacher;
  }

  public void setTeacher(UserAccount teacher) {
    this.teacher = teacher;
  }

  public Section getSection() {
    return section;
  }

  public void setSection(Section section) {
    this.section = section;
  }

  public LocalDate getWorkDate() {
    return workDate;
  }

  public void setWorkDate(LocalDate workDate) {
    this.workDate = workDate;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public void setOriginalFileName(String originalFileName) {
    this.originalFileName = originalFileName;
  }

  public String getStoredPath() {
    return storedPath;
  }

  public void setStoredPath(String storedPath) {
    this.storedPath = storedPath;
  }

  public Instant getUploadedAt() {
    return uploadedAt;
  }

  public void setUploadedAt(Instant uploadedAt) {
    this.uploadedAt = uploadedAt;
  }
}
