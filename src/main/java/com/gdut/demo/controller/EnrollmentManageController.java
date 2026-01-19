package com.gdut.demo.controller;

import com.gdut.demo.dto.EnrollMatrixResultItem;
import com.gdut.demo.model.Enrollment;
import com.gdut.demo.service.EnrollmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/enroll/manage")
public class EnrollmentManageController {

    private final EnrollmentService enrollmentService;

    public EnrollmentManageController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public String page(@RequestParam(required = false) String studentId,
                       @RequestParam(required = false) String courseId,
                       @RequestParam(required = false) String term,
                       Model model) {
        model.addAttribute("studentId", studentId == null ? "" : studentId.trim());
        model.addAttribute("courseId", courseId == null ? "" : courseId.trim());
        model.addAttribute("term", term == null ? "" : term.trim());

        if (studentId != null && term != null) {
            List<Enrollment> byStudent = enrollmentService.listByStudentAndTerm(studentId, term);
            model.addAttribute("byStudent", byStudent);
        }
        if (courseId != null && term != null) {
            List<Enrollment> byCourse = enrollmentService.listByCourseAndTerm(courseId, term);
            model.addAttribute("byCourse", byCourse);
        }
        return "enroll/manage";
    }

    // 一个学生选多门课（返回逐条结果）
    @PostMapping("/by-student")
    public String batchByStudent(@RequestParam String studentId,
                                 @RequestParam String term,
                                 @RequestParam String courseIds,
                                 Model model) {
        List<String> cids = split(courseIds);
        List<EnrollMatrixResultItem> result = enrollmentService.enrollBatchByStudent(studentId, term, cids);
        long ok = result.stream().filter(EnrollMatrixResultItem::isSuccess).count();
        long fail = result.size() - ok;

        model.addAttribute("studentId", studentId);
        model.addAttribute("term", term);
        model.addAttribute("courseIds", courseIds);
        model.addAttribute("result", result);
        model.addAttribute("okCount", ok);
        model.addAttribute("failCount", fail);
        return "enroll/manage";
    }

    // 一门课选多个学生（返回逐条结果）
    @PostMapping("/by-course")
    public String batchByCourse(@RequestParam String courseId,
                                @RequestParam String term,
                                @RequestParam String studentIds,
                                Model model) {
        List<String> sids = split(studentIds);
        List<EnrollMatrixResultItem> result = enrollmentService.enrollBatchByCourse(courseId, term, sids);
        long ok = result.stream().filter(EnrollMatrixResultItem::isSuccess).count();
        long fail = result.size() - ok;

        model.addAttribute("courseId", courseId);
        model.addAttribute("term", term);
        model.addAttribute("studentIds", studentIds);
        model.addAttribute("result", result);
        model.addAttribute("okCount", ok);
        model.addAttribute("failCount", fail);
        return "enroll/manage";
    }

    @PostMapping("/delete")
    public String delete(@RequestParam String studentId,
                         @RequestParam String courseId,
                         @RequestParam String term) {
        enrollmentService.deleteEnrollment(studentId, courseId, term);
        return "redirect:/enroll/manage?studentId=" + studentId + "&term=" + term;
    }

    @PostMapping("/status")
    public String updateStatus(@RequestParam String studentId,
                               @RequestParam String courseId,
                               @RequestParam String term,
                               @RequestParam String status) {
        enrollmentService.updateEnrollmentStatus(studentId, courseId, term, status);
        return "redirect:/enroll/manage?studentId=" + studentId + "&term=" + term;
    }

    private List<String> split(String s) {
        if (s == null || s.trim().isEmpty()) return List.of();
        return Arrays.stream(s.split("[,\\s]+"))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .toList();
    }
}