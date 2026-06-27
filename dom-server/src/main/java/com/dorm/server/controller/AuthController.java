package com.dorm.server.controller;

import com.dorm.server.constant.MessageConstants;
import com.dorm.server.entity.common.Result;
import com.dorm.server.entity.dto.LoginDTO;
import com.dorm.server.entity.vo.LoginVO;
import com.dorm.server.service.AuthService;
import com.dorm.server.service.LogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证接口 Controller
 *
 * @author dorm-server
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final LogService logService;

    /**
     * 用户登录
     * POST /api/v1/auth/login
     *
     * @param loginDTO 登录参数（username, password）
     * @param request  HTTP 请求（获取IP地址）
     * @return 登录响应（token + userInfo）
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Validated LoginDTO loginDTO,
                                 HttpServletRequest request) {
        log.info("[API] POST /api/v1/auth/login, username={}", loginDTO.getUsername());
        LoginVO loginVO = authService.login(loginDTO);

        // ログイン成功後、操作ログを非同期で記録
        String ip = request.getRemoteAddr();
        String userId = loginVO.getUserInfo() != null
                ? String.valueOf(loginVO.getUserInfo().getId()) : null;
        logService.asyncLog(loginDTO.getUsername(), "login", "user", userId, "ログイン", ip);

        return Result.success(MessageConstants.LOGIN_SUCCESS, loginVO);
    }

    /**
     * 用户登出
     * POST /api/v1/auth/logout
     *
     * @param request HTTP 请求（从属性中获取 userId 及 IP）
     * @return 登出结果
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        String username = (String) request.getAttribute("username");
        String ip = request.getRemoteAddr();
        log.info("[API] POST /api/v1/auth/logout, userId={}", userId);

        authService.logout(userId);

        // ログアウト後、操作ログを非同期で記録
        logService.asyncLog(username, "logout", "user",
                userId != null ? String.valueOf(userId) : null, "ログアウト", ip);

        return Result.success(MessageConstants.LOGOUT_SUCCESS, null);
    }
}
