package org.softwire.training.auth;

import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;

import java.util.Objects;

/**
 * Quick and dirty ConstraintSecurityHandler for use with the admin interface,
 * borrowed from https://stackoverflow.com/a/48757093
 */
public class AdminConstraintSecurityHandler extends ConstraintSecurityHandler {

    private static final String ADMIN_ROLE = "admin role";

    public AdminConstraintSecurityHandler(BasicPrincipal adminPrincipal) {
        final Constraint constraint = new Constraint(Constraint.__BASIC_AUTH, ADMIN_ROLE);
        constraint.setAuthenticate(true);
        constraint.setRoles(new String[]{ADMIN_ROLE});
        final ConstraintMapping cm = new ConstraintMapping();
        cm.setConstraint(constraint);
        cm.setPathSpec("/*");
        setAuthenticator(new BasicAuthenticator());
        addConstraintMapping(cm);
        setLoginService(new AdminLoginService(adminPrincipal));
    }

    static class AdminLoginService extends AbstractLoginService {

        private final UserPrincipal principal;
        private final String username;

        AdminLoginService(final BasicPrincipal adminPrincipal) {
            this.principal = new UserPrincipal(
                    adminPrincipal.getName(),
                    new Password(Objects.requireNonNull(adminPrincipal.getPassword()))
            );
            this.username = adminPrincipal.getUsername();
        }

        @Override
        protected String[] loadRoleInfo(final UserPrincipal principal) {
            if (this.principal.getName().equals(principal.getName())) {
                return new String[]{ADMIN_ROLE};
            }
            return new String[0];
        }

        @Override
        protected UserPrincipal loadUserInfo(final String userName) {
            return username.equals(userName) ? principal : null;
        }
    }
}