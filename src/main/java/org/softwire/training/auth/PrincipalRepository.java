package org.softwire.training.auth;

import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class PrincipalRepository {
    private static final String ADMIN_USERNAME = "admin";
    private static final String MODERATOR_USERNAME = "moderator";

    private final Map<String, BasicPrincipal> principals;
    private final BasicPrincipal adminPrincipal;

    public PrincipalRepository(String adminPassword, String moderatorPassword) {
        adminPrincipal = new BasicPrincipal(
                "Admin Prinicpal",
                ADMIN_USERNAME,
                adminPassword,
                Arrays.asList(Roles.IMAGE_ADMINISTRATOR, Roles.USER_ADMINISTRATOR)
        );
        principals = ImmutableMap.<String, BasicPrincipal>builder()
                .put(ADMIN_USERNAME, adminPrincipal)
                .put(MODERATOR_USERNAME, new BasicPrincipal(
                        "Moderator Prinicpal",
                        MODERATOR_USERNAME,
                        moderatorPassword,
                        Collections.singletonList(Roles.IMAGE_ADMINISTRATOR))
                ).build();

    }

    public Optional<BasicPrincipal> getByUsername(String username) {
        return Optional.ofNullable(principals.get(username));
    }

    public BasicPrincipal getAdminPrincipal() {
        return adminPrincipal;
    }
}
