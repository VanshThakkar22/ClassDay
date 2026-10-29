package school.openbell.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.openbell.repo.CityNewsRepo;
import school.openbell.security.UserPrincipal;
import school.openbell.web.ApiModels.CityPage;
import school.openbell.web.ApiModels.NewsItem;

@Service
public class CityService {
  private final CityNewsRepo news;
  private final NoticeService notices;

  public CityService(CityNewsRepo news, NoticeService notices) {
    this.news = news;
    this.notices = notices;
  }

  @Transactional(readOnly = true)
  public CityPage page(UserPrincipal principal) {
    Instant since = Instant.now().minus(Duration.ofHours(24));
    List<NewsItem> items = news.findByCityAndPublishedAtAfterOrderByPublishedAtDesc("Saharanpur", since).stream()
        .map(row -> new NewsItem(
            row.getId(),
            row.getTitle(),
            row.getSourceName(),
            row.getSourceUrl(),
            row.getSummary(),
            row.getPublishedAt(),
            row.isSample()))
        .toList();
    return new CityPage(items, notices.sent(platformView(principal)));
  }

  private UserPrincipal platformView(UserPrincipal principal) {
    return new UserPrincipal(principal.id(), principal.email(), school.openbell.domain.Enums.Role.PLATFORM_ADMIN, null, principal.name());
  }
}
