package controller;

// 补全所有必要的导入（解决StringWriter等编译错误）
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.io.File;

import service.TemperatureDBService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

/**
 * 导入控制器（带完整日志，排查405错误）
 */
@WebServlet("/importTempController")
@MultipartConfig(
        maxFileSize = 10 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024
)
public class ImportTempController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemperatureDBService tempService = new TemperatureDBService();
    // 日志路径和Service保持一致，统一查看
    private static final String LOG_FILE_PATH = "O:\\JavaProgram\\Log\\temp_log.txt";
    // 临时目录用绝对路径，避免路径问题
    private static final String TEMP_DIR = "O:\\JavaProgram\\TemperatureRecord\\src\\main\\webapp\\WEB-INF\\temp_upload";

    // ========== 日志工具方法 ==========
    private void writeLog(String content) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(LOG_FILE_PATH, true))) {
            writer.println("[" + new java.util.Date() + "] [ImportTempController] " + content);
        } catch (IOException e) {
            // 日志写入失败时，打印到控制台，不影响主逻辑
            System.out.println("[" + new java.util.Date() + "] [ImportTempController] 日志写入失败：" + e.getMessage());
        }
    }

    // ========== 2. 控制器初始化日志（确认是否加载） ==========
    @Override
    public void init() throws ServletException {
        super.init();
        writeLog("===== ImportTempController 初始化完成！控制器已被Tomcat加载 =====");
    }

    // ========== 3. 核心POST方法（全流程日志） ==========
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            // 尝试获取上传文件
            Part filePart = request.getPart("jsonFile");

            // 日志3：文件校验
            if (filePart == null || filePart.getSize() == 0) {
                request.setAttribute("errorMsg", "导入失败：未选择文件或文件为空");
                request.getRequestDispatcher("temperature?year=2025&month=12").forward(request, response);
                return;
            }

            // 创建临时目录和文件
            File tempDir = new File(TEMP_DIR);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }
            File jsonFile = new File(tempDir, "import_temp.json");

            // 写入上传文件到临时目录
            try (InputStream is = filePart.getInputStream()) {
                Files.copy(is, jsonFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            // 调用Service导入数据
            boolean success = tempService.importMultiTempFromJson(jsonFile);

            // 删除临时文件
            if (jsonFile.exists()) {
                jsonFile.delete();
            }

            // 返回结果
            String msg = success ? "导入成功（已更新修改后的月份数据）" : "导入失败（检查JSON格式）";
            // 用session存提示信息
            request.getSession().setAttribute(success ? "successMsg" : "errorMsg", msg);
            // 重定向到temperature页面（GET请求，不会报405）
            response.sendRedirect("temperature?year=2025&month=12");
        } catch (Exception e) {
            // 返回异常提示
            request.setAttribute("errorMsg", "导入失败：" + e.getMessage());
            request.getRequestDispatcher("temperature?year=2025&month=12").forward(request, response);
        }
    }

    // ========== 4. GET请求日志（避免误访问） ==========
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().write("此接口仅支持POST上传JSON文件（导出的格式）");
    }

    // ========== 辅助方法：获取上传文件名 ==========
    private String getFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        for (String cd : contentDisposition.split(";")) {
            if (cd.trim().startsWith("filename")) {
                return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
            }
        }
        return "未知文件名.json";
    }

    // ========== 辅助方法：获取异常堆栈字符串（修正编译错误） ==========
    private String getStackTrace(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}