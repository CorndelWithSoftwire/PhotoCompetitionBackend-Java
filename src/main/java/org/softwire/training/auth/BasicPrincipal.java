package org.softwire.training.auth;

import java.security.Principal;
import java.util.List;

public class BasicPrincipal implements Principal {
    private final String username;
    private final String name;
    private final List<String> roles;
    private final String password;

    BasicPrincipal(String name, String username, String password, List<String> roles) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public List<String> getRoles() {
        return roles;
    }
}
