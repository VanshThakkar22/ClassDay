package school.openbell.web;

import java.time.Instant;
import java.util.List;

public final class ApiModels {
  private ApiModels() {}

  public record UserView(
      Long id,
      String name,
      String email,
      String role,
      Long schoolId,
      String schoolName,
      String plan,
      String serverDate,
      String dateLabel) {}

  public record LoginResponse(String token, UserView user) {}

  public record WorkFile(Long id, String subject, String teacherName, String note, String filename) {}

  public record Colleague(String teacherName, String subject, Long fileId, String filename, String note) {}

  public record TodaySection(
      Long sectionId,
      String gradeLabel,
      String sectionName,
      String subject,
      WorkFile mine,
      List<Colleague> colleagues) {}

  public record TodayDesk(String date, String dateLabel, List<TodaySection> sections) {}

  public record GradeOption(Long gradeId, String label) {}

  public record PaperView(
      Long id,
      Long gradeId,
      String gradeLabel,
      String title,
      String note,
      String filename,
      String teacherName,
      Instant createdAt) {}

  public record NoticeView(
      Long id,
      String title,
      String body,
      String scope,
      String targetLabel,
      String authorName,
      Instant createdAt,
      int seenCount,
      int recipientCount,
      boolean seen) {}

  public record NoticeRequest(String scope, Long sectionId, Long gradeId, String title, String body) {}

  public record EventRequest(String title, String body) {}

  public record StudentChip(Long id, String name, String gradeLabel, String sectionName, String schoolName) {}

  public record DiaryView(
      String date,
      String dateLabel,
      List<StudentChip> students,
      Long selectedStudentId,
      List<WorkFile> work,
      List<PaperView> papers,
      List<NoticeView> notices) {}

  public record SectionFolder(Long id, String name, List<String> teachers, long students) {}

  public record GradeFolder(Long id, int level, String label, List<SectionFolder> sections) {}

  public record SchoolTree(
      Long id, String name, String area, String city, String plan, List<GradeFolder> grades) {}

  public record SchoolCard(Long id, String name, String area, String plan, int grades, int sections) {}

  public record NewsItem(
      Long id,
      String title,
      String sourceName,
      String sourceUrl,
      String summary,
      Instant publishedAt,
      boolean sample) {}

  public record CityPage(List<NewsItem> news, List<NoticeView> events) {}
}
