package schoolplatform.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import schoolplatform.config.AppProperties;

@Service
public class FileStorageService {
  private final Path root;

  public FileStorageService(AppProperties properties) {
    this.root = Path.of(properties.getUploadDir()).toAbsolutePath();
  }

  public String store(MultipartFile file) {
    requirePdfName(file.getOriginalFilename());
    if (file.isEmpty()) {
      throw new IllegalArgumentException("Choose a PDF.");
    }
    return write(file.getOriginalFilename(), fileBytes(file));
  }

  public String storeBytes(String originalName, byte[] bytes) {
    return write(originalName, bytes);
  }

  public Resource load(String storedPath) {
    return new FileSystemResource(storedPath);
  }

  private String write(String originalName, byte[] bytes) {
    try {
      Files.createDirectories(root);
      String storedName = UUID.randomUUID() + "-" + safeName(originalName);
      Path target = root.resolve(storedName);
      Files.write(target, bytes);
      return target.toString();
    } catch (IOException ex) {
      throw new IllegalStateException("Could not store the PDF.");
    }
  }

  private static byte[] fileBytes(MultipartFile file) {
    try {
      return file.getBytes();
    } catch (IOException ex) {
      throw new IllegalArgumentException("Could not read the PDF.");
    }
  }

  static void requirePdfName(String originalName) {
    if (originalName == null || !originalName.toLowerCase().endsWith(".pdf")) {
      throw new IllegalArgumentException("Upload a PDF file.");
    }
  }

  private static String safeName(String originalName) {
    String cleaned = originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
    return cleaned.length() > 80 ? cleaned.substring(cleaned.length() - 80) : cleaned;
  }
}
