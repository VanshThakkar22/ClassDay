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
@Table(name = "grades", uniqueConstraints = @UniqueConstraint(columnNames = {"school_id", "level"}))
public class Grade {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "school_id")
  private School school;

  @Column(nullable = false)
  private int level;

  @Column(nullable = false)
  private String label;

  public Long getId() { return id; }
  public School getSchool() { return school; }
  public int getLevel() { return level; }
  public String getLabel() { return label; }

  public void setSchool(School school) { this.school = school; }
  public void setLevel(int level) { this.level = level; }
  public void setLabel(String label) { this.label = label; }
}
