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
@Table(
    name = "teacher_sections",
    uniqueConstraints = @UniqueConstraint(columnNames = {"teacher_id", "section_id", "subject"}))
public class TeacherSection {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "teacher_id")
  private UserAccount teacher;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "section_id")
  private Section section;

  @Column(nullable = false)
  private String subject;

  public Long getId() { return id; }
  public UserAccount getTeacher() { return teacher; }
  public Section getSection() { return section; }
  public String getSubject() { return subject; }

  public void setTeacher(UserAccount teacher) { this.teacher = teacher; }
  public void setSection(Section section) { this.section = section; }
  public void setSubject(String subject) { this.subject = subject; }
}
