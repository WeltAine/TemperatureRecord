package model;

import java.util.List;

// 查询结果封装：包含筛选后的用户列表+上下文
public class QueryResult {
    private List<User> userList; // 筛选后的用户列表
    private QueryContext context; // 筛选上下文（用于装饰器传递）

    // 构造器
    public QueryResult(List<User> userList, QueryContext context) {
        this.userList = userList;
        this.context = context;
    }

    // getter/setter
    public List<User> getUserList() { return userList; }
    public void setUserList(List<User> userList) { this.userList = userList; }
    public QueryContext getContext() { return context; }
    public void setContext(QueryContext context) { this.context = context; }
}