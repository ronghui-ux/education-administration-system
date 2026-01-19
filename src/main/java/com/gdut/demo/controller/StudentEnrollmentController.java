package com.gdut.demo.controller;

import com.gdut.demo.model.Student;
import com.gdut.demo.repository.StudentRepository;
import com.gdut.demo.service.EnrollmentService;
import com.gdut.demo.service.EnrollmentService.EnrollmentRecord;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Controller
public class StudentEnrollmentController {

    private final EnrollmentService enrollmentService;
    private final StudentRepository studentRepository;

    public StudentEnrollmentController(EnrollmentService enrollmentService,
                                       StudentRepository studentRepository) {
        this.enrollmentService = enrollmentService;
        this.studentRepository = studentRepository;
    }

    /**
     * 查看学生选课与总学分页面（GET）。支持可选查询参数 studentId 与 term。
     * 默认学期："2025-2026-1"
     */
    @GetMapping("/students/enrollments")
    public String viewEnrollments(@RequestParam(required = false) String studentId,
                                  @RequestParam(required = false) String term,
                                  Model model) {
        String effectiveTerm = (term == null || term.isBlank()) ? "2025-2026-1" : term.trim();
        model.addAttribute("term", effectiveTerm);
        model.addAttribute("studentId", studentId == null ? "" : studentId.trim());

        if (studentId == null || studentId.isBlank()) {
            // 还没提交学号，显示空表单
            model.addAttribute("records", Collections.emptyList());
            model.addAttribute("totalCredits", BigDecimal.ZERO);
            model.addAttribute("student", null);
            return "students/enrollments";
        }

        String trimmedStudentId = studentId.trim();
        List<EnrollmentRecord> records = enrollmentService.getEnrollmentRecords(trimmedStudentId, effectiveTerm, "selected");
        BigDecimal total = enrollmentService.calculateTotalCredits(records);

        // 查询学生信息（可选）
        Optional<Student> studentOpt = studentRepository.findById(trimmedStudentId);
        model.addAttribute("student", studentOpt.orElse(null));

        model.addAttribute("records", records);
        model.addAttribute("totalCredits", total);
        model.addAttribute("resultCount", records.size());
        return "students/enrollments";
    }
}