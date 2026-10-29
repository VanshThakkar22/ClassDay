package school.openbell.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.openbell.domain.Enums;
import school.openbell.security.CurrentUser;
import school.openbell.service.CityService;
import school.openbell.service.NoticeService;
import school.openbell.web.ApiModels.CityPage;
import school.openbell.web.ApiModels.EventRequest;
import school.openbell.web.ApiModels.NoticeView;

@RestController
public class CityController {
  private final CityService city;
  private final NoticeService notices;

  public CityController(CityService city, NoticeService notices) {
    this.city = city;
    this.notices = notices;
  }

  @GetMapping("/api/city")
  public CityPage page() {
    return city.page(CurrentUser.get());
  }

  @PostMapping("/api/city/events")
  public NoticeView event(@RequestBody EventRequest request) {
    return notices.cityEvent(CurrentUser.require(Enums.Role.PLATFORM_ADMIN), request);
  }
}
