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

    private static final String LOG_FILE_PATH = "O:\\JavaProgram\\Log\\temp_log.txt";
    private static final String DB_URL = "jdbc:sqlite:O:\\JavaProgram\\TemperatureRecord\\db\\user_temperature.db";

    // 日志写入工具方法
    private void writeLog(String content) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_PATH, true))) {
            writer.println("[" + new java.util.Date() + "] " + content);
        } catch (IOException e) {
            System.out.println("日志写入失败：" + e.getMessage());
        }
    }

    // 驱动加载工具方法（复用）
    private boolean loadDriver() {
        try {
            Class.forName("org.sqlite.JDBC");
            writeLog("SQLite驱动加载成功！");
            return true;
        } catch (ClassNotFoundException e) {
            writeLog("SQLite驱动加载失败：" + e.getMessage());
            return false;
        }
    }

    // 查询指定年月的体温数据（原有功能）
    public List<User> getMonthlyTemperature(String year, String month) {
        List<User> userList = new ArrayList<>();
        if (!loadDriver()) {
            return userList;
        }

        String workDir = System.getProperty("user.dir");
        writeLog("当前工作目录：" + workDir);

        String yearMonth = year + "-" + String.format("%02d", Integer.parseInt(month));
        writeLog("查询的年月：" + yearMonth);

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT name, gender, age, address, temperature FROM user_temperature WHERE year_month = ?"
             )) {

            writeLog("数据库连接成功！");
            pstmt.setString(1, yearMonth);
            ResultSet rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                User user = new User();
                user.setName(rs.getString("name"));
                user.setGender(rs.getString("gender"));
                user.setAge(rs.getInt("age"));
                user.setAddress(rs.getString("address"));
                
                String tempStr = rs.getString("temperature");
                if (tempStr != null && !tempStr.isEmpty()) {
                    String[] tempArrStr = tempStr.split(",");
                    double[] tempArr = new double[tempArrStr.length];
                    for (int i = 0; i < tempArrStr.length; i++) {
                        tempArr[i] = Double.parseDouble(tempArrStr[i]);
                    }
                    user.setTemperature(tempArr);
                } else {
                    user.setTemperature(new double[31]);
                }
                userList.add(user);
                count++;
            }
            writeLog("查询到的记录数：" + count);

        } catch (SQLException e) {
            writeLog("数据库查询异常：" + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            writeLog("异常堆栈：" + sw.toString());
            e.printStackTrace();
        }
        return userList;
    }

    // 新增：添加体温记录
    public boolean addTemperature(User user, String yearMonth) {
        if (!loadDriver()) {
            return false;
        }

        // 将体温数组转为字符串（如"36.5,36.6,36.7"）
        double[] temps = user.getTemperature();
        StringBuilder tempStr = new StringBuilder();
        for (int i = 0; i < temps.length; i++) {
            tempStr.append(temps[i]);
            if (i != temps.length - 1) {
                tempStr.append(",");
            }
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "INSERT INTO user_temperature (name, gender, age, address, temperature, year_month) " +
                 "VALUES (?, ?, ?, ?, ?, ?)"
             )) {

            pstmt.setString(1, user.getName());
            pstmt.setString(2, user.getGender());
            pstmt.setInt(3, user.getAge());
            pstmt.setString(4, user.getAddress());
            pstmt.setString(5, tempStr.toString());
            pstmt.setString(6, yearMonth);

            int rows = pstmt.executeUpdate();
            boolean success = rows > 0;
            writeLog("添加记录" + (success ? "成功" : "失败") + "，姓名：" + user.getName() + "，年月：" + yearMonth);
            return success;

        } catch (SQLException e) {
            writeLog("添加记录异常（姓名：" + user.getName() + "）：" + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            writeLog("异常堆栈：" + sw.toString());
            e.printStackTrace();
            return false;
        }
    }

    // 新增：修改体温记录（按姓名和年月定位唯一记录）
    public boolean updateTemperature(User user, String yearMonth) {
        if (!loadDriver()) {
            return false;
        }

        // 体温数组转字符串
        double[] temps = user.getTemperature();
        StringBuilder tempStr = new StringBuilder();
        for (int i = 0; i < temps.length; i++) {
            tempStr.append(temps[i]);
            if (i != temps.length - 1) {
                tempStr.append(",");
            }
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE user_temperature SET gender=?, age=?, address=?, temperature=? " +
                 "WHERE name=? AND year_month=?"
             )) {

            pstmt.setString(1, user.getGender());
            pstmt.setInt(2, user.getAge());
            pstmt.setString(3, user.getAddress());
            pstmt.setString(4, tempStr.toString());
            pstmt.setString(5, user.getName());
            pstmt.setString(6, yearMonth);

            int rows = pstmt.executeUpdate();
            boolean success = rows > 0;
            writeLog("修改记录" + (success ? "成功" : "失败") + "，姓名：" + user.getName() + "，年月：" + yearMonth);
            return success;

        } catch (SQLException e) {
            writeLog("修改记录异常（姓名：" + user.getName() + "）：" + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            writeLog("异常堆栈：" + sw.toString());
            e.printStackTrace();
            return false;
        }
    }

    // 新增：查询指定姓名+年月的单个用户（用于添加/修改前的校验）
    public User getUserByYearMonthAndName(String yearMonth, String name) {
        if (!loadDriver()) {
            return null;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL);
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT name, gender, age, address, temperature FROM user_temperature WHERE year_month = ? AND name = ?"
            )) {

            pstmt.setString(1, yearMonth);
            pstmt.setString(2, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setName(rs.getString("name"));
                user.setGender(rs.getString("gender"));
                user.setAge(rs.getInt("age"));
                user.setAddress(rs.getString("address"));
            
                // 解析体温数组
                String tempStr = rs.getString("temperature");
                if (tempStr != null && !tempStr.isEmpty()) {
                    String[] tempArrStr = tempStr.split(",");
                    double[] tempArr = new double[tempArrStr.length];
                    for (int i = 0; i < tempArrStr.length; i++) {
                        tempArr[i] = Double.parseDouble(tempArrStr[i]);
                    }
                    user.setTemperature(tempArr);
                } else {
                    user.setTemperature(new double[31]); // 兜底31天数组
                }
                return user;
            }

        } catch (SQLException e) {
            writeLog("查询单个用户异常（姓名：" + name + "，年月：" + yearMonth + "）：" + e.getMessage());
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            writeLog("异常堆栈：" + sw.toString());
            e.printStackTrace();
        }
        return null;
    }
}