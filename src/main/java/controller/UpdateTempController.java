package controller;

import model.User;
import service.TemperatureDBService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@WebServlet("/updateTempController")
public class UpdateTempController extends HttpServlet {
    private TemperatureDBService tempService = new TemperatureDBService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 编码处理
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 2. 接收参数
        String name = request.getParameter("name").trim();
        int year = Integer.parseInt(request.getParameter("year"));
        int month = Integer.parseInt(request.getParameter("month"));
        int day = Integer.parseInt(request.getParameter("day"));
        String tempInput = request.getParameter("temperature").trim();
        
        double temperature = tempInput.isEmpty() ? -1 : Double.parseDouble(tempInput);
        String yearMonth = year + "-" + String.format("%02d", month);
        int dayIndex = day - 1;

        // 3. 查询用户（核心目的：读取原有体温数组，仅改目标日期）
        User existUser = tempService.getUserByYearMonthAndName(yearMonth, name);
        boolean success = false;
        if (existUser != null) {
            double[] tempArr = existUser.getTemperature();
            // 扩容数组（避免日期超过长度）
            if (dayIndex >= tempArr.length) {
                tempArr = Arrays.copyOf(tempArr, day);
            }
            // 仅修改目标日期的体温，其他日期保留原有值
            tempArr[dayIndex] = temperature;
            existUser.setTemperature(tempArr);
            // 调用修改
            success = tempService.updateTemperature(existUser, yearMonth);
        }

        // 4. 提示&跳转
        if (!success) {
            request.setAttribute("error", "修改失败：未找到记录或数据库异常");
        }
        response.sendRedirect("temperature?year=" + year + "&month=" + month);
    }
}