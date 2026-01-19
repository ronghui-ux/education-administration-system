package com.gdut.demo.controller;

import com.gdut.demo.model.Staff;
import com.gdut.demo.repository.StaffRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/staffs")
public class StaffController {

    private final StaffRepository staffRepository;

    public StaffController(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    // 列表 + 搜索
    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        List<Staff> staffs;
        if (q != null && !q.isBlank()) {
            String kw = q.trim();
            staffs = staffRepository.findByStaffIdContainingIgnoreCaseOrNameContainingIgnoreCaseOrDeptIdContainingIgnoreCase(kw, kw, kw);
        } else {
            staffs = staffRepository.findAll();
        }
        model.addAttribute("staffs", staffs);
        model.addAttribute("q", q == null ? "" : q);
        return "staffs/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("staff", new Staff()); // 提供 th:object
        return "staffs/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") String id, Model model) {
        return staffRepository.findById(id)
                .map(s -> { model.addAttribute("staff", s); return "staffs/form"; })
                .orElse("redirect:/staffs");
    }

    // 保存（新增/编辑）
    @PostMapping
    public String save(@Valid @ModelAttribute("staff") Staff staff,
                       BindingResult br) {
        if (br.hasErrors()) {
            return "staffs/form";
        }
        staffRepository.save(staff);
        return "redirect:/staffs";
    }

    // 删除
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") String id) {
        staffRepository.deleteById(id);
        return "redirect:/staffs";
    }
}