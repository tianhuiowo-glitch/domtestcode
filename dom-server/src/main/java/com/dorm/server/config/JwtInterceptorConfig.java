package com.dorm.server.config;

import com.dorm.server.util.JwtUtil;
import com.dorm.server.util.RedisUtil;
import com.dorm.server.constant.RedisKeyConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 拦截器配置类
 * 拦截所有 /api/v1/** 请求，校验 JWT token 合法性
 *
 * @author dorm-server
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtInterceptorConfig implements WebMvcConfigurer {

    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    /**
     * 注册 JWT 拦截器，放行登录/登出接口
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor())
                .addPathPatterns("/api/v1/**")
                .excludePathPatterns("/api/v1/auth/login");
    }

    /**
     * JWT 拦截器内部类
     */
    private class JwtInterceptor implements HandlerInterceptor {

        @Override
        public boolean preHandle(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler) throws Exception {
            // 获取 Authorization 请求头
            String authHeader = request.getHeader("Authorization");

            if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
                log.warn("[JWT拦截器] 请求缺少 Authorization 头，路径：{}", request.getRequestURI());
                writeUnauthorized(response, "未登录，请先登录");
                return false;
            }

            String token = authHeader.substring(7);

            // 校验 token 格式和签名
            if (!jwtUtil.validateToken(token)) {
                log.warn("[JWT拦截器] Token 无效，路径：{}", request.getRequestURI());
                writeUnauthorized(response, "Token 无效或已过期");
                return false;
            }

            // 从 token 中获取 userId
            Long userId = jwtUtil.getUserIdFromToken(token);
            if (userId == null) {
                log.warn("[JWT拦截器] Token 解析 userId 失败，路径：{}", request.getRequestURI());
                writeUnauthorized(response, "Token 解析失败");
                return false;
            }

            // 校验 Redis 中 token 是否存在（防止注销后重用）
            String redisKey = RedisKeyConstants.AUTH_TOKEN + userId;
            if (!Boolean.TRUE.equals(redisUtil.hasKey(redisKey))) {
                log.warn("[JWT拦截器] Redis 中 Token 已失效，userId={}，路径：{}", userId, request.getRequestURI());
                writeUnauthorized(response, "登录状态已过期，请重新登录");
                return false;
            }

            // 将 userId 和 username 写入请求属性，供 Controller 使用
            request.setAttribute("userId", userId);
            request.setAttribute("username", jwtUtil.getUsernameFromToken(token));
            return true;
        }

        /**
         * 返回 401 未授权响应
         */
        private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> result = new HashMap<>(4);
            result.put("code", 401);
            result.put("msg", message);
            result.put("data", null);
            PrintWriter writer = response.getWriter();
            writer.write(objectMapper.writeValueAsString(result));
            writer.flush();
        }
    }
}
