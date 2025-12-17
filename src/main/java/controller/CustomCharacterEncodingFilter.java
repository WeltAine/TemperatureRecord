package controller;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import java.io.IOException;

// 全局过滤所有请求
@WebFilter("/*")
public class CustomCharacterEncodingFilter implements Filter {
    private String encoding = "UTF-8";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // 可选：从web.xml读取编码配置（也可直接固定为UTF-8）
        String configEncoding = filterConfig.getInitParameter("encoding");
        if (configEncoding != null && !configEncoding.isEmpty()) {
            this.encoding = configEncoding;
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 设置请求编码（处理前端提交的中文参数）
        request.setCharacterEncoding(this.encoding);
        // 设置响应编码（处理页面输出的中文）
        response.setCharacterEncoding(this.encoding);
        response.setContentType("text/html;charset=" + this.encoding);
        // 放行请求
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // 无需处理
    }
}
