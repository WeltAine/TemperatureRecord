package service.query;

import model.User;
import model.QueryResult;
import model.QueryContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// 基础查询类：仅负责「月份」的数据库查询，返回当前月份所有数据
public class BaseTemperatureQuery implements TemperatureQuery {
    // SQLite驱动类名
    private static final String DRIVER_CLASS = "org.sqlite.JDBC";
    // 数据库绝对路径（替换为你的实际路径）
    private static final String DB_URL = "jdbc:sqlite:O:\\JavaProgram\\TemperatureRecord\\db\\user_temperature.db";
    // 筛选上下文（仅用yearMonth）
    private QueryContext context;

    // 构造器：必须传入上下文，强制校验月份
    public BaseTemperatureQuery(QueryContext context) {
        context.validate(); // 强制校验月份格式
        this.context = context;
        // 手动加载SQLite驱动
        try {
            Class.forName(DRIVER_CLASS);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("SQLite驱动加载失败", e);
        }
    }

    @Override
    public QueryResult execute() {
        List<User> userList = new ArrayList<>();
        String yearMonth = context.getYearMonth();

        // 数据库层仅执行：查询当前月份的所有数据（无任何其他筛选）
        String sql = "SELECT name, gender, age, address, temperature FROM user_temperature WHERE year_month = ?";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // 仅设置月份参数
            pstmt.setString(1, yearMonth);
            ResultSet rs = pstmt.executeQuery();

            // 解析当前月份的所有数据
            while (rs.next()) {
                User user = new User();
                user.setName(rs.getString("name"));
                user.setGender(rs.getString("gender"));
                user.setAge(rs.getInt("age"));
                user.setAddress(rs.getString("address"));
                
                // 解析体温数组（逗号分隔，-1表示未填）
                String tempStr = rs.getString("temperature");
                if (tempStr != null && !tempStr.isEmpty()) {
                    String[] tempArrStr = tempStr.split(",");
                    double[] tempArr = new double[tempArrStr.length];
                    for (int i = 0; i < tempArrStr.length; i++) {
                        tempArr[i] = Double.parseDouble(tempArrStr[i]);
                    }
                    user.setTemperature(tempArr);
                } else {
                    user.setTemperature(new double[31]); // 兜底：31天空数组
                }
                userList.add(user);
            }

        } catch (SQLException e) {
            throw new RuntimeException("数据库查询月份失败（月份：" + yearMonth + "）", e);
        }

        // 返回当前月份的全量数据
        return new QueryResult(userList, context);
    }
}