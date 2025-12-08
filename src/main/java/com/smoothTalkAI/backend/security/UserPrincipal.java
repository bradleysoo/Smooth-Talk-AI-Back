package com.smoothTalkAI.backend.security;

import com.smoothTalkAI.backend.user.User;
import java.util.Collection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Builder
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String name;
    private final String profileImage;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal from(User user) {
        return UserPrincipal.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .profileImage(user.getProfileImage())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
            .build();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

