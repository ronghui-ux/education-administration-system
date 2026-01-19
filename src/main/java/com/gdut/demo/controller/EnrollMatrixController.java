package com.gdut.demo.controller;

import com.gdut.demo.dto.EnrollMatrixResultItem;
import com.gdut.demo.service.EnrollmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/enroll/matrix")
public class EnrollMatrixController {

    private final EnrollmentService enrollmentService;

    public EnrollMatrixController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public String page(Model model) {
        model.addAttribute("term", "2025-2026-1");
        return "enroll/matrix";
    }

    @PostMapping
    public String submit(@RequestParam String term,
                         @RequestParam String studentIds,
                         @RequestParam String courseIds,
                         Model model) {
        List<String> sids = splitList(studentIds);
        List<String> cids = splitList(courseIds);
        List<EnrollMatrixResultItem> results = enrollmentService.enrollMatrix(sids, cids, term);

        long ok = results.stream().filter(EnrollMatrixResultItem::isSuccess).count();
        long fail = results.size() - ok;

        model.addAttribute("term", term);
        model.addAttribute("studentIds", studentIds);
        model.addAttribute("courseIds", courseIds);
        model.addAttribute("results", results);
        model.addAttribute("okCount", ok);
        model.addAttribute("failCount", fail);
        return "enroll/matrix";
    }

    private List<String> splitList(String s) {
        if (s == null || s.trim().isEmpty()) return List.of();
        // 支持逗号、空格、换行分隔
        return Arrays.stream(s.split("[,\\s]+"))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .toList();
    }
}