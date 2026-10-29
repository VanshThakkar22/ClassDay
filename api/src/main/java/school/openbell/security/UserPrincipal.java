package school.openbell.security;

import school.openbell.domain.Enums;

public record UserPrincipal(Long id, String email, Enums.Role role, Long schoolId, String name) {}
