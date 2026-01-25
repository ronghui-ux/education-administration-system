package com.gdut.demo.controller;

import com.gdut.demo.model.Enrollment;
import com.gdut.demo.model.Score;
import com.gdut.demo.repository.CourseRepository;
import com.gdut.demo.repository.EnrollmentRepository;
import com.gdut.demo.repository.StaffRepository;
import com.gdut.demo.service.ScoreService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.*;

/**
 * v10
 * 修复点：
 * - 解决“批量保存后成功 0、失败 N”以及页面出现“C001,C001 / 2025-2026-1,2025-2026-1”的问题。
 *   原因：页面存在嵌套 form，浏览器会把每行里的隐藏 courseId/term 一并提交，Spring 收到多个同名参数后
 *   组合成用逗号拼接的字符串，导致服务端拿到的 courseId/term 是 "C001,C001"。本版彻底移除嵌套 form，只保留
 *   一个批量保存表单；控制器端也做了去重规范化（只取第一个值）。
 * - 合并显示“选课名单（selected）∪ 已有成绩”，没有成绩的学生也能直接新增。
 */
@Controller
@RequestMapping("/scores/manage")
public class ScoresManageController {

    private final ScoreService scoreService;
    private final CourseRepository courseRepository;
    private final StaffRepository staffRepository;
    private final EnrollmentRepository enrollmentRepository;

    public ScoresManageController(ScoreService scoreService,
                                  CourseRepository courseRepository,
                                  StaffRepository staffRepository,
                                  EnrollmentRepository enrollmentRepository) {
        this.scoreService = scoreService;
        this.courseRepository = courseRepository;
        this.staffRepository = staffRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    // 合并后的行数据模型
    public static class ScoreInputRow {
        private String studentId;
        private String courseId;
        private String term;
        private BigDecimal usualScore;
        private BigDecimal examScore;
        private BigDecimal totalScore;
        private boolean hasScore;

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getCourseId() { return courseId; }
        public void setCourseId(String courseId) { this.courseId = courseId; }
        public String getTerm() { return term; }
        public void setTerm(String term) { this.term = term; }
        public BigDecimal getUsualScore() { return usualScore; }
        public void setUsualScore(BigDecimal usualScore) { this.usualScore = usualScore; }
        public BigDecimal getExamScore() { return examScore; }
        public void setExamScore(BigDecimal examScore) { this.examScore = examScore; }
        public BigDecimal getTotalScore() { return totalScore; }
        public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
        public boolean isHasScore() { return hasScore; }
        public void setHasScore(boolean hasScore) { this.hasScore = hasScore; }
    }

    @GetMapping
    public String page(@RequestParam(required = false) String courseId,
                       @RequestParam(required = false) String term,
                       @RequestParam(required = false) String successMsg,
                       @RequestParam(required = false) String errorMsg,
                       Model model) {

        String cid = first(courseId);
        String tm  = first(term);

        model.addAttribute("courseId", cid);
        model.addAttribute("term", tm);
        model.addAttribute("successMsg", successMsg);
        model.addAttribute("errorMsg", errorMsg);

        // 课程信息
        if (!cid.isBlank()) {
            courseRepository.findById(cid).ifPresent(c -> {
                model.addAttribute("courseName", c.getName());
                model.addAttribute("teacherId", c.getTeacherId());
                if (c.getTeacherId() != null && !c.getTeacherId().isBlank()) {
                    staffRepository.findById(c.getTeacherId())
                            .ifPresent(s -> model.addAttribute("teacherName", s.getName()));
                }
            });
        }

        if (!cid.isBlank() && !tm.isBlank()) {
            // 已有成绩
            List<Score> scoreList = scoreService.listByCourseAndTerm(cid, tm);
            Map<String, Score> scoreByStudent = new LinkedHashMap<>();
            for (Score sc : scoreList) {
                scoreByStudent.put(sc.getId().getStudentId(), sc);
            }

            // 选课名单（selected，大小写不敏感）
            List<Enrollment> enrolled = enrollmentRepository.findSelectedByCourseAndTermIgnoreCase(cid, tm);

            // 合并（选课 ∪ 成绩），选课优先，再补充“有成绩但不在选课名单”的学生
            List<ScoreInputRow> rows = new ArrayList<>();
            Set<String> included = new HashSet<>();

            for (Enrollment e : enrolled) {
                String sid = e.getStudentId();
                included.add(sid.toUpperCase());

                ScoreInputRow row = new ScoreInputRow();
                row.setStudentId(sid);
                row.setCourseId(cid);
                row.setTerm(tm);

                Score sc = scoreByStudent.get(sid);
                if (sc != null) {
                    row.setHasScore(true);
                    row.setUsualScore(sc.getUsualScore());
                    row.setExamScore(sc.getExamScore());
                    row.setTotalScore(sc.getTotalScore());
                }
                rows.add(row);
            }

            for (Score sc : scoreList) {
                String sid = sc.getId().getStudentId();
                if (!included.contains(sid.toUpperCase())) {
                    ScoreInputRow row = new ScoreInputRow();
                    row.setStudentId(sid);
                    row.setCourseId(cid);
                    row.setTerm(tm);
                    row.setHasScore(true);
                    row.setUsualScore(sc.getUsualScore());
                    row.setExamScore(sc.getExamScore());
                    row.setTotalScore(sc.getTotalScore());
                    rows.add(row);
                }
            }

            rows.sort(Comparator.comparing(r -> r.getStudentId().toUpperCase()));

            model.addAttribute("rows", rows);
            model.addAttribute("enrolledCount", enrolled.size());
            model.addAttribute("hasScoreCount", scoreList.size());
        } else {
            model.addAttribute("rows", List.of());
            model.addAttribute("enrolledCount", 0);
            model.addAttribute("hasScoreCount", 0);
        }

        return "scores/manage";
    }

    // 批量保存：移除嵌套 form 后的唯一提交入口
    @PostMapping("/batch")
    public String batch(@RequestParam String courseId,
                        @RequestParam String term,
                        @RequestParam(value = "studentId") List<String> studentIds,
                        @RequestParam(value = "usualScore", required = false) List<BigDecimal> usualScores,
                        @RequestParam(value = "examScore", required = false) List<BigDecimal> examScores,
                        RedirectAttributes ra) {

        String cid = first(courseId);
        String tm  = first(term);

        int n = studentIds == null ? 0 : studentIds.size();
        long ok = 0, fail = 0;
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            String sid = safeGet(studentIds, i);
            BigDecimal u = safeGet(usualScores, i);
            BigDecimal e = safeGet(examScores, i);
            if (sid == null || sid.isBlank()) continue; // 跳过空行
            try {
                scoreService.upsertScore(sid.trim(), cid, tm, u, e);
                ok++;
            } catch (Exception ex) {
                fail++;
                if (errors.size() < 5) {
                    errors.add(sid + ": " + ex.getMessage());
                }
            }
        }

        String summary = "批量保存完成：成功 " + ok + " 条，失败 " + fail + " 条";
        if (fail == 0) {
            ra.addFlashAttribute("successMsg", summary);
        } else {
            if (!errors.isEmpty()) {
                summary += "。示例错误：" + String.join("；", errors);
            }
            ra.addFlashAttribute("errorMsg", summary);
        }

        return "redirect:/scores/manage?courseId=" + url(cid) + "&term=" + url(tm);
    }

    /* ------------- helpers ------------- */
    private String first(String s) {
        if (s == null) return "";
        String t = s.trim();
        if (t.isEmpty()) return "";
        int comma = t.indexOf(',');
        return comma >= 0 ? t.substring(0, comma).trim() : t;
    }

    private <T> T safeGet(List<T> list, int i) {
        if (list == null) return null;
        return i < list.size() ? list.get(i) : null;
    }

    private String url(String s) {
        return s == null ? "" : java.net.URLEncoder.encode(s, java.nio.charset.StandardCharsets.UTF_8);
    }
}