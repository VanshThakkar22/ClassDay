package school.openbell.web;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import school.openbell.security.CurrentUser;
import school.openbell.service.AccountService;
import school.openbell.web.ApiModels.LoginResponse;
import school.openbell.web.ApiModels.UserView;

@RestController
public class AuthController {
  private final AccountService accounts;

  public AuthController(AccountService accounts) {
    this.accounts = accounts;
  }

  @GetMapping("/api/health")
  public Map<String, Boolean> health() {
    return Map.of("ok", true);
  }

  @PostMapping("/api/auth/login")
  public LoginResponse login(@RequestBody LoginBody body) {
    return accounts.login(body.email(), body.password());
  }

  @GetMapping("/api/me")
  public UserView me() {
    return accounts.me(CurrentUser.get());
  }

  public record LoginBody(String email, String password) {}
}
