package school.openbell.support;

import java.nio.charset.StandardCharsets;

public final class PlainPdf {
  private PlainPdf() {}

  public static byte[] of(String title, String line) {
    String stream = "BT /F1 20 Tf 56 730 Td (" + esc(title) + ") Tj 0 -34 Td /F1 12 Tf (" + esc(line) + ") Tj ET";
    String[] bodies = {
      "<< /Type /Catalog /Pages 2 0 R >>",
      "<< /Type /Pages /Count 1 /Kids [3 0 R] >>",
      "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Contents 4 0 R /Resources << /Font << /F1 5 0 R >> >> >>",
      "<< /Length " + stream.length() + " >>\nstream\n" + stream + "\nendstream",
      "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>"
    };
    StringBuilder pdf = new StringBuilder("%PDF-1.4\n");
    int[] offsets = new int[bodies.length];
    for (int i = 0; i < bodies.length; i++) {
      offsets[i] = pdf.toString().getBytes(StandardCharsets.US_ASCII).length;
      pdf.append(i + 1).append(" 0 obj\n").append(bodies[i]).append("\nendobj\n");
    }
    int xref = pdf.toString().getBytes(StandardCharsets.US_ASCII).length;
    pdf.append("xref\n0 ").append(bodies.length + 1).append('\n');
    pdf.append("0000000000 65535 f \n");
    for (int offset : offsets) {
      pdf.append(String.format("%010d 00000 n \n", offset));
    }
    pdf.append("trailer\n<< /Size ").append(bodies.length + 1).append(" /Root 1 0 R >>\n");
    pdf.append("startxref\n").append(xref).append("\n%%EOF\n");
    return pdf.toString().getBytes(StandardCharsets.US_ASCII);
  }

  private static String esc(String value) {
    StringBuilder out = new StringBuilder();
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '(' || c == ')' || c == '\\') {
        out.append('\\').append(c);
      } else if (c >= 32 && c < 127) {
        out.append(c);
      } else {
        out.append(' ');
      }
    }
    return out.toString();
  }
}
