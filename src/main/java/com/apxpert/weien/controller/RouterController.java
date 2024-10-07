package com.apxpert.weien.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class RouterController {
    @RequestMapping("/")
    public String home(Model model) {
        model.addAttribute("content", "order.jsp");
        return "/index";
    }

    @RequestMapping("/order-list")
    public String orderList(Model model) {
        model.addAttribute("content", "orderList.jsp");
        return "/index";
    }

    @RequestMapping("/api-test")
    public String apiTest(Model model) {
        model.addAttribute("content", "apiTest.jsp");
        return "/index";
    }
}
