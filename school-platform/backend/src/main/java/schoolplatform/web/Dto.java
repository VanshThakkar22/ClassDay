package schoolplatform.web;

import java.time.Instant;
import java.util.List;

public final class Dto {
  private Dto() {}

  public record LoginRequest(String email, String password) {}

  public record LoginResponse(String token, UserView user) {}

  public record UserView(
      Long id, String fullName, String email, String role, String schoolName, Long schoolId) {}

  public record HomeResponse(
      UserView user,
      String date,
      String focus,
      List<TeacherCard> teacherCards,
      List<DailyFile> todayWork,
      List<PaperFile> classPapers,
      List<NoticeView> notices,
      List<GradeBlock> grades,
      List<SchoolCard> schools,
      List<NewsItem> cityNews,
      String sectionLabel) {}

  public record TeacherCard(
      Long sectionId,
      Long gradeId,
      int classLevel,
      String sectionName,
      String classLabel,
      String subject,
      DailyFile today) {}

  public record DailyFile(
      Long id, String subject, String teacherName, String fileName, String note, String uploadedAt) {}

  public record PaperFile(
      Long id, String title, String fileName, String classLabel, String uploadedBy, String createdAt) {}

  public record NoticeView(
      Long id,
      String title,
      String body,
      String scope,
      String targetLabel,
      String createdAt,
      Integer seenCount,
      Integer audienceCount) {}

  public record GradeBlock(Long gradeId, int level, List<SectionLine> sections) {}

  public record SectionLine(
      Long sectionId, String name, List<String> teachers, int filesToday, int studentCount) {}

  public record SchoolCard(Long id, String name, String plan, int classes, int sections) {}

  public record NewsItem(
      Long id, String title, String sourceName, String sourceUrl, String summary, String publishedAt) {}

  public record NoticeRequest(String scope, Long sectionId, Long gradeId, String title, String body) {}

  public record CityNewsRequest(
      String city,
      String title,
      String sourceName,
      String sourceUrl,
      String summary,
      Instant publishedAt) {}
}
