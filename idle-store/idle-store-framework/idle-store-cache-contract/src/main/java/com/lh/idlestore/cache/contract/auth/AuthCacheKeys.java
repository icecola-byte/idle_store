package com.lh.idlestore.cache.contract.auth;

/**
 * 认证权限缓存的跨服务 Key 契约。
 *
 * <p>User 服务负责写入权限缓存，Gateway 负责读取，双方必须使用完全一致的
 * Key 格式。修改这里的前缀属于跨服务缓存协议变更。</p>
 */
public final class AuthCacheKeys {

    private static final String USER_ROLES_PREFIX = "user:roles:";

    private static final String ROLE_PERMISSIONS_PREFIX =
            "role:permissions:";

    private AuthCacheKeys() {
    }

    public static String userRoles(Long userId) {
        return USER_ROLES_PREFIX + userId;
    }

    public static String rolePermissions(String roleKey) {
        return ROLE_PERMISSIONS_PREFIX + roleKey;
    }
}
