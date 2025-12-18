<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="model.User" %>
<html>
<head>
    <title>${currentYear}年${currentMonth}月体温记录</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        /* 核心：标题+按钮水平布局 */
        .title-container {
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 20px 0;
            gap: 20px;
        }

        /* 按钮通用样式 */
        .month-btn, .add-btn {
            padding: 8px 16px;
            text-decoration: none;
            background-color: #4CAF50;
            color: white;
            border-radius: 4px;
            border: none;
            cursor: pointer;
            font-size: 14px;
        }

        .month-btn:hover, .add-btn:hover {
            background-color: #45a049;
        }

        /* 筛选表单样式 */
        .filter-form-container {
            width: 80%;
            margin: 0 auto 20px;
            padding: 15px;
            border: 1px solid #e0e0e0;
            border-radius: 6px;
            background-color: #f9f9f9;
        }

        .filter-form-container .form-item {
            display: inline-block;
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

        /* 标题样式 */
        .title-container h1 {
            margin: 0;
            font-size: 24px;
            color: #333;
        }

        /* 无数据提示 */
        .no-data {
            text-align: center;
            color: #ff4444;
            font-size: 16px;
            margin: 10px 0;
        }

        /* 姓名列样式 */
        .name-cell {
            width: 120px;
            text-align: center;
            font-weight: 500;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        /* 姓名点击样式 */
        .user-name {
            cursor: pointer;
            color: #0066cc;
            text-decoration: underline;
            transition: color 0.2s;
        }

        .user-name:hover {
            color: #004999;
        }

        /* 弹窗样式 */
        .modal {
            display: none;
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5);
            z-index: 9999;
        }

        .modal-content {
            background-color: white;
            margin: 15% auto;
            padding: 20px;
            border-radius: 8px;
            width: 400px;
            box-shadow: 0 0 10px rgba(0,0,0,0.2);
        }

        .modal-close {
            float: right;
            font-size: 20px;
            cursor: pointer;
            color: #666;
        }

        .modal-close:hover {
            color: #000;
        }

        .modal-form-item {
            margin: 10px 0;
        }

        .modal-form-item label {
            display: inline-block;
            width: 80px;
            text-align: right;
            margin-right: 10px;
        }

        .modal-form-item input {
            padding: 6px;
            border: 1px solid #ddd;
            border-radius: 4px;
            width: 200px;
        }

        .modal-submit {
            background-color: #4CAF50;
            color: white;
            border: none;
            padding: 8px 20px;
            border-radius: 4px;
            cursor: pointer;
            margin-top: 10px;
            margin-left: 90px;
        }

        .modal-submit:hover {
            background-color: #45a049;
        }

        /* 体温单元格样式（可点击） */
        .temp-cell {
            cursor: pointer;
            transition: background-color 0.2s;
            text-align: center;
        }

        .temp-cell:hover {
            background-color: #f0f8ff;
        }

        /* 表格基础样式（补充） */
        table {
            width: 95%;
            margin: 0 auto;
            border-collapse: collapse;
        }

        th, td {
            border: 1px solid #ddd;
            padding: 4px 2px;
        }

        th {
            background-color: #f2f2f2;
            font-size: 12px;
        }
    </style>
</head>
<body>
    <!-- 标题栏：上下月按钮 + 标题 + 添加按钮 + 退出登录 -->
    <div class="title-container">
        <button class="month-btn" onclick="location.href='temperature?year=${prevYear}&month=${prevMonth}'">上一月</button>
        <h1>${currentYear}年${currentMonth}月体温记录表（仅展示当月测过的用户）</h1>
        <button class="month-btn" onclick="location.href='temperature?year=${nextYear}&month=${nextMonth}'">下一月</button>
        <button class="add-btn" onclick="openAddModal()">添加体温记录</button>
        <!-- 新增：退出登录按钮 -->
        <button class="month-btn" style="background-color: #f44336;" onclick="location.href='logoutController'">退出登录</button>
    </div>
    <!-- 添加数据弹窗 -->
    <div id="addModal" class="modal">
        <div class="modal-content">
            <span class="modal-close" onclick="closeAddModal()">&times;</span>
            <h3>添加${currentYear}年${currentMonth}月体温记录</h3>
            <form id="addForm" method="post" action="addTempController">
                <!-- 隐藏域：传递当前年月（月份固定） -->
                <input type="hidden" name="year" value="${currentYear}">
                <input type="hidden" name="month" value="${currentMonth}">
                
                <div class="modal-form-item">
                    <label>姓名：</label>
                    <input type="text" name="name" required placeholder="请输入姓名">
                </div>
                <div class="modal-form-item">
                    <label>性别：</label>
                    <input type="text" name="gender" required placeholder="男/女">
                </div>
                <div class="modal-form-item">
                    <label>年龄：</label>
                    <input type="number" name="age" required min="0" placeholder="请输入年龄">
                </div>
                <div class="modal-form-item">
                    <label>地址：</label>
                    <input type="text" name="address" placeholder="请输入住址">
                </div>
                <div class="modal-form-item">
                    <label>日期：</label>
                    <%
                        // 获取当天日期作为默认值（格式：dd）
                        SimpleDateFormat sdf = new SimpleDateFormat("dd");
                        String today = sdf.format(new Date());
                    %>
                    <input type="number" name="day" id="addDay" required min="1" max="31" value="<%= today %>" placeholder="1-31">
                </div>
                <div class="modal-form-item">
                    <label>体温：</label>
                    <input type="number" name="temperature" step="0.1" required placeholder="如：36.5">
                </div>
                <button type="submit" class="modal-submit">提交</button>
            </form>
        </div>
    </div>

    <!-- 修改温度弹窗 -->
    <div id="updateModal" class="modal">
        <div class="modal-content">
            <span class="modal-close" onclick="closeUpdateModal()">&times;</span>
            <h3>修改体温</h3>
            <form id="updateForm" method="post" action="updateTempController">
                <!-- 隐藏域：传递用户姓名、年月、日期 -->
                <input type="hidden" name="name" id="updateName">
                <input type="hidden" name="year" value="${currentYear}">
                <input type="hidden" name="month" value="${currentMonth}">
                <input type="hidden" name="day" id="updateDay">
                
                <div class="modal-form-item">
                    <label>体温：</label>
                    <input type="number" name="temperature" id="updateTemp" step="0.1" placeholder="如：36.5（空则清空）">
                </div>
                <button type="submit" class="modal-submit">保存</button>
            </form>
        </div>
    </div>

    <!-- 筛选表单 -->
    <div class="filter-form-container">
        <form action="temperature" method="get">
            <input type="hidden" name="year" value="${currentYear}">
            <input type="hidden" name="month" value="${currentMonth}">
            <div class="form-item">
                <label>姓名：</label>
                <input type="text" name="name" placeholder="精确匹配" />
            </div>
            <div class="form-item">
                <label>性别：</label>
                <select name="gender">
                    <option value="">全部</option>
                    <option value="男">男</option>
                    <option value="女">女</option>
                </select>
            </div>
            <div class="form-item">
                <label>年龄：</label>
                <input type="number" name="minAge" placeholder="最小" min="0" style="width: 80px;" />
                <label>-</label>
                <input type="number" name="maxAge" placeholder="最大" min="0" style="width: 80px;" />
            </div>
            <div class="form-item">
                <label>地区：</label>
                <input type="text" name="areaKeyword" placeholder="模糊匹配" />
            </div>
            <div class="form-item">
                <label>日期段：</label>
                <input type="number" name="startDay" placeholder="开始日" min="1" max="31" style="width: 80px;" />
                <label>-</label>
                <input type="number" name="endDay" placeholder="结束日" min="1" max="31" style="width: 80px;" />
            </div>
            <button type="submit" class="submit-btn">查询</button>
        </form>
    </div>

    <!-- 无数据提示 -->
    <% if (request.getAttribute("noDataTip") != null) { %>
        <div class="no-data"><%= request.getAttribute("noDataTip") %></div>
    <% } %>

    <!-- 体温表格 -->
    <table>
        <thead>
            <tr>
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
                        
                        // 转义单引号，避免JS弹窗语法错误
                        String name = user.getName() == null ? "" : user.getName().replace("'", "\\'");
                        String gender = user.getGender() == null ? "" : user.getGender().replace("'", "\\'");
                        String address = user.getAddress() == null ? "" : user.getAddress().replace("'", "\\'");
                        int age = user.getAge();
                        String detailInfo = "姓名：" + name + "\\n性别：" + gender + "\\n年龄：" + age + "\\n住址：" + address;
            %>
                <tr>
                    <!-- 姓名单元格（点击显示详情弹窗） -->
                    <td class="name-cell">
                        <span class="user-name" onclick="alert('<%= detailInfo %>')">
                            <%= user.getName() %>
                        </span>
                    </td>
                    <%
                        // 渲染1-31天体温单元格
                        for (int i = 0; i < 31; i++) {
                            double temp = (tempArr != null && i < tempArr.length) ? tempArr[i] : -1;
                            String tempStr = (temp == -1) ? "" : String.valueOf(temp);
                            int day = i + 1; // 日期（1-31）
                    %>
                        <!-- 体温单元格（点击打开修改弹窗） -->
                        <td class="temp-cell" onclick="openUpdateModal('<%= user.getName() %>', <%= day %>, '<%= tempStr %>')">
                            <%= tempStr %>
                        </td>
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

    <!-- 弹窗控制JS -->
    <script>
        // ========== 添加弹窗控制 ==========
        function openAddModal() {
            document.getElementById("addModal").style.display = "block";
        }

        function closeAddModal() {
            document.getElementById("addModal").style.display = "none";
        }

        // ========== 修改弹窗控制 ==========
        function openUpdateModal(name, day, temp) {
            // 给隐藏域和输入框赋值
            document.getElementById("updateName").value = name;
            document.getElementById("updateDay").value = day;
            document.getElementById("updateTemp").value = temp === "" ? "" : temp;
            // 显示弹窗
            document.getElementById("updateModal").style.display = "block";
        }

        function closeUpdateModal() {
            document.getElementById("updateModal").style.display = "none";
        }

        // 点击弹窗外部关闭
        window.onclick = function(event) {
            var addModal = document.getElementById("addModal");
            var updateModal = document.getElementById("updateModal");
            if (event.target == addModal) {
                closeAddModal();
            }
            if (event.target == updateModal) {
                closeUpdateModal();
            }
        }

        // 表单提交前的简单验证（可选）
        document.getElementById("addForm")?.addEventListener("submit", function(e) {
            var temp = this.querySelector('input[name="temperature"]').value;
            if (temp && (parseFloat(temp) < 35 || parseFloat(temp) > 42)) {
                alert("体温值请输入35-42之间的数值！");
                e.preventDefault();
            }
        });

        document.getElementById("updateForm")?.addEventListener("submit", function(e) {
            var temp = this.querySelector('input[name="temperature"]').value;
            if (temp && (parseFloat(temp) < 35 || parseFloat(temp) > 42)) {
                alert("体温值请输入35-42之间的数值！");
                e.preventDefault();
            }
        });
    </script>
</body>
</html>