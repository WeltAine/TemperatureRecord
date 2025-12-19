package service;


import com.google.gson.Gson;
import model.MultiTempExportVO;
import model.SingleMonthTempVO;
import model.User;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    //导入导出功能
        // ========== 新增：查询所有月份的体温数据 ==========
    public Map<String, List<User>> getAllMonthTemperature() {
        Map<String, List<User>> allMonthData = new HashMap<>();
        if (!loadDriver()) {
            return allMonthData;
        }

        String sql = "SELECT name, gender, age, address, temperature, year_month FROM user_temperature";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                String yearMonth = rs.getString("year_month");
                // 初始化该年月的用户列表
                if (!allMonthData.containsKey(yearMonth)) {
                    allMonthData.put(yearMonth, new ArrayList<>());
                }

                // 封装 User 对象
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

                allMonthData.get(yearMonth).add(user);
            }
            writeLog("查询所有月份数据成功，共 " + allMonthData.size() + " 个年月");

        } catch (SQLException e) {
            writeLog("查询所有月份数据异常：" + e.getMessage());
            e.printStackTrace();
        }
        return allMonthData;
    }

    // ========== 修改：导出所有月份数据为 JSON ==========
    public String exportAllTempToJson() {
        // 1. 查询所有月份数据
        Map<String, List<User>> allMonthData = getAllMonthTemperature();
        if (allMonthData.isEmpty()) {
            writeLog("导出失败：暂无任何体温数据");
            return null;
        }

        // 2. 封装为多月份 VO
        List<SingleMonthTempVO> tempRecords = new ArrayList<>();
        for (Map.Entry<String, List<User>> entry : allMonthData.entrySet()) {
            tempRecords.add(new SingleMonthTempVO(entry.getKey(), entry.getValue()));
        }
        MultiTempExportVO exportVO = new MultiTempExportVO(tempRecords);

        // 3. Gson 转为格式化的 JSON 字符串
        Gson gson = new Gson();
        return gson.toJson(exportVO);
    }

    // ========== 修改：导入多月份 JSON 数据 ==========
    public boolean importMultiTempFromJson(File jsonFile) {
        if (!jsonFile.exists() || !jsonFile.isFile()) {
            writeLog("导入失败：文件不存在或不是合法文件");
            return false;
        }

        Gson gson = new Gson();
        MultiTempExportVO importVO = null;
        // 1. 解析多月份 JSON 文件
        try (FileReader reader = new FileReader(jsonFile)) {
            importVO = gson.fromJson(reader, MultiTempExportVO.class);
        } catch (IOException e) {
            writeLog("JSON 文件解析失败：" + e.getMessage());
            e.printStackTrace();
            return false;
        }

        if (importVO == null || importVO.getTempRecords() == null || importVO.getTempRecords().isEmpty()) {
            writeLog("导入失败：JSON 数据格式错误，无有效体温记录");
            return false;
        }

        // 2. 批量导入多月份数据到数据库
        if (!loadDriver()) {
            return false;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false); // 事务批量处理
            String insertSql = "INSERT OR REPLACE INTO user_temperature " +
                    "(name, gender, age, address, temperature, year_month) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            int totalImport = 0;
            // 循环每个年月导入
            for (SingleMonthTempVO singleVO : importVO.getTempRecords()) {
                String yearMonth = singleVO.getYearMonth();
                List<User> users = singleVO.getUsers();
                if (yearMonth == null || users == null || users.isEmpty()) {
                    continue;
                }

                // 导入该年月的所有用户
                for (User user : users) {
                    // 体温数组转字符串
                    double[] tempArr = user.getTemperature();
                    StringBuilder tempStr = new StringBuilder();
                    for (int i = 0; i < tempArr.length; i++) {
                        tempStr.append(tempArr[i]);
                        if (i != tempArr.length - 1) {
                            tempStr.append(",");
                        }
                    }

                    PreparedStatement pstmt = conn.prepareStatement(insertSql);
                    pstmt.setString(1, user.getName());
                    pstmt.setString(2, user.getGender());
                    pstmt.setInt(3, user.getAge());
                    pstmt.setString(4, user.getAddress());
                    pstmt.setString(5, tempStr.toString());
                    pstmt.setString(6, yearMonth);
                    pstmt.executeUpdate();
                    pstmt.close();
                    totalImport++;
                }
            }

            conn.commit();
            writeLog("多月份 JSON 导入成功：共导入 " + totalImport + " 条用户记录");
            return true;

        } catch (SQLException e) {
            writeLog("多月份导入数据库异常：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 修改后的方法签名（添加年、月参数）
    public boolean importMultiTempFromJson(File jsonFile, int year, int month) {
                // 1. 基础文件校验（复用原有逻辑）
        if (!jsonFile.exists() || !jsonFile.isFile()) {
            writeLog("导入失败：文件不存在或不是合法文件");
            return false;
        }

        // 2. 生成目标年月格式（和原有代码保持一致：yyyy-MM）
        String targetYearMonth = year + "-" + String.format("%02d", month);

        Gson gson = new Gson();
        MultiTempExportVO importVO = null;
        // 3. 解析JSON文件（复用原有逻辑）
        try (FileReader reader = new FileReader(jsonFile)) {
            importVO = gson.fromJson(reader, MultiTempExportVO.class);
        } catch (IOException e) {
            writeLog("JSON 文件解析失败：" + e.getMessage());
            e.printStackTrace();
            return false;
        }

        // 4. 校验解析结果（复用原有逻辑）
        if (importVO == null || importVO.getTempRecords() == null || importVO.getTempRecords().isEmpty()) {
            writeLog("导入失败：JSON 数据格式错误，无有效体温记录");
            return false;
        }

        // 5. 加载驱动（复用原有逻辑）
        if (!loadDriver()) {
            return false;
        }

        // 6. 批量导入指定年月的数据（仅筛选目标年月，其余复用）
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            conn.setAutoCommit(false);
            String insertSql = "INSERT OR REPLACE INTO user_temperature " +
                    "(name, gender, age, address, temperature, year_month) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

            int totalImport = 0;
            // 仅遍历匹配目标年月的记录
            for (SingleMonthTempVO singleVO : importVO.getTempRecords()) {
                String yearMonth = singleVO.getYearMonth();
                List<User> users = singleVO.getUsers();
                // 核心筛选：只处理和目标年月匹配的记录
                if (yearMonth == null || !yearMonth.equals(targetYearMonth) || users == null || users.isEmpty()) {
                    continue;
                }

                // 导入逻辑完全复用原有代码
                for (User user : users) {
                    double[] tempArr = user.getTemperature();
                    StringBuilder tempStr = new StringBuilder();
                    for (int i = 0; i < tempArr.length; i++) {
                        tempStr.append(tempArr[i]);
                        if (i != tempArr.length - 1) {
                            tempStr.append(",");
                        }
                    }

                    PreparedStatement pstmt = conn.prepareStatement(insertSql);
                    pstmt.setString(1, user.getName());
                    pstmt.setString(2, user.getGender());
                    pstmt.setInt(3, user.getAge());
                    pstmt.setString(4, user.getAddress());
                    pstmt.setString(5, tempStr.toString());
                    pstmt.setString(6, yearMonth);
                    pstmt.executeUpdate();
                    pstmt.close();
                    totalImport++;
                }
            }

            conn.commit();
            writeLog("指定年月 JSON 导入成功：[" + targetYearMonth + "] 共导入 " + totalImport + " 条用户记录");
            return true;

        } catch (SQLException e) {
            writeLog("指定年月导入数据库异常：" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}