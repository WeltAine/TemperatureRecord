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

    // ========== 1. 日志工具方法（和Service一致） ==========
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
        // 日志1：确认POST请求进入控制器
        writeLog("收到POST请求，开始处理导入");
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        try {
            // 日志2：尝试获取上传文件
            writeLog("开始获取上传的JSON文件（name=jsonFile）");
            Part filePart = request.getPart("jsonFile");

            // 日志3：文件校验
            if (filePart == null || filePart.getSize() == 0) {
                writeLog("文件校验失败：未选择文件或文件为空（size=" + (filePart == null ? "null" : filePart.getSize()) + "）");
                request.setAttribute("errorMsg", "导入失败：未选择文件或文件为空");
                request.getRequestDispatcher("temperature?year=2025&month=12").forward(request, response);
                return;
            }
            writeLog("文件获取成功：文件名=" + getFileName(filePart) + "，文件大小=" + filePart.getSize() + "字节");

            // 日志4：创建临时目录和文件
            writeLog("开始创建临时目录：" + TEMP_DIR);
            File tempDir = new File(TEMP_DIR);
            if (!tempDir.exists()) {
                tempDir.mkdirs();
                writeLog("临时目录不存在，已创建：" + TEMP_DIR);
            }
            File jsonFile = new File(tempDir, "import_temp.json");
            writeLog("临时文件路径：" + jsonFile.getAbsolutePath());

            // 日志5：写入上传文件到临时目录
            writeLog("开始写入上传文件到临时目录");
            try (InputStream is = filePart.getInputStream()) {
                Files.copy(is, jsonFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            writeLog("文件写入成功：临时文件大小=" + jsonFile.length() + "字节");

            // 日志6：调用Service导入数据
            writeLog("开始调用Service导入JSON数据（全部导入，不筛选年月）");
            boolean success = tempService.importMultiTempFromJson(jsonFile);
            writeLog("Service导入结果：" + (success ? "成功" : "失败"));

            // 日志7：删除临时文件
            if (jsonFile.exists()) {
                jsonFile.delete();
                writeLog("临时文件已删除");
            }

            // 日志8：返回结果
            String msg = success ? "导入成功（已更新修改后的月份数据）" : "导入失败（检查JSON格式）";
            writeLog("导入流程结束：" + msg);
            // 用session存提示信息
            request.getSession().setAttribute(success ? "successMsg" : "errorMsg", msg);
            // 重定向到temperature页面（GET请求，不会报405）
            response.sendRedirect("temperature?year=2025&month=12");
        } catch (Exception e) {
            // 日志9：捕获所有异常，定位问题
            writeLog("导入流程异常：" + e.getMessage());
            // 打印异常堆栈到日志（关键：定位具体报错行）
            String stackTrace = getStackTrace(e);
            writeLog("异常堆栈：" + stackTrace);
            // 返回异常提示
            request.setAttribute("errorMsg", "导入失败：" + e.getMessage());
            request.getRequestDispatcher("temperature?year=2025&month=12").forward(request, response);
        }
    }

    // ========== 4. GET请求日志（避免误访问） ==========
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        writeLog("收到GET请求：此接口仅支持POST上传文件，已拒绝");
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