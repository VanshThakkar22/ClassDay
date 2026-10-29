package school.openbell.domain;

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
@Table(name = "guardians", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "parent_id"}))
public class Guardian {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "student_id")
  private Student student;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "parent_id")
  private UserAccount parent;

  public Long getId() { return id; }
  public Student getStudent() { return student; }
  public UserAccount getParent() { return parent; }

  public void setStudent(Student student) { this.student = student; }
  public void setParent(UserAccount parent) { this.parent = parent; }
}
