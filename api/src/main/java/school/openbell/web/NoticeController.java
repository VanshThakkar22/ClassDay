package school.openbell.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.openbell.domain.Enums;
import school.openbell.security.CurrentUser;
import school.openbell.service.NoticeService;
import school.openbell.web.ApiModels.NoticeRequest;
import school.openbell.web.ApiModels.NoticeView;

@RestController
public class NoticeController {
  private final NoticeService notices;

  public NoticeController(NoticeService notices) {
    this.notices = notices;
  }

  @PostMapping("/api/notices")
  public NoticeView create(@RequestBody NoticeRequest request) {
    CurrentUser.require(Enums.Role.TEACHER, Enums.Role.SCHOOL_ADMIN);
    return notices.create(CurrentUser.get(), request);
  }

  @GetMapping("/api/notices")
  public List<NoticeView> sent() {
    CurrentUser.require(Enums.Role.TEACHER, Enums.Role.SCHOOL_ADMIN, Enums.Role.PLATFORM_ADMIN);
    return notices.sent(CurrentUser.get());
  }
}
