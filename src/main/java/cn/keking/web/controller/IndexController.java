package cn.keking.web.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 *  页面跳转
 * @author yudian-it
 * @date 2017/12/27
 */
@Controller
public class IndexController {

    @GetMapping( "/")
    public void root(HttpServletRequest req,HttpServletResponse response) throws IOException {
    	String url = req.getContextPath() + "/file-search/search.html";
        response.sendRedirect(url);
    }


}
