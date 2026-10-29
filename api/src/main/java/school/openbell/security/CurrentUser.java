package school.openbell.security;

import org.springframework.security.core.context.SecurityContextHolder;
import school.openbell.domain.Enums;
import school.openbell.support.AppException;

public final class CurrentUser {
  private CurrentUser() {}

  public static UserPrincipal get() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof UserPrincipal principal)) {
      throw AppException.forbidden("Please sign in again.");
    }
    return principal;
  }

  public static UserPrincipal require(Enums.Role... roles) {
    UserPrincipal principal = get();
    for (Enums.Role role : roles) {
      if (principal.role() == role) {
        return principal;
      }
    }
    throw AppException.forbidden("That desk belongs to someone else.");
  }
}
