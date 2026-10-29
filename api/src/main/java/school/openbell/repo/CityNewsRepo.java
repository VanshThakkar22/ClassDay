package school.openbell.repo;

import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import school.openbell.domain.CityNews;

public interface CityNewsRepo extends JpaRepository<CityNews, Long> {
  List<CityNews> findByCityAndPublishedAtAfterOrderByPublishedAtDesc(String city, Instant publishedAt);
}
