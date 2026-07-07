package com.lh.idlestore.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.idlestore.user.repository.dataobject.RoleDO;

import java.util.List;

public interface RoleMapper extends BaseMapper<RoleDO> {

    /**
     * 查询所有被启用的角色
     */
    List<RoleDO> selectEnabledList();
}
