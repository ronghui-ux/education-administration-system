package com.gdut.demo.controller;

import com.gdut.demo.model.Enrollment;
import com.gdut.demo.model.Score;
import com.gdut.demo.repository.EnrollmentRepository;
import com.gdut.demo.service.ScoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
        String cid = courseId == null ? "" : courseId.trim();
        String tm  = term == null ? "" : term.trim();

        List<Score> scores = scoreService.listByCourseAndTerm(cid, tm);
        model.addAttribute("courseId", cid);
        model.addAttribute("term", tm);
        model.addAttribute("results", scores);
        model.addAttribute("resultCount", scores.size());

        // 可选：导入该课程在该学期的已选学生名单用于辅助录入（不自动创建成绩行）
        if (!cid.isEmpty() && !tm.isEmpty() && "1".equals(importEnrolled)) {
            List<Enrollment> enrolled = enrollmentRepository.findByCourseIdAndTermAndStatus(cid, tm, "selected");
            model.addAttribute("enrolled", enrolled);
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
        return "redirect:/scores/by-course?courseId=" + url(courseId) + "&term=" + url(term);
    }

    /* ===== 按学生录入与查询 ===== */

    @GetMapping("/by-student")
    public String byStudent(@RequestParam(required = false) String studentId,
                            @RequestParam(required = false) String term,
                            Model model) {
        String sid = studentId == null ? "" : studentId.trim();
        String tm  = term == null ? "" : term.trim();

        List<Score> scores = scoreService.listByStudentAndTerm(sid, tm);
        model.addAttribute("studentId", sid);
        model.addAttribute("term", tm);
        model.addAttribute("results", scores);
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
        return "redirect:/scores/by-student?studentId=" + url(studentId) + "&term=" + url(term);
    }

    private String url(String s) {
        return s == null ? "" : java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}