package com.lh.idlestore.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.idlestore.user.repository.dataobject.PermissionDO;

import java.util.List;

public interface PermissionMapper extends BaseMapper<PermissionDO> {
    /**
     * 查询 APP 端所有被启用的权限
     */
    List<PermissionDO> selectAppEnabledList();
}
