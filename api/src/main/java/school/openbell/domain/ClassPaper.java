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
import java.time.Instant;

@Entity
@Table(name = "class_papers")
public class ClassPaper {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "grade_id")
  private Grade grade;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "teacher_id")
  private UserAccount teacher;

  @Column(nullable = false)
  private String title;

  @Column(length = 2000)
  private String note;

  @Column(nullable = false)
  private String storedName;

  @Column(nullable = false)
  private String originalName;

  @Column(nullable = false)
  private Instant createdAt;

  public Long getId() { return id; }
  public Grade getGrade() { return grade; }
  public UserAccount getTeacher() { return teacher; }
  public String getTitle() { return title; }
  public String getNote() { return note; }
  public String getStoredName() { return storedName; }
  public String getOriginalName() { return originalName; }
  public Instant getCreatedAt() { return createdAt; }

  public void setGrade(Grade grade) { this.grade = grade; }
  public void setTeacher(UserAccount teacher) { this.teacher = teacher; }
  public void setTitle(String title) { this.title = title; }
  public void setNote(String note) { this.note = note; }
  public void setStoredName(String storedName) { this.storedName = storedName; }
  public void setOriginalName(String originalName) { this.originalName = originalName; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
