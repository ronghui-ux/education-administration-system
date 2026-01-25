package com.gdut.demo.controller;

import com.gdut.demo.repository.CourseRepository;
import com.gdut.demo.repository.EnrollmentRepository;
import com.gdut.demo.repository.ScoreRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * 安全删除：仅映射到 /courses/{courseId}/delete-safe
 * 避免与已有的 /courses/{courseId}/delete 冲突。
 * 删除前检查：若 enrollment 或 grade 中仍有引用，则拒绝删除并提示。
 */
@Controller
@RequestMapping("/courses")
public class CoursesDeleteController {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ScoreRepository scoreRepository;

    public CoursesDeleteController(CourseRepository courseRepository,
                                   EnrollmentRepository enrollmentRepository,
                                   ScoreRepository scoreRepository) {
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.scoreRepository = scoreRepository;
    }

    @RequestMapping(value = "/{courseId}/delete-safe", method = {RequestMethod.POST, RequestMethod.GET})
    public String deleteSafe(@PathVariable String courseId, RedirectAttributes ra) {
        String cid = courseId == null ? "" : courseId.trim();
        if (cid.isEmpty()) {
            ra.addFlashAttribute("err", "删除失败：课程号为空");
            return "redirect:/courses";
        }

        long enrollRefs = 0L;
        long gradeRefs  = 0L;
        try { enrollRefs = enrollmentRepository.countByCourseIdIgnoreCase(cid); } catch (Exception ignored) {}
        try { gradeRefs  = scoreRepository.countByCourseIdIgnoreCase(cid); } catch (Exception ignored) {}

        if (enrollRefs > 0 || gradeRefs > 0) {
            ra.addFlashAttribute("err",
                    String.format("无法删除：课程 %s 仍被引用（选课 %d 条、成绩 %d 条）。请先清理相关记录后再删除。", cid, enrollRefs, gradeRefs));
            return "redirect:/courses";
        }

        try {
            courseRepository.deleteById(cid);
            ra.addFlashAttribute("ok", "课程已删除：" + cid);
        } catch (Exception ex) {
            ra.addFlashAttribute("err", "删除失败：" + ex.getMessage());
        }
        return "redirect:/courses";
    }
}