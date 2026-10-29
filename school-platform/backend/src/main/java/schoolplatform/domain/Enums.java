package schoolplatform.domain;

public final class Enums {
  private Enums() {}

  public enum Role {
    PLATFORM_ADMIN,
    SCHOOL_ADMIN,
    TEACHER,
    PARENT,
    STUDENT
  }

  public enum Plan {
    SECTION,
    CAMPUS,
    CITY
  }

  public enum Scope {
    SECTION,
    GRADE,
    SCHOOL,
    CITY
  }
}
