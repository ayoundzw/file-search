package cn.keking.web.filter;


import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 *
 * @author yudian-it
 * @date 2017/11/30
 */
public class ChinesePathFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        HttpServletResponse resp = (HttpServletResponse)response;
        resp.setHeader("X-Frame-Options", "SAMEORIGIN");
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
