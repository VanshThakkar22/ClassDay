package schoolplatform.repo;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import schoolplatform.domain.CityNews;

public interface CityNewsRepository extends JpaRepository<CityNews, Long> {
  List<CityNews> findByCityAndPublishedAtAfterOrderByPublishedAtDesc(String city, Instant after);

  boolean existsByCityAndTitle(String city, String title);
}
