package schoolplatform.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import schoolplatform.domain.UserAccount;
import schoolplatform.repo.UserAccountRepository;

@Component
public class JwtFilter extends OncePerRequestFilter {
  private final JwtService jwtService;
  private final UserAccountRepository users;

  public JwtFilter(JwtService jwtService, UserAccountRepository users) {
    this.jwtService = jwtService;
    this.users = users;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (header != null && header.startsWith("Bearer ")) {
      String email = jwtService.readEmail(header.substring(7));
      if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
        Optional<UserAccount> found = users.findByEmail(email);
        if (found.isPresent()) {
          AppUser principal = new AppUser(found.get());
          var authentication =
              new UsernamePasswordAuthenticationToken(
                  principal, null, principal.getAuthorities());
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      }
    }
    filterChain.doFilter(request, response);
  }
}
