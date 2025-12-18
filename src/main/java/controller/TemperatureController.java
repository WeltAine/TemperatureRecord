package controller;

import service.TemperatureService;
import service.TemperatureDBService;
import model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/temperature")
public class TemperatureController extends HttpServlet {

    // 1. 定义日志文件路径（改成你的路径，比如桌面）
    // Windows桌面示例：C:\\Users\\你的用户名\\Desktop\\temp_log.txt
    // 注意：双反斜杠\\，替换“你的用户名”为实际名称（比如Administrator）
    private static final String LOG_FILE_PATH = "O:\\JavaProgram\\Log\\temp_log.txt";

    // 2. 新增：日志写入文件的工具方法
    private void writeLog(String content) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_PATH, true))) {
            // 追加写入，每行日志带时间
            writer.println("[" + new java.util.Date() + "] " + content);
        } catch (IOException e) {
            // 写入失败时，降级打印到控制台（防止程序报错）
            System.out.println("日志写入失败：" + e.getMessage());
        }
    }



    private TemperatureService tempService = new TemperatureService();//json服务
    private TemperatureDBService dbService = new TemperatureDBService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1. 获取当前年月参数（默认2025-12）
        String currentYearStr = request.getParameter("year");
        String currentMonthStr = request.getParameter("month");
        int currentYear = 2025;
        int currentMonth = 12;

        // 写入前端传递的参数到日志
        writeLog("前端传递的年参数：" + currentYearStr + "，月参数：" + currentMonthStr);



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


        // 写入解析后的参数
        writeLog("解析后的年：" + currentYear + "，月：" + currentMonth);

        // 3.获取2025年12月的用户列表
        // List<User> userList = tempService.getMonthlyTemperature(
        //     String.valueOf(currentYear),
        //     String.format("%02d", currentMonth)
        // );
        List<User> userList = dbService.getMonthlyTemperature(
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
            writeLog("查询结果为空，设置无数据提示");
        }else{
            writeLog("查询结果不为空，共" + userList.size() + "条记录");
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