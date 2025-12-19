package controller;

import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.core.FileUploadException;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletFileUpload; // 核心：新导入路径
import service.TemperatureDBService;

// 保留 Jakarta Servlet 导入（不变）
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@WebServlet("/importTempController")
public class ImportTempController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private TemperatureDBService tempService = new TemperatureDBService();
    // 临时目录（项目内相对路径）
    private static final String TEMP_DIR = System.getProperty("user.dir") + "/src/main/webapp/WEB-INF/temp_upload";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        // 1. 检查是否为文件上传请求（新版本API）
        if (!JakartaServletFileUpload.isMultipartContent(request)) {
            request.setAttribute("error", "导入失败：不是有效的文件上传请求");
            forwardToTempPage(request, response, request.getParameter("year"), request.getParameter("month"));
            return;
        }

        //2. 初始化文件上传工厂（新版本API，逻辑不变但包路径变）
        DiskFileItemFactory factory = DiskFileItemFactory.builder()
                .setPath(Paths.get(TEMP_DIR))
                .setBufferSize(1024 * 1024)
                .get(); // 关键：替换 setRepository → setPath，参数为 Path 类型 // 设置临时目录


        // 3. 创建上传实例
        JakartaServletFileUpload<FileItem<DiskFileItemFactory>, DiskFileItemFactory> upload = new JakartaServletFileUpload<>(factory);
        upload.setFileSizeMax(10L * 1024L * 1024L); // 10MB 限制（long类型）


        // 4. 解析上传请求（新版本API：parseRequest 方法参数不变，但类是新的）
        List<FileItem<DiskFileItemFactory>> items = null;
        try {
            items = upload.parseRequest(request); // 此处不再报错！
        } catch (FileUploadException e) {
            request.setAttribute("error", "文件解析失败：" + e.getMessage());
            forwardToTempPage(request, response, request.getParameter("year"), request.getParameter("month"));
            return;
        }

        // 5. 处理上传的文件（逻辑和旧版本完全一致）
        File jsonFile = null;
        String year = getParamOrDefault(request, "year", "2025");
        String month = getParamOrDefault(request, "month", "12");

        if (items != null && !items.isEmpty()) {
            for (FileItem<DiskFileItemFactory> item : items) {
                if (!item.isFormField()) { // 处理文件字段
                    String fileName = item.getName();
                    // 校验文件类型
                    if (fileName == null || fileName.isEmpty() || !fileName.endsWith(".json")) {
                        request.setAttribute("error", "导入失败：仅支持 .json 格式文件");
                        forwardToTempPage(request, response, year, month);
                        return;
                    }
                    // 创建临时文件（避免重名）
                    File tempDir = new File(TEMP_DIR);
                    if (!tempDir.exists()) {
                        tempDir.mkdirs();
                    }
                    jsonFile = new File(tempDir, System.currentTimeMillis() + "_" + fileName);
                    // 写入文件（新版本 FileItem 的 write 方法不变）
                    try {
                        item.write(jsonFile.toPath());
                    } catch (Exception e) {
                        request.setAttribute("error", "文件保存失败：" + e.getMessage());
                        forwardToTempPage(request, response, year, month);
                        return;
                    }
                }
            }
        }

        // 6. 调用 Service 导入数据（逻辑不变）
        if (jsonFile != null && jsonFile.exists()) {
            boolean success = tempService.importMultiTempFromJson(jsonFile);
            // 删除临时文件
            if (jsonFile.exists()) {
                jsonFile.delete();
            }
            if (success) {
                request.setAttribute("success", "导入成功！已处理所有月份数据");
            } else {
                request.setAttribute("error", "导入失败：JSON 格式错误或数据库异常");
            }
        } else {
            request.setAttribute("error", "导入失败：未选择有效文件");
        }

        // 7. 跳转回原页面
        forwardToTempPage(request, response, year, month);
    }

    // 辅助方法（和旧版本完全一致，无需修改）
    private String getParamOrDefault(HttpServletRequest request, String paramName, String defaultValue) {
        String value = request.getParameter(paramName);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    private void forwardToTempPage(HttpServletRequest request, HttpServletResponse response, String year, String month) throws ServletException, IOException {
        request.getRequestDispatcher("temperature?year=" + year + "&month=" + month).forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String year = getParamOrDefault(request, "year", "2025");
        String month = getParamOrDefault(request, "month", "12");
        response.sendRedirect(request.getContextPath() + "/temperature?year=" + year + "&month=" + month);
    }
}