package school.openbell.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "students")
public class Student {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "school_id")
  private School school;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "section_id")
  private Section section;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private UserAccount user;

  @Column(nullable = false)
  private String name;

  public Long getId() { return id; }
  public School getSchool() { return school; }
  public Section getSection() { return section; }
  public UserAccount getUser() { return user; }
  public String getName() { return name; }

  public void setSchool(School school) { this.school = school; }
  public void setSection(Section section) { this.section = section; }
  public void setUser(UserAccount user) { this.user = user; }
  public void setName(String name) { this.name = name; }
}
