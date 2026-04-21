package com.example.lotterysystem.common.interceptor;

import com.example.lotterysystem.common.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    JWTUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("user_token");
        log.info("获取token：{}", token);
        log.info("获取路径：{}", request.getRequestURI());

        Claims claims = jwtUtil.parseJWT(token);
        if(claims == null) {
            log.error("解析JWT令牌失败！");
            response.setStatus(401);
            return false;
        }

        log.info("解析JWT令牌成功！放行");
        return true;
    }
}
