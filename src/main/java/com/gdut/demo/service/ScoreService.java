package com.gdut.demo.service;

import com.gdut.demo.dto.ScoreUpdateItem;
import com.gdut.demo.model.Score;
import com.gdut.demo.model.ScoreId;
import com.gdut.demo.repository.EnrollmentRepository;
import com.gdut.demo.repository.ScoreRepository;
import com.gdut.demo.repository.CourseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class ScoreService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final ScoreRepository scoreRepository;

    public ScoreService(EnrollmentRepository enrollmentRepository,
                        CourseRepository courseRepository,
                        ScoreRepository scoreRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.scoreRepository = scoreRepository;
    }

    /* ========== 成绩查询（返回 List<Score>）供各控制器使用 ========== */

    // 保持旧方法名，返回成绩列表（修复“List<Score> 无法转为 List<Enrollment”的来源）
    public List<Score> listByCourseAndTerm(String courseId, String term) {
        if (isBlank(courseId) || isBlank(term)) return List.of();
        return scoreRepository.findByIdCourseIdAndIdTerm(courseId.trim(), term.trim());
    }

    public List<Score> listByStudentAndTerm(String studentId, String term) {
        if (isBlank(studentId) || isBlank(term)) return List.of();
        return scoreRepository.findByIdStudentIdAndIdTerm(studentId.trim(), term.trim());
    }

    // 可选别名（如果其它类调用的是 listScoresBy*）
    public List<Score> listScoresByCourseAndTerm(String courseId, String term) { return listByCourseAndTerm(courseId, term); }
    public List<Score> listScoresByStudentAndTerm(String studentId, String term) { return listByStudentAndTerm(studentId, term); }

    /* ========== 成绩 upsert（复合主键 ScoreId） ========== */

    @Transactional
    public void upsertScore(String studentId,
                            String courseId,
                            String term,
                            BigDecimal usualScore,
                            BigDecimal examScore) {
        validateIds(studentId, courseId, term);
        validateScoreRange(usualScore);
        validateScoreRange(examScore);

        // 一致性校验：必须存在选课记录
        enrollmentRepository.findByStudentIdAndCourseIdAndTerm(studentId.trim(), courseId.trim(), term.trim())
                .orElseThrow(() -> new IllegalArgumentException("该学生在该学期未选该课程，无法录入成绩"));

        ScoreId id = new ScoreId(studentId.trim(), courseId.trim(), term.trim());
        Score score = scoreRepository.findById(id).orElseGet(() -> {
            Score s = new Score();
            s.setId(id);
            return s;
        });

        score.setUsualScore(usualScore);
        score.setExamScore(examScore);
        score.setTotalScore(calcTotal(usualScore, examScore));
        scoreRepository.save(score);
    }

    // 兼容旧调用：有的代码还调用 updateScore(...)，这里作为别名转到 upsertScore
    @Transactional
    public void updateScore(String studentId, String courseId, String term, BigDecimal usualScore, BigDecimal examScore) {
        upsertScore(studentId, courseId, term, usualScore, examScore);
    }

    /* ========== 批量更新（以成绩为单位） ========== */
    @Transactional
    public List<ResultItem> updateScoresBatch(List<ScoreUpdateItem> items) {
        List<ResultItem> results = new ArrayList<>();
        if (items == null) return results;
        for (ScoreUpdateItem it : items) {
            ResultItem r = new ResultItem(it.getStudentId(), it.getCourseId(), it.getTerm());
            try {
                upsertScore(it.getStudentId(), it.getCourseId(), it.getTerm(), it.getUsualScore(), it.getExamScore());
                r.setSuccess(true);
                r.setMessage("保存成功");
            } catch (Exception ex) {
                r.setSuccess(false);
                r.setMessage(ex.getMessage());
            }
            results.add(r);
        }
        return results;
    }

    /* ========== 公共校验/计算 ========== */

    private BigDecimal calcTotal(BigDecimal usual, BigDecimal exam) {
        if (usual == null || exam == null) return null;
        BigDecimal total = usual.multiply(new BigDecimal("0.40"))
                .add(exam.multiply(new BigDecimal("0.60")));
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private void validateIds(String studentId, String courseId, String term) {
        if (isBlank(studentId) || isBlank(courseId) || isBlank(term)) {
            throw new IllegalArgumentException("学号/课程号/学期不能为空");
        }
        courseRepository.findById(courseId.trim())
                .orElseThrow(() -> new IllegalArgumentException("课程不存在：" + courseId));
    }

    private void validateScoreRange(BigDecimal score) {
        if (score == null) return; // 允许留空
        if (score.compareTo(BigDecimal.ZERO) < 0 || score.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("成绩必须在 0 到 100 之间");
        }
    }

    public static class ResultItem {
        private final String studentId;
        private final String courseId;
        private final String term;
        private boolean success;
        private String message;

        public ResultItem(String studentId, String courseId, String term) {
            this.studentId = studentId; this.courseId = courseId; this.term = term;
        }
        public String getStudentId() { return studentId; }
        public String getCourseId() { return courseId; }
        public String getTerm() { return term; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    private boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
}