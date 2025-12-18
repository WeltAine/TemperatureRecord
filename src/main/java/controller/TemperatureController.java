package controller;

import service.TemperatureService;
import service.query.QueryChainBuilder;
import service.TemperatureDBService;
import model.QueryContext;
import model.QueryResult;
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
            currentYear = Integer.parseInt(currentYearStr);
        }
        if (currentMonthStr != null && !currentMonthStr.isEmpty()) {
            currentMonth = Integer.parseInt(currentMonthStr);
        }

        // 写入解析后的参数
        writeLog("解析后的年：" + currentYear + "，月：" + currentMonth);

        // 拼接标准月份格式（yyyy-mm）
        String yearMonth = String.format("%d-%02d", currentYear, currentMonth);


                // ------------------- 2. 解析筛选参数（统一用startDay/endDay） -------------------
        // 数据库层筛选参数
        String name = request.getParameter("name");     // 姓名（链2触发条件）
        String gender = request.getParameter("gender"); // 性别（链1用）
        // 内存层筛选参数
        String minAgeStr = request.getParameter("minAge");
        String maxAgeStr = request.getParameter("maxAge");
        String areaKeyword = request.getParameter("areaKeyword");
        String startDayStr = request.getParameter("startDay");
        String endDayStr = request.getParameter("endDay");

        // ------------------- 构建并校验上下文（强制月份） -------------------
        QueryContext context = new QueryContext();
        context.setYearMonth(yearMonth); // 强制设置前端显示的月份
        context.setName(name);
        context.setGender(gender);
        // 解析年龄
        if (minAgeStr != null && !minAgeStr.isEmpty()) {
            context.setMinAge(Integer.parseInt(minAgeStr));
        }
        if (maxAgeStr != null && !maxAgeStr.isEmpty()) {
            context.setMaxAge(Integer.parseInt(maxAgeStr));
        }
        context.setAreaKeyword(areaKeyword);
        // 解析日期段（统一用startDay/endDay，链1/链2都用这个）
        if (startDayStr != null && !startDayStr.isEmpty()) {
            context.setStartDay(Integer.parseInt(startDayStr));
        }
        if (endDayStr != null && !endDayStr.isEmpty()) {
            context.setEndDay(Integer.parseInt(endDayStr));
        }
        context.validate(); // 强制校验月份格式

        // ------------------- 提前选择查询链（核心优化：参数解析后立即选链） -------------------
        List<User> userList;
        if (name != null && !name.isEmpty()) {
            // 链2：姓名 → 日期段（仅姓名+月份查数据库，然后筛日期段）
            QueryResult result = QueryChainBuilder.buildChain2(context);
            userList = result.getUserList();
        } else {
            // 链1：性别 → 年龄 → 地区 → 日期段（仅性别+月份查数据库，然后筛年龄/地区/日期）
            QueryResult result = QueryChainBuilder.buildChain1(context);
            userList = result.getUserList();
        }


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