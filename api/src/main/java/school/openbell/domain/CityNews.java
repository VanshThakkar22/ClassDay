package school.openbell.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "city_news")
public class CityNews {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String sourceName;

  @Column(nullable = false)
  private String sourceUrl;

  @Column(nullable = false, length = 2000)
  private String summary;

  @Column(nullable = false)
  private Instant publishedAt;

  @Column(nullable = false)
  private String city;

  @Column(nullable = false)
  private boolean sample;

  public Long getId() { return id; }
  public String getTitle() { return title; }
  public String getSourceName() { return sourceName; }
  public String getSourceUrl() { return sourceUrl; }
  public String getSummary() { return summary; }
  public Instant getPublishedAt() { return publishedAt; }
  public String getCity() { return city; }
  public boolean isSample() { return sample; }

  public void setTitle(String title) { this.title = title; }
  public void setSourceName(String sourceName) { this.sourceName = sourceName; }
  public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
  public void setSummary(String summary) { this.summary = summary; }
  public void setPublishedAt(Instant publishedAt) { this.publishedAt = publishedAt; }
  public void setCity(String city) { this.city = city; }
  public void setSample(boolean sample) { this.sample = sample; }
}
