package school.openbell.config;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import school.openbell.support.AppException;

@RestControllerAdvice
public class RestErrors {
  private static final Logger log = LoggerFactory.getLogger(RestErrors.class);

  @ExceptionHandler(AppException.class)
  ResponseEntity<Map<String, String>> app(AppException ex) {
    return ResponseEntity.status(ex.status()).body(Map.of("message", ex.getMessage()));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<Map<String, String>> tooLarge() {
    return ResponseEntity.badRequest().body(Map.of("message", "That PDF is over 20 MB. Send a lighter file."));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<Map<String, String>> unexpected(Exception ex) {
    log.error("Request failed", ex);
    return ResponseEntity.internalServerError().body(Map.of("message", "Something in the book snagged. Try that once more."));
  }
}
