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
@Table(
    name = "teacher_assignments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"teacher_id", "section_id", "subject"}))
public class TeacherAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "teacher_id")
  private UserAccount teacher;

  @ManyToOne(optional = false)
  @JoinColumn(name = "section_id")
  private Section section;

  private String subject;

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

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }
}
