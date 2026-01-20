package com.gdut.demo.controller;

import com.gdut.demo.repository.ScoreRepository;
import com.gdut.demo.service.ScoreQueryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.LinkedHashMap;
import java.util.List;

@Controller
@RequestMapping("/scores")
public class ScoresSearchController {

    private final ScoreQueryService scoreQueryService;

    public ScoresSearchController(ScoreQueryService scoreQueryService) {
        this.scoreQueryService = scoreQueryService;
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String studentId,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String deptId,
            @RequestParam(required = false) String courseId,
            @RequestParam(required = false) String courseName,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) String term,
            Model model
    ) {
        List<ScoreRepository.ScoreSearchRowProjection> rows = scoreQueryService.search(
                studentId, studentName, deptId, courseId, courseName, teacherName, term
        );
        LinkedHashMap<String, List<ScoreRepository.ScoreSearchRowProjection>> grouped =
                scoreQueryService.groupByCourse(rows);

        model.addAttribute("studentId", nz(studentId));
        model.addAttribute("studentName", nz(studentName));
        model.addAttribute("deptId", nz(deptId));
        model.addAttribute("courseId", nz(courseId));
        model.addAttribute("courseName", nz(courseName));
        model.addAttribute("teacherName", nz(teacherName));
        model.addAttribute("term", nz(term));

        model.addAttribute("rows", rows);
        model.addAttribute("grouped", grouped);
        model.addAttribute("resultCount", rows.size());
        return "scores/search";
    }

    private String nz(String s) { return s == null ? "" : s; }
}