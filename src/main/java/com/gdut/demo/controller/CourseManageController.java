package com.gdut.demo.controller;

import com.gdut.demo.model.Course;
import com.gdut.demo.repository.CourseRepository;
import com.gdut.demo.repository.StaffRepository;
import com.gdut.demo.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/courses")
public class CourseManageController {

    private final CourseRepository courseRepository;
    private final StaffRepository staffRepository;
    private final CourseService courseService;

    public CourseManageController(CourseRepository courseRepository,
                                  StaffRepository staffRepository,
                                  CourseService courseService) {
        this.courseRepository = courseRepository;
        this.staffRepository = staffRepository;
        this.courseService = courseService;
    }

    // 列表 + 搜索（不影响 /courses/search 已有的查询页）
    @GetMapping
    public String list(@RequestParam(required = false) String courseId,
                       @RequestParam(required = false) String name,
                       @RequestParam(required = false) String teacher,
                       Model model) {
        List<Course> courses = courseService.search(courseId, name, teacher);
        var teacherNameMap = courseService.teacherNameMapFor(courses);

        model.addAttribute("courseId", courseId == null ? "" : courseId.trim());
        model.addAttribute("name", name == null ? "" : name.trim());
        model.addAttribute("teacher", teacher == null ? "" : teacher.trim());
        model.addAttribute("courses", courses);
        // 关键：传入 teacherNameMap，供模板安全取教师姓名
        model.addAttribute("teacherNameMap", teacherNameMap);
        return "courses/list";
    }

    // 新增表单
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("staffs", staffRepository.findAll());
        return "courses/form";
    }

    // 编辑表单
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") String id, Model model) {
        return courseRepository.findById(id)
                .map(c -> {
                    model.addAttribute("course", c);
                    model.addAttribute("staffs", staffRepository.findAll());
                    return "courses/form";
                })
                .orElse("redirect:/courses");
    }

    // 保存（新增/编辑）
    @PostMapping
    public String save(@Valid @ModelAttribute("course") Course course,
                       BindingResult br,
                       Model model) {
        if (br.hasErrors()) {
            model.addAttribute("staffs", staffRepository.findAll());
            return "courses/form";
        }
        if (course.getTeacherId() != null) {
            course.setTeacherId(course.getTeacherId().trim());
        }
        courseRepository.save(course);
        return "redirect:/courses";
    }

    // 删除
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") String id) {
        courseRepository.deleteById(id);
        return "redirect:/courses";
    }
}