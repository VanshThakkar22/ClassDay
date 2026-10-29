package school.openbell.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
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

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "school_id")
  private School school;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id")
  private UserAccount author;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Enums.Scope scope;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "section_id")
  private Section section;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "grade_id")
  private Grade grade;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false, length = 4000)
  private String body;

  @Column(nullable = false)
  private Instant createdAt;

  public Long getId() { return id; }
  public School getSchool() { return school; }
  public UserAccount getAuthor() { return author; }
  public Enums.Scope getScope() { return scope; }
  public Section getSection() { return section; }
  public Grade getGrade() { return grade; }
  public String getTitle() { return title; }
  public String getBody() { return body; }
  public Instant getCreatedAt() { return createdAt; }

  public void setSchool(School school) { this.school = school; }
  public void setAuthor(UserAccount author) { this.author = author; }
  public void setScope(Enums.Scope scope) { this.scope = scope; }
  public void setSection(Section section) { this.section = section; }
  public void setGrade(Grade grade) { this.grade = grade; }
  public void setTitle(String title) { this.title = title; }
  public void setBody(String body) { this.body = body; }
  public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
