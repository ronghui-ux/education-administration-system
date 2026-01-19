package com.gdut.demo.controller;

import com.gdut.demo.model.Course;
import com.gdut.demo.service.CourseService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@Controller
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * 课程查询页面：按课程号/课程名/教师姓名
     * GET /courses/search
     */
    @GetMapping("/courses/search")
    public String search(@RequestParam(required = false) String courseId,
                         @RequestParam(required = false) String name,
                         @RequestParam(required = false) String teacher,
                         Model model) {
        List<Course> courses = courseService.search(courseId, name, teacher);
        Map<String, String> teacherNameMap = courseService.teacherNameMapFor(courses);

        model.addAttribute("courseId", courseId == null ? "" : courseId.trim());
        model.addAttribute("name", name == null ? "" : name.trim());
        model.addAttribute("teacher", teacher == null ? "" : teacher.trim());
        model.addAttribute("courses", courses);
        model.addAttribute("teacherNameMap", teacherNameMap);
        model.addAttribute("resultCount", courses.size());
        return "courses/search";
    }
}