package schoolplatform.web;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import schoolplatform.support.ForbiddenException;

@RestControllerAdvice
public class ApiErrors {
  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException ex) {
    return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
  }

  @ExceptionHandler(ForbiddenException.class)
  ResponseEntity<Map<String, String>> forbidden(ForbiddenException ex) {
    return ResponseEntity.status(403).body(Map.of("error", ex.getMessage()));
  }
}
