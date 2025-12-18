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

@WebServlet("/addTempController")
public class AddTempController extends HttpServlet {
    private TemperatureDBService tempService = new TemperatureDBService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 编码处理
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 2. 接收参数
        String name = request.getParameter("name").trim();
        String gender = request.getParameter("gender").trim();
        int age = Integer.parseInt(request.getParameter("age"));
        String address = request.getParameter("address").trim();
        int year = Integer.parseInt(request.getParameter("year"));
        int month = Integer.parseInt(request.getParameter("month"));
        int day = Integer.parseInt(request.getParameter("day"));
        double temperature = Double.parseDouble(request.getParameter("temperature"));
        
        String yearMonth = year + "-" + String.format("%02d", month);
        int dayIndex = day - 1;

        // 3. 查询用户是否存在（目的：保留原有体温，仅改目标日期）
        User existUser = tempService.getUserByYearMonthAndName(yearMonth, name);
        boolean success;

        if (existUser != null) {
            // 3.1 用户存在：仅更新目标日期的体温，保留其他日期
            double[] tempArr = existUser.getTemperature();
            if (dayIndex >= tempArr.length) {
                tempArr = Arrays.copyOf(tempArr, day); // 扩容数组
            }
            tempArr[dayIndex] = temperature; // 仅改目标日期
            existUser.setTemperature(tempArr);
            // 复用用户原有信息（避免gender/age被覆盖）
            existUser.setGender(existUser.getGender() != null ? existUser.getGender() : gender);
            existUser.setAge(existUser.getAge() > 0 ? existUser.getAge() : age);
            existUser.setAddress(existUser.getAddress() != null ? existUser.getAddress() : address);
            success = tempService.updateTemperature(existUser, yearMonth);
        } else {
            // 3.2 用户不存在：新增，初始化31天数组，仅设目标日期
            User newUser = new User();
            newUser.setName(name);
            newUser.setGender(gender);
            newUser.setAge(age);
            newUser.setAddress(address);
            double[] tempArr = new double[31];
            Arrays.fill(tempArr, -1);
            tempArr[dayIndex] = temperature;
            newUser.setTemperature(tempArr);
            success = tempService.addTemperature(newUser, yearMonth);
        }

        // 4. 提示&跳转
        if (!success) {
            request.setAttribute("error", existUser != null ? "修改失败" : "添加失败");
        }
        response.sendRedirect("temperature?year=" + year + "&month=" + month);
    }
}