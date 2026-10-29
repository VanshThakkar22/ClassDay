package school.openbell.web;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.openbell.domain.Enums;
import school.openbell.security.CurrentUser;
import school.openbell.security.UserPrincipal;
import school.openbell.service.StructureService;
import school.openbell.web.ApiModels.SchoolCard;
import school.openbell.web.ApiModels.SchoolTree;

@RestController
public class OfficeController {
  private final StructureService structure;

  public OfficeController(StructureService structure) {
    this.structure = structure;
  }

  @GetMapping("/api/office/tree")
  public SchoolTree office() {
    UserPrincipal principal = CurrentUser.require(Enums.Role.SCHOOL_ADMIN);
    return structure.tree(principal, principal.schoolId());
  }

  @GetMapping("/api/platform/schools")
  public List<SchoolCard> schools() {
    CurrentUser.require(Enums.Role.PLATFORM_ADMIN);
    return structure.cards();
  }

  @GetMapping("/api/platform/schools/{id}")
  public SchoolTree school(@PathVariable Long id) {
    return structure.tree(CurrentUser.require(Enums.Role.PLATFORM_ADMIN), id);
  }
}
