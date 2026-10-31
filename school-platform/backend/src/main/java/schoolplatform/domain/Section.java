package schoolplatform.domain;

import jakarta.persistence.Entity;
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

  @ManyToOne(optional = false)
  @JoinColumn(name = "grade_id")
  private Grade grade;

  @ManyToOne
  @JoinColumn(name = "class_teacher_id")
  private UserAccount classTeacher;

  private String name;

  public Long getId() {
    return id;
  }

  public Grade getGrade() {
    return grade;
  }

  public void setGrade(Grade grade) {
    this.grade = grade;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public UserAccount getClassTeacher() {
    return classTeacher;
  }

  public void setClassTeacher(UserAccount classTeacher) {
    this.classTeacher = classTeacher;
  }
}
