package school.openbell.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "schools")
public class School {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String city;

  @Column(nullable = false)
  private String area;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Enums.Plan plan;

  public Long getId() { return id; }
  public String getName() { return name; }
  public String getCity() { return city; }
  public String getArea() { return area; }
  public Enums.Plan getPlan() { return plan; }

  public void setName(String name) { this.name = name; }
  public void setCity(String city) { this.city = city; }
  public void setArea(String area) { this.area = area; }
  public void setPlan(Enums.Plan plan) { this.plan = plan; }
}
