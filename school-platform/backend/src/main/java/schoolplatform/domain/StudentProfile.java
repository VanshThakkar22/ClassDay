package schoolplatform.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "user_id", unique = true)
  private UserAccount user;

  @ManyToOne(optional = false)
  @JoinColumn(name = "section_id")
  private Section section;

  private String admissionNo;

  private Integer schoolFee;

  private Integer busFee;

  private Integer otherFee;

  private String otherReason;

  public Long getId() {
    return id;
  }

  public UserAccount getUser() {
    return user;
  }

  public void setUser(UserAccount user) {
    this.user = user;
  }

  public Section getSection() {
    return section;
  }

  public void setSection(Section section) {
    this.section = section;
  }

  public String getAdmissionNo() {
    return admissionNo;
  }

  public void setAdmissionNo(String admissionNo) {
    this.admissionNo = admissionNo;
  }

  public int getSchoolFee() {
    return schoolFee == null ? 0 : schoolFee;
  }

  public void setSchoolFee(int schoolFee) {
    this.schoolFee = schoolFee;
  }

  public int getBusFee() {
    return busFee == null ? 0 : busFee;
  }

  public void setBusFee(int busFee) {
    this.busFee = busFee;
  }

  public int getOtherFee() {
    return otherFee == null ? 0 : otherFee;
  }

  public void setOtherFee(int otherFee) {
    this.otherFee = otherFee;
  }

  public String getOtherReason() {
    return otherReason == null ? "" : otherReason;
  }

  public void setOtherReason(String otherReason) {
    this.otherReason = otherReason;
  }
}
