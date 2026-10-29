package schoolplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import schoolplatform.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class SchoolPlatformApplication {
  public static void main(String[] args) {
    SpringApplication.run(SchoolPlatformApplication.class, args);
  }
}
