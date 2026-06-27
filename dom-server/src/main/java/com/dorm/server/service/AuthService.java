package com.dorm.server.service;

import com.dorm.server.entity.dto.LoginDTO;
import com.dorm.server.entity.vo.LoginVO;

/**
 * 认证业务接口
 *
 * @author dorm-server
 */
public interface AuthService {

    /**
     * 用户登录
     *
     * @param loginDTO 登录请求参数
     * @return 登录响应（含Token和用户信息）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户登出（清除Redis Token）
     *
     * @param userId 当前用户ID
     */
    void logout(Long userId);
}
