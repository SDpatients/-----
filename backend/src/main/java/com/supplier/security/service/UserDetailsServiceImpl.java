package com.supplier.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.supplier.security.model.LoginUser;
import com.supplier.system.entity.SysPermission;
import com.supplier.system.entity.SysRole;
import com.supplier.system.entity.SysUser;
import com.supplier.system.mapper.SysPermissionMapper;
import com.supplier.system.mapper.SysRoleMapper;
import com.supplier.system.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysPermissionMapper sysPermissionMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username)
                .eq(SysUser::getDeleted, 0)
                .last("LIMIT 1"));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        List<SysRole> roles = sysRoleMapper.selectByUserId(user.getId());
        List<SysPermission> permissions = getPermissionsWithCache(user.getId());
        return new LoginUser(user, roles, permissions);
    }

    /**
     * 带缓存的权限加载：
     * 先从 Redis 查，未命中再查 DB 并写入缓存（TTL 2小时）
     */
    @SuppressWarnings("unchecked")
    private List<SysPermission> getPermissionsWithCache(Long userId) {
        String key = "auth:permissions:" + userId;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof List<?> list && !list.isEmpty()) {
            log.debug("权限缓存命中: userId={}", userId);
            return (List<SysPermission>) list;
        }
        List<SysPermission> permissions = sysPermissionMapper.selectByUserId(userId);
        redisTemplate.opsForValue().set(key, permissions, 2, TimeUnit.HOURS);
        log.debug("权限已缓存: userId={}, count={}", userId, permissions.size());
        return permissions;
    }
}
