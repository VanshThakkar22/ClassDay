package school.openbell.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import school.openbell.domain.UserAccount;
import school.openbell.repo.UserRepo;
import school.openbell.security.JwtService;
import school.openbell.security.UserPrincipal;
import school.openbell.support.AppException;
import school.openbell.support.SchoolClock;
import school.openbell.web.ApiModels.LoginResponse;
import school.openbell.web.ApiModels.UserView;

@Service
public class AccountService {
  private final UserRepo users;
  private final PasswordEncoder passwords;
  private final JwtService jwt;

  public AccountService(UserRepo users, PasswordEncoder passwords, JwtService jwt) {
    this.users = users;
    this.passwords = passwords;
    this.jwt = jwt;
  }

  @Transactional(readOnly = true)
  public LoginResponse login(String email, String password) {
    String clean = email == null ? "" : email.trim().toLowerCase();
    UserAccount user = users.findByEmail(clean).orElseThrow(() -> AppException.bad("That email is not on the book."));
    if (!passwords.matches(password == null ? "" : password, user.getPasswordHash())) {
      throw AppException.bad("That password does not match.");
    }
    UserPrincipal principal = principal(user);
    return new LoginResponse(jwt.issue(principal), view(user));
  }

  @Transactional(readOnly = true)
  public UserView me(UserPrincipal principal) {
    return view(users.findById(principal.id()).orElseThrow(() -> AppException.missing("This login is no longer here.")));
  }

  private UserView view(UserAccount user) {
    var today = SchoolClock.today();
    return new UserView(
        user.getId(),
        user.getName(),
        user.getEmail(),
        user.getRole().name(),
        user.getSchool() == null ? null : user.getSchool().getId(),
        user.getSchool() == null ? "Saharanpur" : user.getSchool().getName(),
        user.getSchool() == null ? null : user.getSchool().getPlan().name(),
        today.toString(),
        SchoolClock.label(today));
  }

  private UserPrincipal principal(UserAccount user) {
    return new UserPrincipal(
        user.getId(),
        user.getEmail(),
        user.getRole(),
        user.getSchool() == null ? null : user.getSchool().getId(),
        user.getName());
  }
}
