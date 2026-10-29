package school.openbell.support;

import org.springframework.http.HttpStatus;

public class AppException extends RuntimeException {
  private final HttpStatus status;

  public AppException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }

  public HttpStatus status() {
    return status;
  }

  public static AppException bad(String message) {
    return new AppException(HttpStatus.BAD_REQUEST, message);
  }

  public static AppException forbidden(String message) {
    return new AppException(HttpStatus.FORBIDDEN, message);
  }

  public static AppException missing(String message) {
    return new AppException(HttpStatus.NOT_FOUND, message);
  }
}
