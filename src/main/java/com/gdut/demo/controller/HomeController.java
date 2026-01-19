package com.gdut.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;

@Controller
public class HomeController {

    @GetMapping({"/", "/home"})
    public String index(Model model) {
        model.addAttribute("today", LocalDate.now());
        // 可在此添加全局菜单数据或版本信息
        return "home/index";
    }
}