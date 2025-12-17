package controller;

import service.TemperatureService;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/temperature")
public class TemperatureController extends HttpServlet {
    private TemperatureService tempService = new TemperatureService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        // 获取2025年12月的用户列表
        List<User> userList = tempService.getMonthlyTemperature("2025", "12");
        // 传递数据到JSP
        request.setAttribute("userList", userList);
        // 转发到首页
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}