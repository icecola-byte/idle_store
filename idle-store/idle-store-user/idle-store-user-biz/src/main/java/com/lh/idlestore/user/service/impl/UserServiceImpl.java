package com.lh.idlestore.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.lh.framework.biz.context.holder.LoginUserContextHolder;
import com.lh.framework.common.exception.BizException;
import com.lh.framework.common.util.ParamUtils;
import com.lh.idlestore.cache.contract.auth.AuthCacheKeys;
import com.lh.framework.web.enums.CommonResponseCodeEnum;
import com.lh.idlestore.user.constant.RoleConstants;
import com.lh.idlestore.user.constant.UserDefaultConstants;
import com.lh.idlestore.user.repository.dataobject.RoleDO;
import com.lh.idlestore.user.repository.dataobject.UserDO;
import com.lh.idlestore.user.repository.dataobject.UserRoleDO;
import com.lh.idlestore.user.repository.mapper.RoleMapper;
import com.lh.idlestore.user.repository.mapper.UserMapper;
import com.lh.idlestore.user.repository.mapper.UserRoleMapper;
import com.lh.idlestore.user.enums.UserResponseCodeEnum;
import com.lh.idlestore.user.enums.SexEnum;
import com.lh.idlestore.user.enums.UserStatusEnum;
import com.lh.idlestore.user.model.vo.request.UpdateUserInfoReqVO;
import com.lh.idlestore.user.model.vo.response.UserProfileRespVO;
import com.lh.idlestore.user.service.UserService;
import com.lh.idlestore.user.dto.request.FindUserByPhoneRequest;
import com.lh.idlestore.user.dto.request.MiniLoginRequest;
import com.lh.idlestore.user.dto.request.BatchFindUsersRequest;
import com.lh.idlestore.user.dto.response.FindUserByPhoneResponse;
import com.lh.idlestore.user.dto.response.MiniLoginResponse;
import com.lh.idlestore.user.dto.response.UserBriefResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;
import java.util.Objects;


@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private UserRoleMapper userRoleMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    private static final String PHONE_REGEX = "\\d{11}";


    /**
     * 更新用户信息
     */
    @Override
    public UserProfileRespVO updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {
        Long userId = LoginUserContextHolder.getUserId();
        if (Objects.isNull(userId)) {
            throw new IllegalStateException("用户上下文缺失");
        }

        UserDO currentUser = userMapper.selectById(userId);
        if (Objects.isNull(currentUser) || Boolean.TRUE.equals(currentUser.getIsDeleted())) {
            throw new BizException(UserResponseCodeEnum.USER_NOT_FOUND);
        }

        UserDO userDO = new UserDO();
        userDO.setUserId(userId);
        boolean needUpdate = false;
        // 头像
        String avatarUrl = updateUserInfoReqVO.getAvatarUrl();
        if (Objects.nonNull(avatarUrl)) {
            Preconditions.checkArgument(StringUtils.isNotBlank(avatarUrl), CommonResponseCodeEnum.PARAM_NOT_VALID.getErrorMessage());
            userDO.setAvatarUrl(avatarUrl);
            needUpdate = true;
        }
        // 用户名
        String username = updateUserInfoReqVO.getUsername();
        if (Objects.nonNull(username)) {
            Preconditions.checkArgument(ParamUtils.checkUsername(username), UserResponseCodeEnum.NICK_NAME_VALID_FAIL.getErrorMessage());
            userDO.setUsername(username);
            needUpdate = true;
        }
        // 手机号
        String phone = updateUserInfoReqVO.getPhone();
        if (Objects.nonNull(phone)) {
            Preconditions.checkArgument(phone.matches(PHONE_REGEX), UserResponseCodeEnum.PHONE_VALID_FAIL.getErrorMessage());
            UserDO phoneOwner = userMapper.selectByPhone(phone);
            Preconditions.checkArgument(Objects.isNull(phoneOwner) || Objects.equals(phoneOwner.getUserId(), userId),
                    UserResponseCodeEnum.PHONE_ALREADY_EXISTS.getErrorMessage());
            userDO.setPhone(phone);
            needUpdate = true;
        }
        // 性别
        SexEnum sex = updateUserInfoReqVO.getSex();
        if (Objects.nonNull(sex)) {
            userDO.setSex(sex);
            needUpdate = true;
        }
        // 所属社区
        String communityId = updateUserInfoReqVO.getCommunityId();
        if (Objects.nonNull(communityId)) {
            Preconditions.checkArgument(StringUtils.isNotBlank(communityId), CommonResponseCodeEnum.PARAM_NOT_VALID.getErrorMessage());
            userDO.setCommunityId(communityId);
            needUpdate = true;
        }
        // 真实姓名
        if (Objects.nonNull(updateUserInfoReqVO.getRealName())) {
            userDO.setRealName(updateUserInfoReqVO.getRealName());
            needUpdate = true;
        }
        // 详细地址
        if (Objects.nonNull(updateUserInfoReqVO.getAddressDetail())) {
            userDO.setAddressDetail(updateUserInfoReqVO.getAddressDetail());
            needUpdate = true;
        }
        // 更新时间
        if (needUpdate) {
            userDO.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(userDO);
        }
        return getCurrentUserProfile();
    }

    /**
     * 小程序端用户注册
     */
    @Override
    public MiniLoginResponse login(MiniLoginRequest request) {
        String phone = request.getPhone();

        UserDO user = userMapper.selectByPhone(phone);
        // 已注册，直接返回用户 ID
        if (Objects.nonNull(user)) {
            return BeanUtil.copyProperties(user, MiniLoginResponse.class);
        }
        // 用户不存在，进行注册
        UserDO userDO = registerUser(phone);

        if (Objects.isNull(userDO)) {
            throw new BizException(UserResponseCodeEnum.REGISTER_FAIL);
        }

        return BeanUtil.copyProperties(userDO, MiniLoginResponse.class);
    }

    @Override
    public FindUserByPhoneResponse findByPhone(FindUserByPhoneRequest request) {
        String phone = request.getPhone();

        // 根据手机号查询用户信息
        UserDO userDO = userMapper.selectByPhone(phone);

        // 判空
        if (Objects.isNull(userDO)) {
            throw new BizException(UserResponseCodeEnum.USER_NOT_FOUND);
        }

        // 构建返参
        FindUserByPhoneResponse response = BeanUtil.copyProperties(userDO, FindUserByPhoneResponse.class);

        return response;
    }

    @Override
    public UserBriefResponse findById(Long userId) {
        if (Objects.isNull(userId) || userId <= 0) {
            throw new BizException(CommonResponseCodeEnum.PARAM_NOT_VALID);
        }

        UserDO userDO = userMapper.selectById(userId);
        if (Objects.isNull(userDO) || Boolean.TRUE.equals(userDO.getIsDeleted())) {
            throw new BizException(UserResponseCodeEnum.USER_NOT_FOUND);
        }

        return buildUserBrief(userDO);
    }

    @Override
    public List<UserBriefResponse> findByIds(BatchFindUsersRequest request) {
        if (Objects.isNull(request) || Objects.isNull(request.getUserIds())
                || request.getUserIds().isEmpty()) {
            throw new BizException(CommonResponseCodeEnum.PARAM_NOT_VALID);
        }

        List<Long> userIds = request.getUserIds().stream()
                .filter(Objects::nonNull)
                .filter(userId -> userId > 0)
                .distinct()
                .limit(100)
                .toList();
        if (userIds.isEmpty()) {
            throw new BizException(CommonResponseCodeEnum.PARAM_NOT_VALID);
        }

        Collection<UserDO> users = userMapper.selectBatchIds(userIds);
        List<UserBriefResponse> result = users.stream()
                .filter(user -> !Boolean.TRUE.equals(user.getIsDeleted()))
                .map(this::buildUserBrief)
                .toList();
        return result;
    }

    private UserBriefResponse buildUserBrief(UserDO userDO) {
        return UserBriefResponse.builder()
                .userId(userDO.getUserId())
                .username(userDO.getUsername())
                .avatarUrl(userDO.getAvatarUrl())
                .status(Objects.isNull(userDO.getStatus()) ? null : userDO.getStatus().getValue())
                .build();
    }

    @Override
    public UserProfileRespVO getCurrentUserProfile() {
        Long userId = LoginUserContextHolder.getUserId();
        if (Objects.isNull(userId)) {
            // 不属于业务上的错误，是 userId 透传失败，可能没经过网关
            throw new IllegalStateException("用户上下文缺失");
        }
        UserDO userDO = userMapper.selectById(userId);
        if (Objects.isNull(userDO) || Boolean.TRUE.equals(userDO.getIsDeleted())) {
            throw new BizException(UserResponseCodeEnum.USER_NOT_FOUND);
        }
        return BeanUtil.copyProperties(userDO, UserProfileRespVO.class);
    }

    private UserDO registerUser(String phone) {
        // 开启事务
        return transactionTemplate.execute(status -> {
            try {
                UserDO userDO = UserDO.builder()
                        .phone(phone)
                        .username(UserDefaultConstants.DEFAULT_USERNAME)
                        .sex(UserDefaultConstants.DEFAULT_SEX)
                        .status(UserStatusEnum.NORMAL)
                        .avatarUrl(UserDefaultConstants.DEFAULT_AVATAR_URL)
                        .coinBalance(UserDefaultConstants.DEFAULT_COIN_BALANCE)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(false)
                        .build();
                userMapper.insert(userDO);

                Long userId = userDO.getUserId();
                UserRoleDO userRoleDO = UserRoleDO.builder()
                        .userId(userId)
                        .roleId(RoleConstants.COMMON_USER_ROLE_ID) // 用户默认角色就是普通用户
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .isDeleted(false)
                        .build();
                userRoleMapper.insert(userRoleDO);
                RoleDO roleDO = roleMapper.selectById(RoleConstants.COMMON_USER_ROLE_ID);
                // 将该用户的角色 KEY 存入 Redis 中
                List<String> roles = Lists.newArrayList();
                roles.add(roleDO.getRoleKey());
                String userRolesKey = AuthCacheKeys.userRoles(userId);
//                redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));
                redisTemplate.opsForValue().set(userRolesKey, roles);

                return userDO;
            } catch (Exception e) {
                status.setRollbackOnly(); // 标记事务为回归
                log.error("==> 系统注册用户异常: ", e);
                return null;
            }
        });
    }
}
