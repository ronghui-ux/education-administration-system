package com.gdut.demo.controller;

import com.gdut.demo.model.Department;
import com.gdut.demo.repository.DepartmentRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    public DepartmentController(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // 列表 + 搜索
    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        List<Department> departments;
        if (q != null && !q.isBlank()) {
            departments = departmentRepository.findByDeptIdContainingIgnoreCaseOrNameContainingIgnoreCase(q.trim(), q.trim());
        } else {
            departments = departmentRepository.findAll();
        }
        model.addAttribute("departments", departments);
        model.addAttribute("q", q == null ? "" : q);
        return "departments/list";
    }

    // 新增表单
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("department", new Department());
        return "departments/form";
    }

    // 编辑表单
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") String id, Model model) {
        Optional<Department> opt = departmentRepository.findById(id);
        if (opt.isPresent()) {
            model.addAttribute("department", opt.get());
            return "departments/form";
        }
        return "redirect:/departments";
    }

    // 保存（新增/编辑）
    @PostMapping
    public String save(@Valid @ModelAttribute("department") Department department,
                       BindingResult br) {
        if (br.hasErrors()) {
            return "departments/form";
        }
        departmentRepository.save(department);
        return "redirect:/departments";
    }

    // 删除
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") String id) {
        departmentRepository.deleteById(id);
        return "redirect:/departments";
    }
}