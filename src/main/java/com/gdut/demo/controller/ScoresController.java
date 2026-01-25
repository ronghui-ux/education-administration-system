package com.gdut.demo.controller;

import com.gdut.demo.model.Score;
import com.gdut.demo.repository.EnrollmentRepository;
import com.gdut.demo.service.ScoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 关键改动：
 * - 规范化入参（去空格、只取第一个值，避免出现 "C001,C001" 这种情况）
 * - 查询结果同时以 results 和 rows 两个模型键放入，兼容不同模板写法
 */
@Controller
@RequestMapping("/scores")
public class ScoresController {

    private final ScoreService scoreService;
    private final EnrollmentRepository enrollmentRepository;

    public ScoresController(ScoreService scoreService,
                            EnrollmentRepository enrollmentRepository) {
        this.scoreService = scoreService;
        this.enrollmentRepository = enrollmentRepository;
    }

    /* ===== 按课程录入与查询 ===== */

    @GetMapping("/by-course")
    public String byCourse(@RequestParam(required = false) String courseId,
                           @RequestParam(required = false) String term,
                           @RequestParam(required = false) String importEnrolled,
                           Model model) {
        String cid = first(courseId);
        String tm  = first(term);

        List<Score> scores = scoreService.listByCourseAndTerm(cid, tm);

        model.addAttribute("courseId", cid);
        model.addAttribute("term", tm);
        // 同时提供两种键名，兼容不同模板
        model.addAttribute("results", scores);
        model.addAttribute("rows", scores);
        model.addAttribute("resultCount", scores.size());

        // 旧模板的“从选课导入学生名单”功能所需数据（可选）
        if (!cid.isEmpty() && !tm.isEmpty() && "1".equals(importEnrolled)) {
            model.addAttribute("enrolled", enrollmentRepository.findSelectedByCourseAndTermIgnoreCase(cid, tm));
        }
        return "scores/by-course";
    }

    @PostMapping("/by-course/save")
    public String saveByCourse(@RequestParam String studentId,
                               @RequestParam String courseId,
                               @RequestParam String term,
                               @RequestParam(required = false) BigDecimal usualScore,
                               @RequestParam(required = false) BigDecimal examScore) {
        scoreService.upsertScore(studentId, courseId, term, usualScore, examScore);
        return "redirect:/scores/by-course?courseId=" + url(first(courseId)) + "&term=" + url(first(term));
    }

    /* ===== 按学生录入与查询 ===== */

    @GetMapping("/by-student")
    public String byStudent(@RequestParam(required = false) String studentId,
                            @RequestParam(required = false) String term,
                            Model model) {
        String sid = first(studentId);
        String tm  = first(term);

        List<Score> scores = scoreService.listByStudentAndTerm(sid, tm);

        model.addAttribute("studentId", sid);
        model.addAttribute("term", tm);
        model.addAttribute("results", scores);
        model.addAttribute("rows", scores);
        model.addAttribute("resultCount", scores.size());
        return "scores/by-student";
    }

    @PostMapping("/by-student/save")
    public String saveByStudent(@RequestParam String studentId,
                                @RequestParam String courseId,
                                @RequestParam String term,
                                @RequestParam(required = false) BigDecimal usualScore,
                                @RequestParam(required = false) BigDecimal examScore) {
        scoreService.upsertScore(studentId, courseId, term, usualScore, examScore);
        return "redirect:/scores/by-student?studentId=" + url(first(studentId)) + "&term=" + url(first(term));
    }

    /* ===== helpers ===== */
    private String first(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.isEmpty()) return "";
        int i = t.indexOf(',');
        return i >= 0 ? t.substring(0, i).trim() : t;
    }
    private String url(String s) {
        return s == null ? "" : java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}