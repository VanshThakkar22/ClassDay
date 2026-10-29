package school.openbell.support;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class SchoolClock {
  public static final ZoneId ZONE = ZoneId.of("Asia/Kolkata");
  private static final DateTimeFormatter LONG =
      DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.UK);

  private SchoolClock() {}

  public static LocalDate today() {
    return LocalDate.now(ZONE);
  }

  public static String label(LocalDate date) {
    return date.format(LONG);
  }
}
