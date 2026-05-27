package com.supplier.security.model;

import com.supplier.system.entity.SysPermission;
import com.supplier.system.entity.SysRole;
import com.supplier.system.entity.SysUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class LoginUser implements UserDetails {

    private final SysUser user;
    private final List<SysRole> roles;
    private final List<SysPermission> permissions;
    private final List<GrantedAuthority> authorities;

    public LoginUser(SysUser user, List<SysRole> roles, List<SysPermission> permissions) {
        this.user = user;
        this.roles = roles;
        this.permissions = permissions;
        this.authorities = Stream.concat(
                        roles.stream().map(role -> "ROLE_" + role.getRoleCode()),
                        permissions.stream().map(SysPermission::getPermCode)
                )
                .filter(Objects::nonNull)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public Long getUserId() {
        return user.getId();
    }

    public String getRealName() {
        return user.getRealName();
    }

    public Long getSupplierId() {
        return user.getSupplierId();
    }

    public Integer getUserType() {
        return user.getUserType();
    }

    public List<String> getRoleCodes() {
        return roles.stream().map(SysRole::getRoleCode).toList();
    }

    public List<String> getPermissionCodes() {
        return permissions.stream().map(SysPermission::getPermCode).toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
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
        return Integer.valueOf(1).equals(user.getStatus());
    }
}
