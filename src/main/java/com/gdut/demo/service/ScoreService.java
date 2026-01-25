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

    /* ========== 成绩查询（大小写不敏感） ========== */

    public List<Score> listByCourseAndTerm(String courseId, String term) {
        String cid = t(courseId);
        String tm  = t(term);
        if (cid.isEmpty() || tm.isEmpty()) return List.of();
        return scoreRepository.findByCourseIdAndTermIgnoreCase(cid, tm);
    }

    public List<Score> listByStudentAndTerm(String studentId, String term) {
        String sid = t(studentId);
        String tm  = t(term);
        if (sid.isEmpty() || tm.isEmpty()) return List.of();
        return scoreRepository.findByStudentIdAndTermIgnoreCase(sid, tm);
    }

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

        String sid = studentId.trim();
        String cid = courseId.trim();
        String tm  = term.trim();

        // 一致性校验：必须存在选课记录（大小写不敏感，openGauss 友好）
        boolean enrolled = enrollmentRepository.existsEnrollmentIgnoreCase(sid, cid, tm);
        if (!enrolled) {
            throw new IllegalArgumentException("该学生在该学期未选该课程，无法录入成绩");
        }

        // 先按大小写不敏感查找已有成绩，若无则新建
        Score score = scoreRepository.findOneIgnoreCase(sid, cid, tm)
                .orElseGet(() -> {
                    Score s = new Score();
                    s.setId(new ScoreId(sid, cid, tm));
                    return s;
                });

        score.setUsualScore(usualScore);
        score.setExamScore(examScore);
        score.setTotalScore(calcTotal(usualScore, examScore));
        scoreRepository.save(score);
    }

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
    private String t(String s) { return s == null ? "" : s.trim(); }
}