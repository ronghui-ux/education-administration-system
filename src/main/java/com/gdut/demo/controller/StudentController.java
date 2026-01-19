package com.gdut.demo.controller;

import com.gdut.demo.model.Student;
import com.gdut.demo.service.StudentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    /**
     * 学生查询页面：按学号、姓名、系号进行模糊查询（任意组合）
     * GET /students
     */
    @GetMapping("/students")
    public String list(@RequestParam(required = false) String studentId,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String deptId,
                       Model model) {

        String sid = studentId == null ? "" : studentId.trim();
        String nm = name == null ? "" : name.trim();
        String did = deptId == null ? "" : deptId.trim();

        List<Student> students;
        if (sid.isEmpty() && nm.isEmpty() && did.isEmpty()) {
            students = studentService.findAll();
        } else {
            students = studentService.searchByFilters(sid, nm, did);
        }

        model.addAttribute("studentId", sid);
        model.addAttribute("name", nm);
        model.addAttribute("deptId", did);
        model.addAttribute("students", students);
        model.addAttribute("resultCount", students.size());
        return "students/list";
    }
}