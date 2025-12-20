package service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.ServletContext;

/**
 * 管理员登录验证服务（仅验证用户名密码，无增删改查）
 */
public class AdminService {
    private static final String LOG_FILE_PATH = "O:\\JavaProgram\\Log\\temp_log.txt";
    // private static final String DB_URL = "jdbc:sqlite:O:\\JavaProgram\\TemperatureRecord\\db\\user_temperature.db";
    private String DB_URL;

    // 初始化数据库路径（由控制器调用，传递ServletContext）
    public void initDBPath(ServletContext servletContext) {
        // 获取WEB-INF的真实部署路径（适配任意电脑的Tomcat）
        String webInfPath = servletContext.getRealPath("/WEB-INF");
        // 拼接数据库文件路径：WEB-INF/user_temperature.db
        File dbFile = new File(webInfPath, "user_temperature.db");
        // 生成SQLite的JDBC URL
        this.DB_URL = "jdbc:sqlite:" + dbFile.getAbsolutePath();
        // 日志记录路径（方便排查）
    }


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
            return true;
        } catch (ClassNotFoundException e) {
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
        try (Connection conn = DriverManager.getConnection(this.DB_URL);
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