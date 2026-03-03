package com.book.manager.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * @Description 登录拦截器
 * @Date 2020/7/15 20:39
 * @Author by 尘心
 */
@Slf4j
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        HttpSession session = request.getSession();

        // TODO：这里可以根据你的项目逻辑判断是否登录
        // Object user = session.getAttribute("user");
        // if (user == null) {
        //     response.sendRedirect("/login");
        //     return false;
        // }

        return true;
    }
}

