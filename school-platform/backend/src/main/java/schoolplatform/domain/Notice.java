package schoolplatform.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "notices")
public class Notice {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "school_id")
  private School school;

  @ManyToOne(optional = false)
  @JoinColumn(name = "author_id")
  private UserAccount author;

  @Enumerated(EnumType.STRING)
  private Enums.Scope scope;

  @ManyToOne
  @JoinColumn(name = "section_id")
  private Section section;

  @ManyToOne
  @JoinColumn(name = "grade_id")
  private Grade grade;

  private String title;

  @Column(length = 2000)
  private String body;

  private Instant createdAt;

  public Long getId() {
    return id;
  }

  public School getSchool() {
    return school;
  }

  public void setSchool(School school) {
    this.school = school;
  }

  public UserAccount getAuthor() {
    return author;
  }

  public void setAuthor(UserAccount author) {
    this.author = author;
  }

  public Enums.Scope getScope() {
    return scope;
  }

  public void setScope(Enums.Scope scope) {
    this.scope = scope;
  }

  public Section getSection() {
    return section;
  }

  public void setSection(Section section) {
    this.section = section;
  }

  public Grade getGrade() {
    return grade;
  }

  public void setGrade(Grade grade) {
    this.grade = grade;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }
}
