package com.project.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SessionInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        String path = request.getRequestURI();

        // Allow access to login pages
        if (path.equals("/contractor/login") || path.equals("/employee/login")) {
            return true;
        }

        // Check if the user is trying to access protected contractor or employee routes
        if (path.startsWith("/contractor/")) {
            if (session == null || session.getAttribute("loggedInContractor") == null) {
                response.sendRedirect("/contractor/login?reason=intercept_null_contractor");
                return false;
            }
        } else if (path.startsWith("/employee/")) {
            if (session == null || session.getAttribute("loggedInEmployee") == null) {
                response.sendRedirect("/employee/login?error=unauthorized");
                return false;
            }
            
            if (!path.equals("/employee/change-password") && !path.equals("/employee/update-password")) {
                com.project.entity.Employee emp = (com.project.entity.Employee) session.getAttribute("loggedInEmployee");
                if (emp.getPasswordChanged() == null || !emp.getPasswordChanged()) {
                    response.sendRedirect("/employee/change-password");
                    return false;
                }
            }
        }

        return true;
    }
}
