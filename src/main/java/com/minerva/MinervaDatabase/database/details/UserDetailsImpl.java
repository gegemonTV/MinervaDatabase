package com.minerva.MinervaDatabase.database.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.minerva.MinervaDatabase.database.models.User;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;
    private Long id;

    private String username;
    private String alias;

    private String email;
    private String phone;

    @JsonIgnore
    private String password;
    @JsonIgnore
    private String resetPasswordToken;

    private Collection<? extends GrantedAuthority> authorities;

    private boolean is2FA = false;
    private boolean enabled = false;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

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
        return enabled;
    }

    public static UserDetailsImpl build(User user){
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getAlias(),
                user.getEmail(),
                user.getPhone(),
                user.getPassword(),
                user.getResetPasswordToken(),
                authorities,
                user.is2FA(),
                user.isEnabled()
        );
    }
}
