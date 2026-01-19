package com.gdut.demo.service;

import com.gdut.demo.repository.ScoreRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ScoreQueryService {

    private final ScoreRepository scoreRepository;

    public ScoreQueryService(ScoreRepository scoreRepository) {
        this.scoreRepository = scoreRepository;
    }

    public List<ScoreRepository.ScoreSearchRowProjection> search(
            String studentId, String studentName, String deptId,
            String courseId, String courseName, String teacherName,
            String term
    ) {
        return scoreRepository.searchComposite(
                emptyToNull(studentId), emptyToNull(studentName), emptyToNull(deptId),
                emptyToNull(courseId), emptyToNull(courseName), emptyToNull(teacherName),
                emptyToNull(term)
        );
    }

    // 将结果按课程分组（保序：课程号升序，组内总评降序已在 SQL 中处理）
    public LinkedHashMap<String, List<ScoreRepository.ScoreSearchRowProjection>> groupByCourse(
            List<ScoreRepository.ScoreSearchRowProjection> rows
    ) {
        LinkedHashMap<String, List<ScoreRepository.ScoreSearchRowProjection>> map = new LinkedHashMap<>();
        for (var r : rows) {
            map.computeIfAbsent(r.getCourseId(), k -> new ArrayList<>()).add(r);
        }
        return map;
    }

    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }
}