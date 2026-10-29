package schoolplatform.support;

import java.nio.charset.StandardCharsets;

public final class SimplePdf {
  private SimplePdf() {}

  public static byte[] text(String line) {
    String escaped =
        line.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    String stream = "BT /F1 16 Tf 48 140 Td (" + escaped + ") Tj ET";
    String[] objects =
        new String[] {
          "1 0 obj<< /Type /Catalog /Pages 2 0 R >>endobj\n",
          "2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1 >>endobj\n",
          "3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources<< /Font<< /F1 5 0 R >> >> >>endobj\n",
          "4 0 obj<< /Length "
              + stream.length()
              + " >>stream\n"
              + stream
              + "\nendstream\nendobj\n",
          "5 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>endobj\n"
        };
    StringBuilder body = new StringBuilder("%PDF-1.4\n");
    int[] offsets = new int[objects.length];
    for (int i = 0; i < objects.length; i++) {
      offsets[i] = body.length();
      body.append(objects[i]);
    }
    int xref = body.length();
    body.append("xref\n0 ").append(objects.length + 1).append("\n");
    body.append("0000000000 65535 f \n");
    for (int offset : offsets) {
      body.append(String.format("%010d 00000 n \n", offset));
    }
    body.append("trailer<< /Size ")
        .append(objects.length + 1)
        .append(" /Root 1 0 R >>\nstartxref\n")
        .append(xref)
        .append("\n%%EOF");
    return body.toString().getBytes(StandardCharsets.US_ASCII);
  }
}
