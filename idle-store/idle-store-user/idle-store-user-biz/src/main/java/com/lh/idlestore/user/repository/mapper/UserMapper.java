package com.lh.idlestore.user.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.idlestore.user.repository.dataobject.UserDO;


public interface UserMapper extends BaseMapper<UserDO> {
    UserDO selectByPhone(String phone);
}
