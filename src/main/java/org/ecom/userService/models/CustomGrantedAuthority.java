package org.ecom.userService.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import tools.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomGrantedAuthority implements GrantedAuthority {

    private String authority;


    public CustomGrantedAuthority(){}

    public CustomGrantedAuthority(UserRoles userRoles) {
        this.authority = userRoles.getName();
    }

    @Override
    public @Nullable String getAuthority() {
        return authority;
    }
}
