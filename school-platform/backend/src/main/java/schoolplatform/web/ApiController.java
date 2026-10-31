package schoolplatform.web;

import java.time.LocalDate;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import schoolplatform.service.SchoolService;

@RestController
@RequestMapping("/api")
public class ApiController {
  private final SchoolService schoolService;

  public ApiController(SchoolService schoolService) {
    this.schoolService = schoolService;
  }

  @GetMapping("/health")
  Map<String, String> health() {
    return Map.of("status", "ok");
  }

  @PostMapping("/auth/login")
  Dto.LoginResponse login(@RequestBody Dto.LoginRequest request) {
    return schoolService.login(request.email(), request.password());
  }

  @GetMapping("/home")
  Dto.HomeResponse home(
      Authentication authentication,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
    return schoolService.home(CurrentUser.require(authentication), date);
  }

  @PostMapping(value = "/daily-work", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  Dto.DailyFile uploadDaily(
      Authentication authentication,
      @RequestParam Long sectionId,
      @RequestParam String subject,
      @RequestParam(required = false) String note,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
      @RequestParam("file") MultipartFile file) {
    return schoolService.uploadDaily(
        CurrentUser.require(authentication), sectionId, subject, note, workDate, file);
  }

  @PostMapping(value = "/class-papers", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  Dto.PaperFile uploadPaper(
      Authentication authentication,
      @RequestParam Long gradeId,
      @RequestParam String title,
      @RequestParam("file") MultipartFile file) {
    return schoolService.uploadPaper(CurrentUser.require(authentication), gradeId, title, file);
  }

  @PostMapping("/notices")
  Dto.NoticeView postNotice(Authentication authentication, @RequestBody Dto.NoticeRequest request) {
    return schoolService.postNotice(CurrentUser.require(authentication), request);
  }

  @DeleteMapping("/notices/{id}")
  void deleteNotice(Authentication authentication, @PathVariable Long id) {
    schoolService.deleteNotice(CurrentUser.require(authentication), id);
  }

  @PostMapping("/city-news/ingest")
  Dto.NewsItem ingest(
      @RequestHeader(value = "X-Ingest-Key", required = false) String key,
      @RequestBody Dto.CityNewsRequest request) {
    return schoolService.ingest(key, request);
  }

  @GetMapping("/files/daily/{id}")
  ResponseEntity<org.springframework.core.io.Resource> daily(
      Authentication authentication, @org.springframework.web.bind.annotation.PathVariable Long id) {
    return pdf(
        schoolService.openDaily(CurrentUser.require(authentication), id), schoolService.dailyName(id));
  }

  @GetMapping("/files/papers/{id}")
  ResponseEntity<org.springframework.core.io.Resource> paper(
      Authentication authentication, @org.springframework.web.bind.annotation.PathVariable Long id) {
    return pdf(
        schoolService.openPaper(CurrentUser.require(authentication), id), schoolService.paperName(id));
  }

  private static ResponseEntity<org.springframework.core.io.Resource> pdf(
      org.springframework.core.io.Resource resource, String filename) {
    String safe = filename.replace("\"", "");
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safe + "\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(resource);
  }
}
