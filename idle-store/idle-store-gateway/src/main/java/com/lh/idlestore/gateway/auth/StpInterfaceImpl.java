package com.lh.idlestore.gateway.auth;

import cn.dev33.satoken.stp.StpInterface;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.lh.idlestore.cache.contract.auth.AuthCacheKeys;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义权限验证接口扩展
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public List<String> getPermissionList(Object loginId, String loginType) {
        // 返回此 loginId 拥有的权限列表
        log.info("## 获取用户权限列表, loginId: {}", loginId);
        // 从 redis 获取
        // 用户 redis Key
        String userRolesKey = AuthCacheKeys.userRoles(
                Long.valueOf(loginId.toString()));
        // 用户角色集合
        List<String> userRoleKeys = Convert.toList(String.class, redisTemplate.opsForValue().get(userRolesKey));
        if (CollUtil.isEmpty(userRoleKeys)) {
            return null;
        }

        if (CollUtil.isNotEmpty(userRoleKeys)) {
            // 查询这些角色对应的权限
            List<String> rolePermissionsKeys = userRoleKeys.stream()
                    .map(AuthCacheKeys::rolePermissions)
                    .toList();

            // 通过 key 集合批量查询权限，提升查询性能。
            List<String> rolePermissionsValues = redisTemplate.opsForValue().multiGet(rolePermissionsKeys).stream().map(Object::toString).toList();

            if (CollUtil.isNotEmpty(rolePermissionsValues)) {
                List<String> permissions = Lists.newArrayList();

                // 遍历所有角色的权限集合，统一添加到 permissions 集合中
                rolePermissionsValues.forEach(jsonValue -> {
                    try {
                        // 将 JSON 字符串转换为 List<String> 权限集合
                        List<String> rolePermissions = objectMapper.readValue(jsonValue, new TypeReference<>() {});
                        permissions.addAll(rolePermissions);
                    } catch (JsonProcessingException e) {
                        log.error("==> JSON 解析错误: ", e);
                    }
                });

                // 返回此用户所拥有的权限
                return permissions;
            }
        }

        return null;
    }

    @SneakyThrows
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 返回此 loginId 拥有的角色列表
        log.info("## 获取用户角色列表, loginId: {}", loginId);
        // 从 redis 获取
        String userRolesKey = AuthCacheKeys.userRoles(
                Long.valueOf(loginId.toString()));
        List<String> userRolesValue = Convert.toList(String.class, redisTemplate.opsForValue().get(userRolesKey));
        if (CollUtil.isEmpty(userRolesValue)) {
            return null;
        }
        System.out.println(userRolesValue);
//        String json = objectMapper.readValue(userRolesValue, String.class);
        // JSON 转 List<String> 集合
        return userRolesValue;
    }
}
