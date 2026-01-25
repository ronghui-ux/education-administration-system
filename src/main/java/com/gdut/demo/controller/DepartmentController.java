package com.gdut.demo.controller;

import com.gdut.demo.model.Department;
import com.gdut.demo.repository.DepartmentRepository;
import com.gdut.demo.repository.StaffRepository;
import com.gdut.demo.repository.StudentRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/departments")
public class DepartmentController {

    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final StaffRepository staffRepository;

    public DepartmentController(DepartmentRepository departmentRepository,
                                 StudentRepository studentRepository,
                                 StaffRepository staffRepository) {
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.staffRepository = staffRepository;
    }

    /* ---------------- 列表与查询 ---------------- */

    /**
     * 系列表与查询（按系号、系名；大小写不敏感、包含匹配）
     * GET /departments?deptId=CS&name=计算机
     */
    @GetMapping
    public String list(@RequestParam(required = false) String deptId,
                       @RequestParam(required = false) String name,
                       Model model) {
        String did = nz(deptId);
        String nm  = nz(name);

        List<Department> all = departmentRepository.findAll();
        List<Department> filtered = all.stream()
                .filter(d -> did.isEmpty() || containsIgnoreCase(d.getDeptId(), did))
                .filter(d -> nm.isEmpty()  || containsIgnoreCase(d.getName(), nm))
                .collect(Collectors.toList());

        model.addAttribute("deptId", did);
        model.addAttribute("name", nm);
        model.addAttribute("departments", filtered);
        model.addAttribute("resultCount", filtered.size());
        return "departments/list";
    }

    /* ---------------- 新增与编辑表单 ---------------- */

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("dept", new Department());
        model.addAttribute("mode", "create");
        return "departments/form";
    }

    @GetMapping("/{deptId}/edit")
    public String editForm(@PathVariable String deptId, Model model, RedirectAttributes ra) {
        return departmentRepository.findById(nz(deptId))
                .map(d -> {
                    model.addAttribute("dept", d);
                    model.addAttribute("mode", "edit");
                    return "departments/form";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("err", "未找到系：" + deptId);
                    return "redirect:/departments";
                });
    }

    /* ---------------- 保存（新增/更新） ---------------- */

    /**
     * 保存操作：新增或更新。
     * - 新增：dept.deptId 作为主键必填
     * - 更新：不允许修改主键（表单应将 deptId 设为只读）
     */
    @PostMapping("/save")
    public String save(@ModelAttribute("dept") Department dept, RedirectAttributes ra) {
        if (dept == null || isBlank(dept.getDeptId())) {
            ra.addFlashAttribute("err", "保存失败：系号不能为空");
            return "redirect:/departments";
        }

        // 修剪与规范化
        dept.setDeptId(dept.getDeptId().trim());
        if (dept.getName() != null) dept.setName(dept.getName().trim());
        if (dept.getIntro() != null) dept.setIntro(dept.getIntro().trim());

        boolean exists = departmentRepository.existsById(dept.getDeptId());
        departmentRepository.save(dept);
        ra.addFlashAttribute("ok", (exists ? "更新成功：" : "新增成功：") + dept.getDeptId());
        return "redirect:/departments";
    }

    /* ---------------- 删除 ---------------- */

    /**
     * 删除支持 GET 和 POST（兼容你页面上的超链接 /departments/{deptId}/delete）
     * 删除失败（被引用）时，捕获外键异常并返回友好提示，而不是 500 白页。
     */
    @RequestMapping(value = "/{deptId}/delete", method = {RequestMethod.GET, RequestMethod.POST})
    public String delete(@PathVariable String deptId, RedirectAttributes ra) {
        String id = nz(deptId);
        try {
            departmentRepository.deleteById(id);
            ra.addFlashAttribute("ok", "删除成功：" + id);
        } catch (DataIntegrityViolationException ex) {
            long stuCount = safeCountStudents(id);
            long staffCount = safeCountStaff(id);
            String msg = String.format("无法删除：系 %s 仍被 %d 名学生、%d 名教职工引用。请先迁移后再删除。", id, stuCount, staffCount);
            ra.addFlashAttribute("err", msg);
        } catch (Exception ex) {
            ra.addFlashAttribute("err", "删除失败：" + ex.getMessage());
        }
        return "redirect:/departments";
    }

    /* ---------------- helpers ---------------- */

    private String nz(String s) { return s == null ? "" : s.trim(); }
    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private boolean containsIgnoreCase(String src, String sub) {
        if (src == null) return false;
        return src.toLowerCase().contains(sub.toLowerCase());
    }
    private long safeCountStudents(String deptId) {
        try { return studentRepository.countByDeptIdIgnoreCase(deptId); }
        catch (Exception ignore) { return 0L; }
    }
    private long safeCountStaff(String deptId) {
        try { return staffRepository.countByDeptIdIgnoreCase(deptId); }
        catch (Exception ignore) { return 0L; }
    }
}