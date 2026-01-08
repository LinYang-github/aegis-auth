package com.company.aegis.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.aegis.modules.system.entity.SysUser;
import com.company.aegis.modules.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AegisUserDetailsService implements UserDetailsService {

    private final SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser sysUser = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username));
        if (sysUser == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        // Check if user is deleted or disabled
        if (sysUser.getDeleted() != null && sysUser.getDeleted() == 1) {
            throw new UsernameNotFoundException("User deleted: " + username);
        }

        boolean enabled = sysUser.getStatus() == 1;
        boolean accountNonLocked = sysUser.getStatus() != 2;

        return new User(
                sysUser.getUsername(),
                sysUser.getPassword(),
                enabled,
                true, // accountNonExpired
                true, // credentialsNonExpired
                accountNonLocked,
                getAuthorities(sysUser.getId()));
    }

    private List<GrantedAuthority> getAuthorities(Long userId) {
        List<String> roleCodes = sysUserMapper.selectRoleCodesByUserId(userId);
        List<String> permissionCodes = sysUserMapper.selectPermissionCodesByUserId(userId);

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (roleCodes != null) {
            authorities.addAll(roleCodes.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }

        if (permissionCodes != null) {
            authorities.addAll(permissionCodes.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }

        return authorities;
    }
}
