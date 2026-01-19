package com.gdut.demo.controller;

import com.gdut.demo.model.Score;
import com.gdut.demo.service.ScoreService;
import com.gdut.demo.repository.CourseRepository;
import com.gdut.demo.repository.StaffRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/scores/manage")
public class ScoresManageController {

    private final ScoreService scoreService;
    private final CourseRepository courseRepository;
    private final StaffRepository staffRepository;

    public ScoresManageController(ScoreService scoreService,
                                  CourseRepository courseRepository,
                                  StaffRepository staffRepository) {
        this.scoreService = scoreService;
        this.courseRepository = courseRepository;
        this.staffRepository = staffRepository;
    }

    @GetMapping
    public String page(@RequestParam(required = false) String courseId,
                       @RequestParam(required = false) String term,
                       @RequestParam(required = false) String successMsg,
                       @RequestParam(required = false) String errorMsg,
                       Model model) {
        model.addAttribute("courseId", courseId == null ? "" : courseId.trim());
        model.addAttribute("term", term == null ? "" : term.trim());
        model.addAttribute("successMsg", successMsg);
        model.addAttribute("errorMsg", errorMsg);

        if (courseId != null && term != null && !courseId.isBlank() && !term.isBlank()) {
            // 改为使用成绩列表（Score）
            List<Score> list = scoreService.listByCourseAndTerm(courseId, term);
            model.addAttribute("records", list); // 模板应按 Score 访问：record.id.studentId / usualScore / examScore / totalScore

            courseRepository.findById(courseId).ifPresent(c -> {
                model.addAttribute("courseName", c.getName());
                model.addAttribute("teacherId", c.getTeacherId());
                if (c.getTeacherId() != null && !c.getTeacherId().isBlank()) {
                    staffRepository.findById(c.getTeacherId()).ifPresent(s -> model.addAttribute("teacherName", s.getName()));
                }
            });
        }
        return "scores/manage";
    }

    // 单行保存：统一调用 upsertScore（兼容 updateScore 别名）
    @PostMapping("/update")
    public String update(@RequestParam String studentId,
                         @RequestParam String courseId,
                         @RequestParam String term,
                         @RequestParam(required = false) BigDecimal usualScore,
                         @RequestParam(required = false) BigDecimal examScore) {
        try {
            scoreService.upsertScore(studentId, courseId, term, usualScore, examScore);
            return "redirect:/scores/manage?courseId=" + courseId + "&term=" + term + "&successMsg=成绩已保存";
        } catch (Exception ex) {
            return "redirect:/scores/manage?courseId=" + courseId + "&term=" + term + "&errorMsg=" + urlEncode(ex.getMessage());
        }
    }

    // 批量保存（以成绩为单位）
    @PostMapping("/batch")
    public String batch(@RequestParam String courseId,
                        @RequestParam String term,
                        @RequestParam(value = "studentId") List<String> studentIds,
                        @RequestParam(value = "usualScore", required = false) List<BigDecimal> usualScores,
                        @RequestParam(value = "examScore", required = false) List<BigDecimal> examScores,
                        Model model) {
        int n = studentIds == null ? 0 : studentIds.size();
        long ok = 0, fail = 0;
        for (int i = 0; i < n; i++) {
            String sid = studentIds.get(i);
            BigDecimal u = usualScores != null && i < usualScores.size() ? usualScores.get(i) : null;
            BigDecimal e = examScores != null && i < examScores.size() ? examScores.get(i) : null;
            try {
                scoreService.upsertScore(sid, courseId, term, u, e);
                ok++;
            } catch (Exception ex) {
                fail++;
            }
        }

        model.addAttribute("courseId", courseId);
        model.addAttribute("term", term);
        model.addAttribute("okCount", ok);
        model.addAttribute("failCount", fail);

        // 重新载入成绩列表
        model.addAttribute("records", scoreService.listByCourseAndTerm(courseId, term));
        return "scores/manage";
    }

    private String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }
}