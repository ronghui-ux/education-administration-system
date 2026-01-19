package com.gdut.demo.controller;

import com.gdut.demo.service.EnrollmentService;
import com.gdut.demo.service.EnrollmentService.EnrollmentRecord;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    // 学生选课汇总页：展示选课明细并计算总学分
    @GetMapping("/enroll/summary")
    public String summary(@RequestParam String studentId,
                          @RequestParam String term,
                          Model model) {

        // 取明细（selected 状态）
        List<EnrollmentRecord> selectedRecords =
                enrollmentService.getEnrollmentRecords(studentId, term, "selected");

        // 明细用于页面展示
        model.addAttribute("studentId", studentId);
        model.addAttribute("term", term);
        model.addAttribute("selectedRecords", selectedRecords);

        // 方案A：直接对明细列表汇总（返回 BigDecimal）
        BigDecimal totalCreditsFromList = enrollmentService.calculateTotalCredits(selectedRecords);
        model.addAttribute("totalCreditsFromList", totalCreditsFromList);

        // 方案B：仅用学号+学期直接汇总（返回 BigDecimal）
        BigDecimal totalCreditsDirect = enrollmentService.calculateTotalCredits(studentId, term);
        model.addAttribute("totalCreditsDirect", totalCreditsDirect);

        // 你也可以只保留其中一种（把另一种删除），两者都返回 BigDecimal，不会再出现“void 无法转换为 BigDecimal”
        return "enroll/summary";
    }

    // 如需展示 dropped 或全部状态，可再加一个端点或参数控制
    @GetMapping("/enroll/summary-all")
    public String summaryAll(@RequestParam String studentId,
                             @RequestParam String term,
                             Model model) {
        List<EnrollmentRecord> allRecords =
                enrollmentService.getEnrollmentRecords(studentId, term, null); // status 为空返回该学期所有（按你的 service 实现）
        model.addAttribute("studentId", studentId);
        model.addAttribute("term", term);
        model.addAttribute("allRecords", allRecords);

        // 对“所有记录”按 selected 状态进行学分汇总
        BigDecimal totalCredits = enrollmentService.calculateTotalCredits(allRecords);
        model.addAttribute("totalCredits", totalCredits);

        return "enroll/summary";
    }
}