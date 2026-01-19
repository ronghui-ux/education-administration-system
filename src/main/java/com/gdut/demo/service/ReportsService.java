package com.gdut.demo.service;

import com.gdut.demo.repository.EnrollmentRepository;
import com.gdut.demo.repository.ScoreRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReportsService {

    private final EnrollmentRepository enrollmentRepository;
    private final ScoreRepository scoreRepository;

    public ReportsService(EnrollmentRepository enrollmentRepository,
                          ScoreRepository scoreRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.scoreRepository = scoreRepository;
    }

    public byte[] exportRegistrationCsv(String term, String courseId, String courseName, String teacher, String deptId) {
        List<EnrollmentRepository.RegistrationRow> rows =
                enrollmentRepository.registrationRows(emptyToNull(term), emptyToNull(courseId), emptyToNull(courseName), emptyToNull(teacher), emptyToNull(deptId));

        StringBuilder sb = new StringBuilder();
        // 头
        sb.append("course_id,course_name,teacher_id,teacher_name,hours,credits,schedule,location,exam_time,term,student_id,student_name,gender,usual_score,exam_score,total_score\n");

        for (var r : rows) {
            sb.append(csv(r.getCourseId())).append(',')
                    .append(csv(r.getCourseName())).append(',')
                    .append(csv(r.getTeacherId())).append(',')
                    .append(csv(r.getTeacherName())).append(',')
                    .append(csv(r.getHours())).append(',')
                    .append(csv(r.getCredits())).append(',')
                    .append(csv(r.getSchedule())).append(',')
                    .append(csv(r.getLocation())).append(',')
                    .append(csv(dt(r.getExamTime()))).append(',')
                    .append(csv(r.getTerm())).append(',')
                    .append(csv(r.getStudentId())).append(',')
                    .append(csv(r.getStudentName())).append(',')
                    .append(csv(r.getGender())).append(',')
                    .append("") .append(',') // usual 空
                    .append("") .append(',') // exam 空
                    .append("")              // total 空
                    .append('\n');
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    public byte[] exportGradesCsv(String term, String courseId, String courseName, String teacherName, String deptId) {
        List<ScoreRepository.GradesReportRow> rows =
                scoreRepository.gradesReportRows(emptyToNull(term), emptyToNull(courseId), emptyToNull(courseName), emptyToNull(teacherName), emptyToNull(deptId));

        StringBuilder sb = new StringBuilder();
        // 头
        sb.append("course_id,course_name,teacher_name,term,student_id,student_name,dept_id,usual_score,exam_score,total_score\n");

        // 逐课程输出明细，并添加分段统计
        String currentCourse = null;
        List<BigDecimal> bucket = new ArrayList<>();
        for (var r : rows) {
            String courseKey = r.getCourseId();
            if (currentCourse != null && !Objects.equals(currentCourse, courseKey)) {
                // 输出上一门课的统计
                appendSummary(sb, bucket, currentCourse);
                bucket.clear();
            }
            currentCourse = courseKey;
            BigDecimal total = safe(r.getTotalScore());
            bucket.add(total);

            sb.append(csv(r.getCourseId())).append(',')
                    .append(csv(r.getCourseName())).append(',')
                    .append(csv(r.getTeacherName())).append(',')
                    .append(csv(r.getTerm())).append(',')
                    .append(csv(r.getStudentId())).append(',')
                    .append(csv(r.getStudentName())).append(',')
                    .append(csv(r.getDeptId())).append(',')
                    .append(csv(r.getUsualScore())).append(',')
                    .append(csv(r.getExamScore())).append(',')
                    .append(csv(total)).append('\n');
        }
        // 最后一门课的统计
        if (currentCourse != null) {
            appendSummary(sb, bucket, currentCourse);
        }
        return sb.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);
    }

    /* ------------------ 辅助 ------------------ */

    private void appendSummary(StringBuilder sb, List<BigDecimal> totals, String courseKey) {
        int n = totals.size();
        int ge90 = 0, ge80 = 0, ge70 = 0, ge60 = 0, lt60 = 0;
        for (BigDecimal t : totals) {
            double v = t.doubleValue();
            if (v >= 90) ge90++;
            else if (v >= 80) ge80++;
            else if (v >= 70) ge70++;
            else if (v >= 60) ge60++;
            else lt60++;
        }
        sb.append("summary,").append(courseKey).append(",>=90,").append(ge90).append(',').append(pct(ge90, n)).append('\n');
        sb.append("summary,").append(courseKey).append(",>=80,").append(ge80).append(',').append(pct(ge80, n)).append('\n');
        sb.append("summary,").append(courseKey).append(",>=70,").append(ge70).append(',').append(pct(ge70, n)).append('\n');
        sb.append("summary,").append(courseKey).append(",>=60,").append(ge60).append(',').append(pct(ge60, n)).append('\n');
        sb.append("summary,").append(courseKey).append(",<60,").append(lt60).append(',').append(pct(lt60, n)).append('\n');
        // 空行分隔课程
        sb.append('\n');
    }

    private String pct(int count, int total) {
        if (total == 0) return "0%";
        BigDecimal p = new BigDecimal(count * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
        return p.toPlainString() + "%";
    }

    private String csv(Object o) {
        if (o == null) return "";
        String s = (o instanceof BigDecimal) ? ((BigDecimal) o).toPlainString() : o.toString();
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            s = s.replace("\"", "\"\"");
            return "\"" + s + "\"";
        }
        return s;
    }

    private String dt(LocalDateTime t) {
        if (t == null) return "";
        return t.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    private String emptyToNull(String s) {
        return (s == null || s.trim().isEmpty()) ? null : s.trim();
    }

    private BigDecimal safe(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }
}
