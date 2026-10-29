package school.openbell.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import school.openbell.domain.Enums;

@Service
public class JwtService {
  private final SecretKey key;

  public JwtService(@Value("${app.jwt-secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String issue(UserPrincipal principal) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject(principal.email())
        .claim("uid", principal.id())
        .claim("role", principal.role().name())
        .claim("sid", principal.schoolId())
        .claim("name", principal.name())
        .issuedAt(Date.from(now))
        .expiration(Date.from(now.plusSeconds(60L * 60 * 24 * 14)))
        .signWith(key)
        .compact();
  }

  public UserPrincipal read(String token) {
    var claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    Number sid = claims.get("sid", Number.class);
    return new UserPrincipal(
        claims.get("uid", Number.class).longValue(),
        claims.getSubject(),
        Enums.Role.valueOf(claims.get("role", String.class)),
        sid == null ? null : sid.longValue(),
        claims.get("name", String.class));
  }
}
