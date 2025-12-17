<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="model.User" %>
<html>
<head>
    <title>${currentYear}年${currentMonth}月体温记录</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        /* 核心：水平布局容器 */
        .title-container {
            display: flex; /* 弹性布局，子元素水平排列 */
            align-items: center; /* 垂直居中 */
            justify-content: center; /* 整体水平居中 */
            margin: 20px 0;
            gap: 20px; /* 元素之间的间距 */
        }
        /* 按钮样式优化 */
        .month-btn {
            padding: 8px 16px;
            text-decoration: none;
            background-color: #4CAF50;
            color: white;
            border-radius: 4px;
            border: none;
            cursor: pointer;
            font-size: 14px;
        }
        .month-btn:hover {
            background-color: #45a049;
        }
        /* 标题样式微调 */
        .title-container h1 {
            margin: 0; /* 去掉默认margin */
            font-size: 24px;
            color: #333;
        }
        .no-data {
            text-align: center;
            color: #ff4444;
            font-size: 16px;
            margin: 10px 0;
        }
        /* 关键：调整姓名列样式 */
        .name-cell {
            width: 120px; /* 加宽到120px，确保4-5字姓名一行显示 */
            text-align: center; /* 姓名居中显示 */
            font-weight: 500; /* 加粗突出姓名 */
            white-space: nowrap; /* 核心：禁止文字自动换行 */
            overflow: hidden; /* 可选：超长姓名隐藏溢出部分 */
            text-overflow: ellipsis; /* 可选：超长姓名显示省略号（如“欧阳娜娜...”） */        
        }
        /* 点击样式：手型+蓝色+下划线，提示可点击 */
        .user-name {
            cursor: pointer;
            color: #0066cc; 
            text-decoration: underline;
            /* 点击时轻微变色，提升交互感 */
            transition: color 0.2s;
        }
        .user-name:hover {
            color: #004999;
        }
    </style>
</head>
<body>
    <!-- 动态标题 + 上下月按钮（水平布局） -->
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
                <!-- 调整表头：仅显示“姓名”，宽度100px -->
                <th rowspan="2" class="name-cell">姓名</th>
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
                        
                        // 核心：手动转义单引号，无需调用方法（彻底解决方法未定义问题）
                        String name = user.getName() == null ? "" : user.getName().replace("'", "\\'");
                        String gender = user.getGender() == null ? "" : user.getGender().replace("'", "\\'");
                        String address = user.getAddress() == null ? "" : user.getAddress().replace("'", "\\'");
                        // 拼接弹窗内容，\\n在JS中解析为\n（换行）
                        String detailInfo = "姓名：" + name + "\\n性别：" + gender + "\\n住址：" + address;
            %>
                <tr>
                    <!-- 点击姓名弹出弹窗，无语法错误 -->
                    <td class="name-cell">
                        <span class="user-name" onclick="alert('<%= detailInfo %>')">
                            <%= user.getName() %>
                        </span>
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