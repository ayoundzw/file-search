package cn.keking.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

    @RequestMapping("/login")
    public String login() {
        return "login/index.html";
    }
    
    @RequestMapping("/fail")
    public String fail() {
        return "login/fail.html";
    }
    
    
    @RequestMapping("/login/css")
    public String logincss() {
        return "login/css/style.css";
    }

}
