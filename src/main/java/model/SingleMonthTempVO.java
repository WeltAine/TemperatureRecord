package model;

import java.util.List;


/**
 * 单月份体温数据实体
 */
public class SingleMonthTempVO {
    private String yearMonth; // 年月（yyyy-mm）
    private List<User> users; // 该年月的用户列表

    // 空参/全参构造 + getter/setter
    public SingleMonthTempVO() {}
    public SingleMonthTempVO(String yearMonth, List<User> users) {
        this.yearMonth = yearMonth;
        this.users = users;
    }

    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }
}