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

@Entity
@Table(name = "sections", uniqueConstraints = @UniqueConstraint(columnNames = {"grade_id", "name"}))
public class Section {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "grade_id")
  private Grade grade;

  @Column(nullable = false, length = 8)
  private String name;

  public Long getId() { return id; }
  public Grade getGrade() { return grade; }
  public String getName() { return name; }

  public void setGrade(Grade grade) { this.grade = grade; }
  public void setName(String name) { this.name = name; }
}
