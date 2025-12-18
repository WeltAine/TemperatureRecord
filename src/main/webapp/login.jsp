<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>管理员登录</title>
    <style>
        .login-container {
            width: 400px;
            margin: 100px auto;
            padding: 20px;
            border: 1px solid #ddd;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0,0,0,0.1);
        }
        .login-title {
            text-align: center;
            margin-bottom: 20px;
            color: #333;
        }
        .form-item {
            margin: 15px 0;
        }
        .form-item label {
            display: inline-block;
            width: 80px;
            text-align: right;
            margin-right: 10px;
        }
        .form-item input {
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
            width: 250px;
        }
        .login-btn {
            width: 100%;
            padding: 10px;
            background-color: #4CAF50;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 16px;
            margin-top: 10px;
        }
        .login-btn:hover {
            background-color: #45a049;
        }
        .error-tip {
            color: #f44336;
            text-align: center;
            margin: 10px 0;
        }
    </style>
</head>
<body>
    <div class="login-container">
        <h2 class="login-title">体温管理系统 - 管理员登录</h2>
        <!-- 错误提示 -->
        <% if (request.getAttribute("errorMsg") != null) { %>
            <div class="error-tip"><%= request.getAttribute("errorMsg") %></div>
        <% } %>
        <!-- 登录表单 -->
        <form action="loginController" method="post">
            <div class="form-item">
                <label>用户名：</label>
                <input type="text" name="username" required placeholder="请输入管理员用户名">
            </div>
            <div class="form-item">
                <label>密码：</label>
                <input type="password" name="password" required placeholder="请输入管理员密码">
            </div>
            <button type="submit" class="login-btn">登录</button>
        </form>
    </div>
</body>
</html>