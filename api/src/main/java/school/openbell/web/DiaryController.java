package school.openbell.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import school.openbell.security.CurrentUser;
import school.openbell.service.DiaryService;
import school.openbell.web.ApiModels.DiaryView;

@RestController
public class DiaryController {
  private final DiaryService diaries;

  public DiaryController(DiaryService diaries) {
    this.diaries = diaries;
  }

  @GetMapping("/api/diary")
  public DiaryView open(@RequestParam(required = false) Long studentId, @RequestParam(required = false) String date) {
    return diaries.open(CurrentUser.get(), studentId, date);
  }
}
