package controller; // 必须和其他Controller在同一个包下

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 管理员退出登录控制器
 * 注解路径必须是 /logoutController，且无拼写错误
 */
@WebServlet("/logoutController") // 重点：路径前必须加 /，且名称和页面跳转一致
public class LogoutController extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 强制设置编码（避免中文乱码，可选但推荐）
        response.setContentType("text/html;charset=UTF-8");
        
        // 2. 获取session并清空
        HttpSession session = request.getSession(false); // false：不存在则不创建，性能更好
        if (session != null) {
            session.removeAttribute("loginAdmin");
            session.invalidate(); // 销毁session
        }

        // 3. 跳回登录页（同样添加上下文路径）
        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    // 覆盖doPost，防止POST请求访问出错
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}