package schoolplatform.web;

import org.springframework.security.core.Authentication;
import schoolplatform.domain.UserAccount;
import schoolplatform.security.AppUser;
import schoolplatform.support.ForbiddenException;

public final class CurrentUser {
  private CurrentUser() {}

  public static UserAccount require(Authentication authentication) {
    if (authentication == null || !(authentication.getPrincipal() instanceof AppUser appUser)) {
      throw new ForbiddenException("Sign in required.");
    }
    return appUser.user();
  }
}
