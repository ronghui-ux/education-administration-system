package com.gdut.demo.controller;

import com.gdut.demo.model.Student;
import com.gdut.demo.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/students/history")
public class StudentHistoryController {

    private final StudentService studentService;

    public StudentHistoryController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    public String manage(Model model) {
        // 向前端提供当前时间对象，便于 datetime-local 预填
        model.addAttribute("now", LocalDateTime.now());
        return "students/history_manage";
    }

    @PostMapping("/graduate")
    public String graduate(@RequestParam String studentId,
                           @RequestParam(required = false) String note,
                           @RequestParam(required = false) String movedAt,
                           RedirectAttributes ra) {
        try {
            LocalDateTime ts = parseFlexibleDateTime(movedAt, LocalDateTime.now());
            studentService.graduate(studentId, note, ts);
            ra.addFlashAttribute("ok", "已办理毕业：" + studentId);
        } catch (Exception ex) {
            ra.addFlashAttribute("err", "毕业失败：" + ex.getMessage());
        }
        return "redirect:/students/history";
    }

    @PostMapping("/transfer-out")
    public String transferOut(@RequestParam String studentId,
                              @RequestParam(required = false) String note,
                              @RequestParam(required = false) String movedAt,
                              RedirectAttributes ra) {
        try {
            LocalDateTime ts = parseFlexibleDateTime(movedAt, LocalDateTime.now());
            studentService.transferOut(studentId, note, ts);
            ra.addFlashAttribute("ok", "已办理转出：" + studentId);
        } catch (Exception ex) {
            ra.addFlashAttribute("err", "转出失败：" + ex.getMessage());
        }
        return "redirect:/students/history";
    }

    @PostMapping("/transfer-in")
    public String transferIn(@RequestParam String studentId,
                             @RequestParam String name,
                             @RequestParam(required = false) String gender,
                             @RequestParam(required = false) String deptId,
                             @RequestParam(required = false) String note,
                             @RequestParam(required = false) String movedAt,
                             RedirectAttributes ra) {
        try {
            Student s = new Student();
            s.setStudentId(studentId.trim());
            s.setName(name);
            s.setGender(gender);
            s.setDeptId(deptId);
            LocalDateTime ts = parseFlexibleDateTime(movedAt, LocalDateTime.now());
            studentService.transferIn(s, note, ts);
            ra.addFlashAttribute("ok", "已办理转入：" + studentId);
        } catch (Exception ex) {
            ra.addFlashAttribute("err", "转入失败：" + ex.getMessage());
        }
        return "redirect:/students/history";
    }

    /**
     * 兼容多种输入格式：
     * - yyyy-MM-dd（仅日期，自动补 00:00:00）
     * - yyyy-MM-dd HH:mm
     * - yyyy-MM-dd HH:mm:ss
     * - yyyy-MM-dd'T'HH:mm（HTML datetime-local）
     * - 其它尝试 ISO_LOCAL_DATE_TIME
     * 若为空则返回默认值 defaultValue。
     */
    private LocalDateTime parseFlexibleDateTime(String text, LocalDateTime defaultValue) {
        if (text == null || text.isBlank()) return defaultValue;
        String s = text.trim();
        try {
            if (s.matches("\\d{4}-\\d{2}-\\d{2}")) {
                return LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
            }
            if (s.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")) {
                return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            }
            if (s.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
                return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            }
            if (s.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}")) {
                return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
            }
            return LocalDateTime.parse(s);
        } catch (Exception ex) {
            throw new IllegalArgumentException("时间格式不正确：" + s + "。支持：yyyy-MM-dd、yyyy-MM-dd HH:mm、yyyy-MM-dd HH:mm:ss、yyyy-MM-dd'T'HH:mm。", ex);
        }
    }
}