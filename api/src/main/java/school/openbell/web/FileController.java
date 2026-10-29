package school.openbell.web;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.openbell.security.CurrentUser;
import school.openbell.service.FileStorage;
import school.openbell.service.ReadingService;

@RestController
public class FileController {
  private final ReadingService reading;
  private final FileStorage files;

  public FileController(ReadingService reading, FileStorage files) {
    this.reading = reading;
    this.files = files;
  }

  @GetMapping("/api/files/daily/{id}")
  public ResponseEntity<byte[]> daily(@PathVariable Long id) {
    var row = reading.daily(CurrentUser.get(), id);
    return pdf(row.getOriginalName(), files.read(row.getStoredName()));
  }

  @GetMapping("/api/files/papers/{id}")
  public ResponseEntity<byte[]> paper(@PathVariable Long id) {
    var row = reading.paper(CurrentUser.get(), id);
    return pdf(row.getOriginalName(), files.read(row.getStoredName()));
  }

  private ResponseEntity<byte[]> pdf(String filename, byte[] body) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(filename).build().toString())
        .contentType(MediaType.APPLICATION_PDF)
        .body(body);
  }
}
