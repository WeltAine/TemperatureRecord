package controller;

import service.AdminService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 管理员登录控制器
 */
@WebServlet("/loginController")
public class LoginController extends HttpServlet {
    private AdminService adminService = new AdminService();

    @Override
    public void init() throws ServletException {
        super.init();
        // 初始化数据库路径
        ServletContext servletContext = this.getServletContext();
        adminService.initDBPath(servletContext);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 编码处理
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 2. 获取登录参数
        String username = request.getParameter("username").trim();
        String password = request.getParameter("password").trim();

        // 3. 验证用户名密码
        boolean isValid = adminService.validateAdmin(username, password);
        if (isValid) {
            // 登录成功：存入session，跳转到体温管理页面
            HttpSession session = request.getSession();
            session.setAttribute("loginAdmin", username); // 标记已登录的管理员
            session.setMaxInactiveInterval(3600); // session有效期1小时
            response.sendRedirect("temperature?year=2025&month=12"); // 跳转到默认年月的体温页面
        } else {
            // 登录失败：返回登录页，提示错误
            request.setAttribute("errorMsg", "用户名或密码错误，请重新登录！");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }

    // 防止GET请求直接访问
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect("login.jsp");
    }
}