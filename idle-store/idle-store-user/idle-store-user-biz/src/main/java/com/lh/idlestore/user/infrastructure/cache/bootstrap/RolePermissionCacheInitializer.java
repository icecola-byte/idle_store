package com.lh.idlestore.user.infrastructure.cache.bootstrap;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lh.idlestore.cache.contract.auth.AuthCacheKeys;
import com.lh.idlestore.user.repository.dataobject.PermissionDO;
import com.lh.idlestore.user.repository.dataobject.RoleDO;
import com.lh.idlestore.user.repository.dataobject.RolePermissionDO;
import com.lh.idlestore.user.repository.mapper.PermissionMapper;
import com.lh.idlestore.user.repository.mapper.RoleMapper;
import com.lh.idlestore.user.repository.mapper.RolePermissionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 推送角色权限数据到 Redis 中
 **/
@Component
@RequiredArgsConstructor
@Slf4j
public class RolePermissionCacheInitializer implements ApplicationRunner {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    // 权限同步标记 Key --> 防止集群情况下，同时多个项目启动导致多次同步
    private static final String PUSH_PERMISSION_FLAG = "push.permission.flag";

    @Override
    public void run(ApplicationArguments args) {
        log.info("==> 服务启动，开始同步角色权限数据到 Redis 中...");
        try {
            // 解决重复同步(问题，项目启动后同步一次，如果同步失败，就不会加载到 redis 了)
            boolean canPushed = redisTemplate.opsForValue().setIfAbsent(PUSH_PERMISSION_FLAG, "1", 1, TimeUnit.DAYS);
            // 如果无法同步权限数据
            if (!canPushed) {
                log.warn("==> 角色权限数据已经同步至 Redis 中，不再同步...");
                return;
            }

            // 查询所有角色
            List<RoleDO> roleDOS = roleMapper.selectEnabledList();
            if (CollUtil.isNotEmpty(roleDOS)) {
                // 角色 Id 列表
                List<Long> roleIds = roleDOS.stream().map(RoleDO::getId).toList();

                List<RolePermissionDO> rolePermissionDOS = rolePermissionMapper.selectByRoleIds(roleIds);

                Map<Long, List<Long>> roleIdPermissionIdsMap = rolePermissionDOS.stream().collect(
                        Collectors.groupingBy(RolePermissionDO::getRoleId,
                                Collectors.mapping(RolePermissionDO::getPermissionId, Collectors.toList()))
                );
                // 查询 APP 端被启用的权限
                List<PermissionDO> permissionDOS = permissionMapper.selectAppEnabledList();
                // 权限 ID - 权限 DO
                Map<Long, PermissionDO> permissionIdDOMap = permissionDOS.stream().collect(
                        Collectors.toMap(PermissionDO::getId, permissionDO -> permissionDO)
                );

                // 组织 角色KEY-权限 关系
                Map<String, List<String>> roleIdPermissionDOMap = Maps.newHashMap();

                // 循环所有角色
                roleDOS.forEach(roleDO -> {
                    // 当前角色 ID
                    Long roleId = roleDO.getId();
                    String roleKey = roleDO.getRoleKey();
                    // 当前角色 ID 对应的权限 ID 集合
                    List<Long> permissionIds = roleIdPermissionIdsMap.get(roleId);
                    if (CollUtil.isNotEmpty(permissionIds)) {
                        List<String> permissionKeys = Lists.newArrayList();
                        permissionIds.forEach(permissionId -> {
                            // 根据权限 ID 获取具体的权限 DO 对象
                            PermissionDO permissionDO = permissionIdDOMap.get(permissionId);
                            if (Objects.nonNull(permissionDO)) {
                                permissionKeys.add(permissionDO.getPermissionKey());
                            }
                        });
                        roleIdPermissionDOMap.put(roleKey, permissionKeys);
                    }
                });

                // 同步至 Redis 中，方便后续网关查询鉴权使用
                roleIdPermissionDOMap.forEach((roleKey, permissions) -> {
                    String key = AuthCacheKeys.rolePermissions(roleKey);
                    redisTemplate.opsForValue().set(key, permissions);
                });
            }
            log.info("==> 服务启动，成功同步角色权限数据到 Redis 中...");
        }catch (Exception e) {
            log.error("==> 同步角色权限数据到 Redis 中失败: ", e);
        }


    }
}
