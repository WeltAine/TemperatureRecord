package filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 登录拦截器：未登录用户无法访问体温管理相关页面
 */
@WebFilter(urlPatterns = {"/temperature", "/addTempController", "/updateTempController"})
public class LoginFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // 排除登录相关路径
        String requestURI = request.getRequestURI();
        if (requestURI.contains("login.jsp") || requestURI.contains("loginController") || requestURI.contains("logoutController")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 检查session是否有登录标记
        HttpSession session = request.getSession();
        String loginAdmin = (String) session.getAttribute("loginAdmin");
        if (loginAdmin == null || loginAdmin.isEmpty()) {
            // 未登录：跳转到登录页
            response.sendRedirect("login.jsp");
            return;
        }

        // 已登录：放行
        filterChain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}