package controller;

import service.TemperatureDBService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * 导出所有月份体温数据为 JSON 文件
 */
@WebServlet("/exportAllTempController")
public class ExportAllTempController extends HttpServlet {
    private TemperatureDBService tempService = new TemperatureDBService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 1. 调用 Service 生成所有数据的 JSON 字符串
        String jsonStr = tempService.exportAllTempToJson();
        if (jsonStr == null || jsonStr.isEmpty()) {
            request.setAttribute("error", "导出失败：暂无任何体温数据");
            request.getRequestDispatcher("temperature?year=2025&month=12").forward(request, response);
            return;
        }

        // 2. 设置响应头（文件名：体温记录_20251219.json，带时间戳）
        response.setContentType("application/json;charset=UTF-8");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String fileName = "体温记录_" + sdf.format(new Date()) + ".json";
        fileName = URLEncoder.encode(fileName, "UTF-8"); // 解决中文乱码
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

        // 3. 输出 JSON 到响应流
        try (OutputStream os = response.getOutputStream()) {
            os.write(jsonStr.getBytes("UTF-8"));
            os.flush();
        }
    }
}