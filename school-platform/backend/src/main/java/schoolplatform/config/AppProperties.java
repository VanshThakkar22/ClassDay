package schoolplatform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppProperties {
  private String jwtSecret;
  private int jwtHours;
  private String ingestKey;
  private String uploadDir;

  public String getJwtSecret() {
    return jwtSecret;
  }

  public void setJwtSecret(String jwtSecret) {
    this.jwtSecret = jwtSecret;
  }

  public int getJwtHours() {
    return jwtHours;
  }

  public void setJwtHours(int jwtHours) {
    this.jwtHours = jwtHours;
  }

  public String getIngestKey() {
    return ingestKey;
  }

  public void setIngestKey(String ingestKey) {
    this.ingestKey = ingestKey;
  }

  public String getUploadDir() {
    return uploadDir;
  }

  public void setUploadDir(String uploadDir) {
    this.uploadDir = uploadDir;
  }
}
