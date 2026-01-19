package com.gdut.demo.controller;

import com.gdut.demo.repository.ScoreRepository;
import com.gdut.demo.service.ScoreQueryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;

@Controller
public class ScoresSearchController {

    private final ScoreQueryService scoreQueryService;

    public ScoresSearchController(ScoreQueryService scoreQueryService) {
        this.scoreQueryService = scoreQueryService;
    }

    /**
     * 成绩综合查询：
     * 支持条件：学号、学生姓名、课程号、课程名称、教师姓名、系号、学期
     * 展示：按课程分组，每门课内按总评降序
     * GET /scores/search
     */
    @GetMapping("/scores/search")
    public String search(@RequestParam(required = false) String studentId,
                         @RequestParam(required = false) String studentName,
                         @RequestParam(required = false) String courseId,
                         @RequestParam(required = false) String courseName,
                         @RequestParam(required = false) String teacherName,
                         @RequestParam(required = false) String deptId,
                         @RequestParam(required = false) String term,
                         Model model) {

        List<ScoreRepository.ScoreSearchRowProjection> rows =
                scoreQueryService.search(studentId, studentName, deptId, courseId, courseName, teacherName, term);
        LinkedHashMap<String, List<ScoreRepository.ScoreSearchRowProjection>> grouped = scoreQueryService.groupByCourse(rows);

        model.addAttribute("studentId", studentId == null ? "" : studentId.trim());
        model.addAttribute("studentName", studentName == null ? "" : studentName.trim());
        model.addAttribute("courseId", courseId == null ? "" : courseId.trim());
        model.addAttribute("courseName", courseName == null ? "" : courseName.trim());
        model.addAttribute("teacherName", teacherName == null ? "" : teacherName.trim());
        model.addAttribute("deptId", deptId == null ? "" : deptId.trim());
        model.addAttribute("term", term == null ? "" : term.trim());

        model.addAttribute("grouped", grouped);
        model.addAttribute("totalCount", rows.size());
        return "scores/search";
    }
}