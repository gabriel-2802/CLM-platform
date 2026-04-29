package clm.user.demo.models;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RoleName {
    public static final String USER    = "ROLE_USER";
    public static final String ADMIN   = "ROLE_ADMIN";
    public static final String MANAGER = "ROLE_MANAGER";
}
