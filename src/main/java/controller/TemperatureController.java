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

        // 1. 获取当前年月参数（默认2025-12）
        String currentYearStr = request.getParameter("year");
        String currentMonthStr = request.getParameter("month");
        int currentYear = 2025;
        int currentMonth = 12;

        // 2. 解析前端传递的年月参数
        if (currentYearStr != null && !currentYearStr.isEmpty()) {
            try {
                currentYear = Integer.parseInt(currentYearStr);
            } catch (NumberFormatException e) {
                currentYear = 2025; // 解析失败用默认
            }
        }
        if (currentMonthStr != null && !currentMonthStr.isEmpty()) {
            try {
                currentMonth = Integer.parseInt(currentMonthStr);
            } catch (NumberFormatException e) {
                currentMonth = 12; // 解析失败用默认
            }
        }


        // 获取2025年12月的用户列表
        List<User> userList = tempService.getMonthlyTemperature(
            String.valueOf(currentYear),
            String.format("%02d", currentMonth)
        );


        // 4. 计算上一月/下一月的年月（用于按钮参数）
        int prevYear = currentYear;
        int prevMonth = currentMonth - 1;
        if (prevMonth < 1) { // 1月的上一月是去年12月
            prevYear = currentYear - 1;
            prevMonth = 12;
        }

        int nextYear = currentYear;
        int nextMonth = currentMonth + 1;
        if (nextMonth > 12) { // 12月的下一月是明年1月
            nextYear = currentYear + 1;
            nextMonth = 1;
        }

        // 5. 无对应JSON文件（用户列表为空），则保持原年月（可选：提示无数据）
        if (userList.isEmpty()) {
            request.setAttribute("noDataTip", "暂无" + currentYear + "年" + currentMonth + "月的体温记录");
        }



        // 6. 传递参数到JSP
        request.setAttribute("userList", userList);
        request.setAttribute("currentYear", currentYear);
        request.setAttribute("currentMonth", currentMonth);
        request.setAttribute("prevYear", prevYear);
        request.setAttribute("prevMonth", prevMonth);
        request.setAttribute("nextYear", nextYear);
        request.setAttribute("nextMonth", nextMonth);

        // 转发到首页
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}