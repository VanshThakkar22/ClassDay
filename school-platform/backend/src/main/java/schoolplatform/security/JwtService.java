package schoolplatform.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import schoolplatform.config.AppProperties;
import schoolplatform.domain.UserAccount;

@Service
public class JwtService {
  private final SecretKey key;
  private final int hours;

  public JwtService(AppProperties properties) {
    this.key = Keys.hmacShaKeyFor(properties.getJwtSecret().getBytes(StandardCharsets.UTF_8));
    this.hours = properties.getJwtHours();
  }

  public String create(UserAccount user) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(user.getEmail())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(hours * 3600L)))
        .signWith(key)
        .compact();
  }

  public String readEmail(String token) {
    try {
      return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();
    } catch (RuntimeException ex) {
      return null;
    }
  }
}
