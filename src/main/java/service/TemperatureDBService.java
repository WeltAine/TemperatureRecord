package service;

import model.User;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TemperatureDBService {

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



    // 数据库文件路径（注意：路径为项目根目录下的db/temperature.db，需确保db文件夹已创建）
    private static final String DB_URL = "jdbc:sqlite:O:\\JavaProgram\\TemperatureRecord\\db\\user_temperature.db";

    // 从数据库读取指定年月的体温数据（和JSON服务的方法名/返回值一致，方便控制器切换）
    public List<User> getMonthlyTemperature(String year, String month) {

        List<User> userList = new ArrayList<>();

        // 新增：手动加载SQLite驱动类（核心！解决No suitable driver问题）
        try {
            Class.forName("org.sqlite.JDBC");
            writeLog("SQLite驱动加载成功！");
        } catch (ClassNotFoundException e) {
            writeLog("SQLite驱动加载失败：" + e.getMessage());
            return userList; // 驱动加载失败，直接返回空列表
        }

        //打印当前工作目录到日志文件
        String workDir = System.getProperty("user.dir");
        writeLog("当前工作目录：" + workDir);


        // 拼接年月格式（如2025-12）
        String yearMonth = year + "-" + String.format("%02d", Integer.parseInt(month));
        // String yearMonth = year + "-" + month;
        writeLog("查询的年月：" + yearMonth);

        // JDBC连接SQLite（try-with-resources自动关闭连接，避免内存泄漏）
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT name, gender, age, address, temperature FROM user_temperature WHERE year_month = ?"
             )) {

            writeLog("数据库连接成功！");
            // 设置查询参数
            pstmt.setString(1, yearMonth);
            ResultSet rs = pstmt.executeQuery();

            // 解析结果集为User对象
            int count = 0;
            while (rs.next()) {
                User user = new User();
                user.setName(rs.getString("name"));
                user.setGender(rs.getString("gender"));
                user.setAge(rs.getInt("age"));
                user.setAddress(rs.getString("address"));
                
                // 解析体温字符串为double数组（兼容原有逻辑）
                String tempStr = rs.getString("temperature");
                if (tempStr != null && !tempStr.isEmpty()) {
                    String[] tempArrStr = tempStr.split(",");
                    double[] tempArr = new double[tempArrStr.length];
                    for (int i = 0; i < tempArrStr.length; i++) {
                        tempArr[i] = Double.parseDouble(tempArrStr[i]);
                    }
                    user.setTemperature(tempArr);
                } else {
                    user.setTemperature(new double[31]); // 兜底：空体温数组
                }
                userList.add(user);
            }
            writeLog("查询到的记录数：" + count); // 写入结果数量

        } catch (SQLException e) {
            // 新增：把异常信息写入日志（关键！）
            writeLog("数据库连接/查询异常：" + e.getMessage());
            // 把完整的异常堆栈也写入日志（方便定位）
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            writeLog("异常堆栈：" + sw.toString());

            e.printStackTrace(); // 异常打印，方便调试
        }
        return userList;
    }
}