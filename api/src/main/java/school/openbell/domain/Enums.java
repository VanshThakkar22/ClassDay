package school.openbell.domain;

public final class Enums {
  private Enums() {}

  public enum Plan { SECTION, CAMPUS, CITY }

  public enum Role { PLATFORM_ADMIN, SCHOOL_ADMIN, TEACHER, PARENT, STUDENT }

  public enum Scope { SECTION, GRADE, SCHOOL, CITY }
}
