<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.User" %>
<html>
<head>
    <title>${currentYear}年${currentMonth}月体温记录</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        /* 按钮样式 */
        .month-btn {
            padding: 8px 16px;
            margin: 0 10px;
            text-decoration: none;
            background-color: #4CAF50;
            color: white;
            border-radius: 4px;
            border: none;
            cursor: pointer;
        }
        .month-btn:hover {
            background-color: #45a049;
        }
        .title-container {
            text-align: center;
            margin: 20px 0;
        }
        .no-data {
            text-align: center;
            color: #ff4444;
            font-size: 16px;
            margin: 10px 0;
        }
    </style>
</head>
<body>
    <!-- 动态标题 + 上下月按钮 -->
    <div class="title-container">
        <button class="month-btn" onclick="location.href='temperature?year=${prevYear}&month=${prevMonth}'">上一月</button>
        <h1>${currentYear}年${currentMonth}月体温记录表（仅展示当月测过的用户）</h1>
        <button class="month-btn" onclick="location.href='temperature?year=${nextYear}&month=${nextMonth}'">下一月</button>
    </div>

    <!-- 无数据提示 -->
    <% if (request.getAttribute("noDataTip") != null) { %>
        <div class="no-data"><%= request.getAttribute("noDataTip") %></div>
    <% } %>

    <table>
        <thead>
            <tr>
                <th rowspan="2" style="width: 20%;">用户信息</th>
                <th colspan="31">日期</th>
            </tr>
            <tr>
                <%-- 生成1-31天表头 --%>
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
                } else if (request.getAttribute("noDataTip") == null) {
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