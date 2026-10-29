package school.openbell.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import school.openbell.domain.Enums;
import school.openbell.security.CurrentUser;
import school.openbell.security.UserPrincipal;
import school.openbell.service.DeskService;
import school.openbell.web.ApiModels.GradeOption;
import school.openbell.web.ApiModels.PaperView;
import school.openbell.web.ApiModels.TodayDesk;
import school.openbell.web.ApiModels.WorkFile;

@RestController
public class TeacherController {
  private final DeskService desk;

  public TeacherController(DeskService desk) {
    this.desk = desk;
  }

  @GetMapping("/api/teacher/today")
  public TodayDesk today() {
    return desk.today(teacher());
  }

  @PostMapping("/api/teacher/daily-work")
  public WorkFile daily(
      @RequestParam Long sectionId,
      @RequestParam(required = false) String note,
      @RequestParam("file") MultipartFile file) {
    return desk.uploadToday(teacher(), sectionId, note, file);
  }

  @GetMapping("/api/teacher/grades")
  public List<GradeOption> grades() {
    return desk.grades(teacher());
  }

  @GetMapping("/api/teacher/papers")
  public List<PaperView> papers() {
    return desk.papers(teacher());
  }

  @PostMapping("/api/teacher/papers")
  public PaperView paper(
      @RequestParam Long gradeId,
      @RequestParam String title,
      @RequestParam(required = false) String note,
      @RequestParam("file") MultipartFile file) {
    return desk.uploadPaper(teacher(), gradeId, title, note, file);
  }

  private UserPrincipal teacher() {
    return CurrentUser.require(Enums.Role.TEACHER);
  }
}
