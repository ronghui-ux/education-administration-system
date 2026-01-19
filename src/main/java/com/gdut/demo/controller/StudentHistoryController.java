package com.gdut.demo.controller;

import com.gdut.demo.model.Student;
import com.gdut.demo.repository.StudentRepository;
import com.gdut.demo.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Controller
@RequestMapping("/students/history")
public class StudentHistoryController {

    private final StudentService studentService;
    private final StudentRepository studentRepository;

    public StudentHistoryController(StudentService studentService,
                                    StudentRepository studentRepository) {
        this.studentService = studentService;
        this.studentRepository = studentRepository;
    }

    @GetMapping
    public String manage(Model model) {
        model.addAttribute("now", LocalDateTime.now().toString());
        return "students/history_manage";
    }

    @PostMapping("/graduate")
    public String graduate(@RequestParam String studentId,
                           @RequestParam(required = false) String note,
                           @RequestParam(required = false) String movedAt) {
        LocalDateTime ts = (movedAt == null || movedAt.isBlank())
                ? LocalDateTime.now()
                : LocalDateTime.parse(movedAt);
        studentService.graduate(studentId, note, ts);
        return "redirect:/students/history";
    }

    @PostMapping("/transfer-out")
    public String transferOut(@RequestParam String studentId,
                              @RequestParam(required = false) String note,
                              @RequestParam(required = false) String movedAt) {
        LocalDateTime ts = (movedAt == null || movedAt.isBlank())
                ? LocalDateTime.now()
                : LocalDateTime.parse(movedAt);
        studentService.transferOut(studentId, note, ts);
        return "redirect:/students/history";
    }

    @PostMapping("/transfer-in")
    public String transferIn(@RequestParam String studentId,
                             @RequestParam String name,
                             @RequestParam(required = false) String gender,
                             @RequestParam(required = false) String deptId,
                             @RequestParam(required = false) String note,
                             @RequestParam(required = false) String movedAt) {
        Student s = new Student();
        s.setStudentId(studentId.trim());
        s.setName(name);
        s.setGender(gender);
        s.setDeptId(deptId);
        LocalDateTime ts = (movedAt == null || movedAt.isBlank())
                ? LocalDateTime.now()
                : LocalDateTime.parse(movedAt);
        studentService.transferIn(s, note, ts);
        return "redirect:/students/history";
    }
}
