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
        /* 新增：筛选表单样式（匹配原有风格） */
        .filter-form-container {
            width: 80%;
            margin: 0 auto 20px; /* 居中+底部间距 */
            padding: 15px;
            border: 1px solid #e0e0e0;
            border-radius: 6px;
            background-color: #f9f9f9;
        }
        .filter-form-container .form-item {
            display: inline-block; /* 行内块，水平排列 */
            margin-right: 15px;
            margin-bottom: 8px;
            vertical-align: middle;
        }
        .filter-form-container label {
            margin-right: 5px;
            color: #333;
            font-size: 14px;
        }
        .filter-form-container input, .filter-form-container select {
            padding: 6px 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
            width: 120px;
        }
        .filter-form-container .submit-btn {
            background-color: #4CAF50;
            color: white;
            border: none;
            padding: 8px 20px;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
        }
        .filter-form-container .submit-btn:hover {
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
    <!-- 原有：动态标题 + 上下月按钮（水平布局） -->
    <div class="title-container">
        <button class="month-btn" onclick="location.href='temperature?year=${prevYear}&month=${prevMonth}'">上一月</button>
        <h1>${currentYear}年${currentMonth}月体温记录表（仅展示当月测过的用户）</h1>
        <button class="month-btn" onclick="location.href='temperature?year=${nextYear}&month=${nextMonth}'">下一月</button>
    </div>

    <!-- 新增：筛选表单（放在标题后、无数据提示前，样式匹配原有风格） -->
    <div class="filter-form-container">
        <form action="temperature" method="get">
            <!-- 隐藏域：传递当前年月，保证筛选范围仅限当前显示月份 -->
            <input type="hidden" name="year" value="${currentYear}">
            <input type="hidden" name="month" value="${currentMonth}">
            
            <!-- 链2：姓名筛选 -->
            <div class="form-item">
                <label>姓名：</label>
                <input type="text" name="name" placeholder="精确匹配" />
            </div>
            
            <!-- 链1：性别筛选 -->
            <div class="form-item">
                <label>性别：</label>
                <select name="gender">
                    <option value="">全部</option>
                    <option value="男">男</option>
                    <option value="女">女</option>
                </select>
            </div>
            
            <!-- 链1：年龄范围 -->
            <div class="form-item">
                <label>年龄：</label>
                <input type="number" name="minAge" placeholder="最小" min="0" style="width: 80px;" />
                <label>-</label>
                <input type="number" name="maxAge" placeholder="最大" min="0" style="width: 80px;" />
            </div>
            
            <!-- 链1：地区模糊查询 -->
            <div class="form-item">
                <label>地区：</label>
                <input type="text" name="areaKeyword" placeholder="模糊匹配" />
            </div>
            
            <!-- 统一：日期段筛选 -->
            <div class="form-item">
                <label>日期段：</label>
                <input type="number" name="startDay" placeholder="开始日" min="1" max="31" style="width: 80px;" />
                <label>-</label>
                <input type="number" name="endDay" placeholder="结束日" min="1" max="31" style="width: 80px;" />
            </div>
            
            <!-- 提交按钮（匹配原有month-btn样式） -->
            <div class="form-item">
                <button type="submit" class="submit-btn">查询</button>
            </div>
        </form>
    </div>

    <!-- 原有：无数据提示 -->
    <% if (request.getAttribute("noDataTip") != null) { %>
        <div class="no-data"><%= request.getAttribute("noDataTip") %></div>
    <% } %>

    <!-- 原有：表格（仅修改体温渲染部分） -->
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
                        int age = user.getAge();
                        // 拼接弹窗内容，\\n在JS中解析为\n（换行）
                        String detailInfo = "姓名：" + name + "\\n性别：" + gender + "\\n年龄：" + age + "\\n住址：" + address;
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
                        <!-- ########### 核心修改 ########### -->
                        <!-- 判断：如果temp=-1则显示空，否则显示体温值 -->
                        <td><%= (temp == -1) ? "" : temp %></td>
                        <!-- ########### 核心修改结束 ########### -->
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