package school.openbell.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import school.openbell.support.AppException;

@Service
public class FileStorage {
  private final Path dir;

  public FileStorage(@Value("${app.file-dir}") String directory) {
    this.dir = Path.of(directory);
    try {
      Files.createDirectories(dir);
    } catch (IOException ex) {
      throw new IllegalStateException("Could not open the file cupboard", ex);
    }
  }

  public String store(byte[] bytes) {
    String name = UUID.randomUUID() + ".pdf";
    try {
      Files.write(dir.resolve(name), bytes);
    } catch (IOException ex) {
      throw AppException.bad("The PDF could not be saved.");
    }
    return name;
  }

  public String store(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw AppException.bad("Choose a PDF, or leave the day empty.");
    }
    String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
    String type = file.getContentType() == null ? "" : file.getContentType();
    if (!original.endsWith(".pdf") && !type.equals("application/pdf")) {
      throw AppException.bad("Send a PDF. That is the sheet an absent student can open.");
    }
    try {
      return store(file.getBytes());
    } catch (IOException ex) {
      throw AppException.bad("The PDF could not be read.");
    }
  }

  public byte[] read(String storedName) {
    Path path = dir.resolve(storedName).normalize();
    if (!path.startsWith(dir)) {
      throw AppException.missing("That file is not here.");
    }
    try {
      return Files.readAllBytes(path);
    } catch (IOException ex) {
      throw AppException.missing("That file is not here.");
    }
  }

  public void delete(String storedName) {
    if (storedName == null) {
      return;
    }
    try {
      Files.deleteIfExists(dir.resolve(storedName).normalize());
    } catch (IOException ignored) {
      // A leftover file should not block a teacher from sending a new one.
    }
  }
}
