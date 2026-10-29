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
    name = "guardian_links",
    uniqueConstraints = @UniqueConstraint(columnNames = {"parent_id", "student_id"}))
public class GuardianLink {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "parent_id")
  private UserAccount parent;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id")
  private UserAccount student;

  public Long getId() {
    return id;
  }

  public UserAccount getParent() {
    return parent;
  }

  public void setParent(UserAccount parent) {
    this.parent = parent;
  }

  public UserAccount getStudent() {
    return student;
  }

  public void setStudent(UserAccount student) {
    this.student = student;
  }
}
