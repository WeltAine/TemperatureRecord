package service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 管理员登录验证服务（仅验证用户名密码，无增删改查）
 */
public class AdminService {
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


    // 验证管理员用户名密码是否正确
    public boolean validateAdmin(String username, String password) {
        // 加载驱动
        if (!loadDriver()) {
            return false;
        }

        // 查询admin表验证
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM admin WHERE username = ? AND password = ?"
            )) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            // 存在匹配记录则返回true
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}