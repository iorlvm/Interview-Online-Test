package com.apxpert.weien.filter;

import com.apxpert.weien.entity.Customer;
import com.apxpert.weien.service.TokenService;
import com.apxpert.weien.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class TokenParsingInterceptor implements HandlerInterceptor {
    @Autowired
    private TokenService tokenService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 從請求頭中取出token
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            Customer customer = tokenService.validateToken(token);
            if (customer != null) {
                UserHolder.saveUser(customer);
                tokenService.flashLoginExpire(token);
            }
            return true;
        }

        // 沒有攜帶請求頭的情況, 嘗試從session中取出token
        HttpSession session = request.getSession(false);
        if (session != null) {
            String token = (String) session.getAttribute("token");
            Customer customer = tokenService.validateToken(token);
            if (customer != null) {
                UserHolder.saveUser(customer);
                tokenService.flashLoginExpire(token);
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 使用完畢的時候移除資料
        UserHolder.removeUser();
    }
}