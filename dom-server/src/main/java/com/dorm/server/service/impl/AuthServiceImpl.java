package com.dorm.server.service.impl;

import com.dorm.server.constant.MessageConstants;
import com.dorm.server.constant.RedisKeyConstants;
import com.dorm.server.constant.SystemConstants;
import com.dorm.server.entity.SysUser;
import com.dorm.server.entity.dto.LoginDTO;
import com.dorm.server.entity.vo.LoginVO;
import com.dorm.server.entity.vo.UserInfoVO;
import com.dorm.server.exception.BusinessException;
import com.dorm.server.mapper.SysUserMapper;
import com.dorm.server.service.AuthService;
import com.dorm.server.util.JwtUtil;
import com.dorm.server.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 认证业务实现类
 *
 * @author dorm-server
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    /** BCrypt 密码编码器（注入自 MyBatisConfig） */
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    /**
     * 用户登录
     * 1. 查询用户 2. 校验密码 3. 生成Token 4. 写入Redis
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginDTO loginDTO) {
        log.info("[登录] 用户名：{}", loginDTO.getUsername());

        // 1. 根据用户名查询用户
        SysUser user = sysUserMapper.selectByUsername(loginDTO.getUsername());
        if (user == null) {
            throw new BusinessException(MessageConstants.LOGIN_FAIL);
        }

        // 2. 校验账号状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new BusinessException(MessageConstants.ACCOUNT_DISABLED);
        }

        // 3. 校验密码（BCrypt 比对）
        if (!bCryptPasswordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(MessageConstants.LOGIN_FAIL);
        }

        // 4. 生成 JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 5. 将 Token 写入 Redis，过期时间24小时
        String redisKey = RedisKeyConstants.AUTH_TOKEN + user.getId();
        redisUtil.set(redisKey, token, SystemConstants.TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // 6. 更新最后登录时间（忽略失败）
        sysUserMapper.updateLastLoginAt(user.getId());

        // 7. 构建响应 VO
        UserInfoVO userInfoVO = new UserInfoVO();
        userInfoVO.setId(user.getId());
        userInfoVO.setUsername(user.getUsername());
        userInfoVO.setRealName(user.getRealName());
        userInfoVO.setRole(user.getRole());

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserInfo(userInfoVO);

        log.info("[登录成功] 用户ID={}，用户名={}", user.getId(), user.getUsername());
        return loginVO;
    }

    /**
     * 用户登出：删除 Redis 中的 Token
     */
    @Override
    public void logout(Long userId) {
        String redisKey = RedisKeyConstants.AUTH_TOKEN + userId;
        redisUtil.delete(redisKey);
        log.info("[登出] 用户ID={}，Token已清除", userId);
    }
}
