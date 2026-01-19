package com.gdut.demo.controller;

import com.gdut.demo.model.Course;
import com.gdut.demo.service.CourseService;
import com.gdut.demo.service.EnrollmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/enroll/browse") // 改为 /enroll/browse，避免与现有 /enroll 冲突
public class EnrollBrowseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    public EnrollBrowseController(CourseService courseService,
                                  EnrollmentService enrollmentService) {
        this.courseService = courseService;
        this.enrollmentService = enrollmentService;
    }

    // 浏览与搜索课程 + 选课入口
    @GetMapping
    public String page(@RequestParam(required = false) String courseId,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String teacher,
                       @RequestParam(required = false) String studentId,
                       @RequestParam(required = false) String term,
                       @RequestParam(required = false) String ok,
                       @RequestParam(required = false) String err,
                       Model model) {
        List<Course> courses = courseService.search(courseId, name, teacher);
        Map<String, String> teacherNameMap = courseService.teacherNameMapFor(courses);

        model.addAttribute("courseId", courseId == null ? "" : courseId.trim());
        model.addAttribute("name", name == null ? "" : name.trim());
        model.addAttribute("teacher", teacher == null ? "" : teacher.trim());
        model.addAttribute("courses", courses);
        model.addAttribute("teacherNameMap", teacherNameMap);

        model.addAttribute("studentId", studentId == null ? "" : studentId.trim());
        model.addAttribute("term", term == null || term.isBlank() ? "2025-2026-1" : term.trim());
        model.addAttribute("ok", ok);
        model.addAttribute("err", err);
        return "enroll/courses";
    }

    // 选课动作（保持 /enroll/select，不与 /enroll 冲突）
    @PostMapping("/select")
    public String select(@RequestParam String studentId,
                         @RequestParam String term,
                         @RequestParam String courseId) {
        try {
            enrollmentService.enrollStudent(studentId, courseId, term);
            return "redirect:/enroll/browse?studentId=" + enc(studentId) + "&term=" + enc(term) + "&ok=1";
        } catch (Exception ex) {
            return "redirect:/enroll/browse?studentId=" + enc(studentId) + "&term=" + enc(term) + "&err=" + enc(ex.getMessage());
        }
    }

    private String enc(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}