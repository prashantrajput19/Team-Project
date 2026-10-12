package com.examly.springapp.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.examly.springapp.model.User;

// Wraps our User entity so Spring Security can read it
public class UserPrincipal implements UserDetails {
    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    // The role is saved as "Admin" or "User".
    // Spring's hasRole("Admin") checks for "ROLE_Admin", so we add the "ROLE_" prefix here.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getUserRole()));
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // We log in with email, so the email is the "username" for Spring Security
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // The account never expires, gets locked or gets disabled
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
