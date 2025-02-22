package com.eazybytes.security.services;

import com.eazybytes.model.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Data
public class UserDetailsImpl implements UserDetails {
    // This ensures that serialized instances of this class remain compatible across different versions.
    //
    private static final long serialVersionUID = 1L;

    // Fields

    private Long id;
    private String username;
    private String email;

    @JsonIgnore  // Prevents the password from being exposed in JSON responses (e.g., API responses).
    private String password;

    private boolean is2faEnabled; // Stores whether two-factor authentication (2FA) is enabled.

   /*
   This field is a collection of authorities (roles/permissions) assigned to a user in Spring Security.
   GrantedAuthority is an interface in Spring Security that represents a role or permission assigned to a user.
   Collection<> --> The field is a collection (List, Set, etc.), meaning a user can have multiple roles or permissions.
   The wildcard (? extends GrantedAuthority) means this collection can hold any subclass of GrantedAuthority.
   In practice, this allows flexibility:
    It can hold SimpleGrantedAuthority (the most common implementation).
    It can hold custom implementations of GrantedAuthority if needed.
   */
    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String email, String password,
                           boolean is2faEnabled, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.is2faEnabled = is2faEnabled;
        this.authorities = authorities;
    }
    // Since Spring Security requires roles to be in GrantedAuthority format,
    // we need to convert AppRole values (enum ->ROLE_USER, ROLE_ADMIN) into SimpleGrantedAuthority.

    // user.getRole() returns an AppRole enum value (ROLE_USER or ROLE_ADMIN).
    //.name() converts it to a String (e.g., "ROLE_USER").
    //new SimpleGrantedAuthority(user.getRole().name()) creates a GrantedAuthority.
    //Wraps the authority in a List since authorities is a collection.
    public static UserDetailsImpl build(User user) {
        GrantedAuthority authority = new SimpleGrantedAuthority(user.getRole().getRoleName().name());

        return new UserDetailsImpl(
                user.getUserId(),
                user.getUserName(),
                user.getEmail(),
                user.getPassword(),
                user.isTwoFactorEnabled(),
                List.of(authority) // Wrapping the single authority in a list
        );
    }

// How Does Spring Security Use This?
//Spring Security calls getAuthorities() during authentication to get the user's roles/permissions.
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }
// Returns the hashed password (used for authentication)
    @Override
    public String getUsername() {
        return username;
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

    public boolean is2faEnabled() {
        return is2faEnabled;
    }
// Object Comparison
// This ensures users are compared by id instead of memory reference.
// Prevents duplicate instances in authentication handling.
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}
