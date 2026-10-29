package schoolplatform.domain;

import jakarta.persistence.Entity;
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

  @ManyToOne(optional = false)
  @JoinColumn(name = "grade_id")
  private Grade grade;

  @ManyToOne(optional = false)
  @JoinColumn(name = "uploaded_by")
  private UserAccount uploadedBy;

  private String title;
  private String originalFileName;
  private String storedPath;
  private Instant createdAt;

  public Long getId() {
    return id;
  }

  public Grade getGrade() {
    return grade;
  }

  public void setGrade(Grade grade) {
    this.grade = grade;
  }

  public UserAccount getUploadedBy() {
    return uploadedBy;
  }

  public void setUploadedBy(UserAccount uploadedBy) {
    this.uploadedBy = uploadedBy;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
