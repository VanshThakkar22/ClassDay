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
      String sectionLabel,
      List<Staff> teachers) {}

  public record Staff(Long id, String fullName) {}

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
      Long id, String title, String fileName, String classLabel, String uploadedBy, String createdAt, Long uploadedById) {}

  public record NoticeView(
      Long id,
      String title,
      String body,
      String scope,
      String targetLabel,
      String createdAt,
      Integer seenCount,
      Integer audienceCount,
      Long authorId) {}

  public record GradeBlock(Long gradeId, int level, List<SectionLine> sections) {}

  public record SectionLine(
      Long sectionId,
      String name,
      List<String> teachers,
      int filesToday,
      int studentCount,
      Long classTeacherId,
      List<Duty> duties,
      String classTeacherName,
      List<UploadLine> uploads) {}

  public record Duty(Long id, Long teacherId, String teacherName, String subject) {}

  public record UploadLine(String subject, String teacherName) {}

  public record AssignRequest(Long sectionId, Long teacherId, String subject, boolean classTeacher) {}

  public record SchoolCard(Long id, String name, String plan, int classes, int sections) {}

  public record NewsItem(
      Long id, String title, String sourceName, String sourceUrl, String summary, String publishedAt, String scope) {}

  public record NoticeRequest(String scope, Long sectionId, Long gradeId, String title, String body) {}

  public record FeeLine(
      Long studentId,
      String studentName,
      String classLabel,
      int schoolFee,
      int busFee,
      int otherFee,
      String otherReason) {}

  public record FeeBoard(List<FeeLine> lines, boolean canEdit, boolean canRemind, String note) {}

  public record FeeSave(int schoolFee, int busFee, int otherFee, String otherReason) {}

  public record FeeRemind(String title, String body) {}

  public record CityNewsRequest(
      String city,
      String title,
      String sourceName,
      String sourceUrl,
      String summary,
      Instant publishedAt,
      String scope) {}
}
