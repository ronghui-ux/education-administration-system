package com.gdut.demo.controller;

import com.gdut.demo.model.Enrollment;
import com.gdut.demo.model.Score;
import com.gdut.demo.model.Student;
import com.gdut.demo.repository.EnrollmentRepository;
import com.gdut.demo.repository.StudentRepository;
import com.gdut.demo.service.ScoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/scores")
public class ScoreController {

    private final ScoreService scoreService;
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;

    public ScoreController(ScoreService scoreService,
                           EnrollmentRepository enrollmentRepository,
                           StudentRepository studentRepository) {
        this.scoreService = scoreService;
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
    }

    // 按课程输入与查询
    @GetMapping("/by-course")
    public String byCourse(@RequestParam(required = false) String courseId,
                           @RequestParam(required = false) String term,
                           @RequestParam(required = false) String importEnrolled, // import=1 时展示批量录入表格
                           Model model) {
        String effectiveTerm = (term == null || term.isBlank()) ? "2025-2026-1" : term.trim();
        model.addAttribute("term", effectiveTerm);
        model.addAttribute("courseId", courseId == null ? "" : courseId.trim());

        List<Score> list = Collections.emptyList();
        if (courseId != null && !courseId.isBlank()) {
            // 改为调用返回 List<Score> 的方法
            list = scoreService.listScoresByCourseAndTerm(courseId.trim(), effectiveTerm);
        }
        model.addAttribute("scores", list);
        model.addAttribute("resultCount", list.size());

        // 如果请求带 importEnrolled=1，则加载选课名单，生成批量录入行
        if (courseId != null && !courseId.isBlank() && "1".equals(importEnrolled)) {
            List<Enrollment> enrolled = enrollmentRepository.findByCourseIdAndTermAndStatus(courseId.trim(), effectiveTerm, "selected");
            // 已有成绩映射：studentId -> Score
            Map<String, Score> scoreMap = list.stream()
                    .collect(Collectors.toMap(s -> s.getId().getStudentId(), s -> s, (a, b) -> a));

            // 学生姓名（可选）
            Set<String> stuIds = enrolled.stream().map(Enrollment::getStudentId).collect(Collectors.toSet());
            Map<String, String> nameMap = studentRepository.findAllById(stuIds).stream()
                    .collect(Collectors.toMap(Student::getStudentId, Student::getName));

            List<BatchRow> rows = new ArrayList<>();
            for (Enrollment e : enrolled) {
                String sid = e.getStudentId();
                Score sc = scoreMap.get(sid);
                rows.add(new BatchRow(
                        sid,
                        nameMap.getOrDefault(sid, ""),
                        sc == null ? null : sc.getUsualScore(),
                        sc == null ? null : sc.getExamScore(),
                        sc == null ? null : sc.getTotalScore()
                ));
            }
            // 按学号排序，便于填写
            rows.sort(Comparator.comparing(BatchRow::getStudentId));
            model.addAttribute("batchRows", rows);
        }

        return "scores/by-course";
    }

    // 单条保存（保留原有）
    @PostMapping("/by-course/save")
    public String saveByCourse(@RequestParam String courseId,
                               @RequestParam String term,
                               @RequestParam String studentId,
                               @RequestParam(required = false) BigDecimal usualScore,
                               @RequestParam(required = false) BigDecimal examScore,
                               Model model) {
        scoreService.upsertScore(studentId, courseId, term, usualScore, examScore);
        return "redirect:/scores/by-course?courseId=" + courseId + "&term=" + term;
    }

    // 批量保存
    @PostMapping("/by-course/batch-save")
    public String batchSaveByCourse(@RequestParam String courseId,
                                    @RequestParam String term,
                                    @RequestParam List<String> studentId,
                                    @RequestParam(required = false) List<BigDecimal> usualScore,
                                    @RequestParam(required = false) List<BigDecimal> examScore) {
        int n = studentId.size();
        for (int i = 0; i < n; i++) {
            String sid = studentId.get(i);
            BigDecimal u = (usualScore != null && i < usualScore.size()) ? usualScore.get(i) : null;
            BigDecimal e = (examScore != null && i < examScore.size()) ? examScore.get(i) : null;
            scoreService.upsertScore(sid, courseId, term, u, e);
        }
        return "redirect:/scores/by-course?courseId=" + courseId + "&term=" + term + "&importEnrolled=1";
    }

    // 按学生输入与查询
    @GetMapping("/by-student")
    public String byStudent(@RequestParam(required = false) String studentId,
                            @RequestParam(required = false) String term,
                            Model model) {
        String effectiveTerm = (term == null || term.isBlank()) ? "2025-2026-1" : term.trim();
        model.addAttribute("term", effectiveTerm);
        model.addAttribute("studentId", studentId == null ? "" : studentId.trim());

        List<Score> list = Collections.emptyList();
        if (studentId != null && !studentId.isBlank()) {
            // 改为调用返回 List<Score> 的方法
            list = scoreService.listScoresByStudentAndTerm(studentId.trim(), effectiveTerm);
        }
        model.addAttribute("scores", list);
        model.addAttribute("resultCount", list.size());
        return "scores/by-student";
    }

    @PostMapping("/by-student/save")
    public String saveByStudent(@RequestParam String studentId,
                                @RequestParam String term,
                                @RequestParam String courseId,
                                @RequestParam(required = false) BigDecimal usualScore,
                                @RequestParam(required = false) BigDecimal examScore,
                                Model model) {
        scoreService.upsertScore(studentId, courseId, term, usualScore, examScore);
        return "redirect:/scores/by-student?studentId=" + studentId + "&term=" + term;
    }

    // 批量行 DTO（仅用于视图）
    public static class BatchRow {
        private final String studentId;
        private final String name;
        private final BigDecimal usualScore;
        private final BigDecimal examScore;
        private final BigDecimal totalScore;

        public BatchRow(String studentId, String name, BigDecimal usualScore, BigDecimal examScore, BigDecimal totalScore) {
            this.studentId = studentId;
            this.name = name;
            this.usualScore = usualScore;
            this.examScore = examScore;
            this.totalScore = totalScore;
        }
        public String getStudentId() { return studentId; }
        public String getName() { return name; }
        public BigDecimal getUsualScore() { return usualScore; }
        public BigDecimal getExamScore() { return examScore; }
        public BigDecimal getTotalScore() { return totalScore; }
    }
}