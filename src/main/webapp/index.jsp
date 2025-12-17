<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.User" %>
<html>
<head>
    <title>2025年12月体温记录</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
    <h1>2025年12月体温记录表（仅展示当月测过的用户）</h1>
    <table>
        <thead>
            <tr>
                <th rowspan="2" style="width: 20%;">用户信息</th>
                <th colspan="31">日期</th>
            </tr>
            <tr>
                <% for (int i = 1; i <= 31; i++) { %>
                    <th><%= String.format("%02d", i) %>日</th>
                <% } %>
            </tr>
        </thead>
        <tbody>
            <%
                // 从request获取用户列表
                List<User> userList = (List<User>) request.getAttribute("userList");
                if (userList != null && !userList.isEmpty()) {
                    for (User user : userList) {
                        double[] tempArr = user.getTemperature();
            %>
                <tr>
                    <td class="user-info">
                        姓名：<%= user.getName() %><br>
                        性别：<%= user.getGender() %><br>
                        住址：<%= user.getAddress() %>
                    </td>
                    <%
                        for (int i = 0; i < 31; i++) {
                            // 兜底：防止tempArr为null
                            double temp = (tempArr != null && i < tempArr.length) ? tempArr[i] : -1;
                    %>
                        <td><%= temp %></td>
                    <% } %>
                </tr>
            <%
                    }
                } else {
            %>
                <tr>
                    <td colspan="32" style="text-align: center; color: #999;">
                        本月暂无用户提交体温记录
                    </td>
                </tr>
            <% } %>
        </tbody>
    </table>
</body>
</html>