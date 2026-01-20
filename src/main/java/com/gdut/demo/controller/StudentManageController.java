package com.gdut.demo.controller;

import com.gdut.demo.model.Student;
import com.gdut.demo.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生基本信息管理：
 * - 新增（新生信息录入）
 * - 按学号/姓名/系号查询与编辑
 * - 删除（可选）
 * 注：转学/毕业请使用已存在的 /students/history 管理页与 StudentHistoryController。
 */
@Controller
@RequestMapping("/students/manage")
public class StudentManageController {

    private final StudentService studentService;

    public StudentManageController(StudentService studentService) {
        this.studentService = studentService;
    }

    // 列表 + 组合搜索（学号/姓名/系号任意组合）
    @GetMapping
    public String list(@RequestParam(required = false) String studentId,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String deptId,
                       Model model) {
        String sid = studentId == null ? "" : studentId.trim();
        String nm  = name == null ? "" : name.trim();
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

    // 新增表单（新生信息录入）
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("student", new Student());
        return "students/form";
    }

    // 编辑表单（按学号进入）
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") String id, Model model) {
        return studentService.findById(id)
                .map(s -> { model.addAttribute("student", s); return "students/form"; })
                .orElse("redirect:/students/manage");
    }

    // 保存（新增/编辑）
    @PostMapping
    public String save(@Valid @ModelAttribute("student") Student student,
                       BindingResult br) {
        if (br.hasErrors()) {
            return "students/form";
        }
        if (student.getStudentId() != null) {
            student.setStudentId(student.getStudentId().trim());
        }
        studentService.save(student);
        return "redirect:/students/manage?studentId=" + student.getStudentId();
    }

    // 删除（谨慎使用）
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") String id) {
        studentService.deleteById(id);
        return "redirect:/students/manage";
    }
}