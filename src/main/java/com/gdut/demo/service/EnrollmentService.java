package com.gdut.demo.service;

import com.gdut.demo.dto.EnrollMatrixResultItem;
import com.gdut.demo.model.Course;
import com.gdut.demo.model.Enrollment;
import com.gdut.demo.repository.CourseRepository;
import com.gdut.demo.repository.EnrollmentRepository;
import com.gdut.demo.repository.StaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class EnrollmentService {

    private static final BigDecimal MAX_CREDITS = new BigDecimal("15.00");

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final StaffRepository staffRepository;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             CourseRepository courseRepository,
                             StaffRepository staffRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.staffRepository = staffRepository;
    }

    /* ================= 投影类型 ================= */

    public static class EnrollmentRecord {
        private String studentId;
        private String courseId;
        private String courseName;
        private String teacherId;
        private String teacherName;
        private Integer hours;
        private BigDecimal credits;
        private String schedule;
        private String location;
        private java.time.LocalDateTime examTime;
        private String term;
        private String status;

        public String getStudentId() { return studentId; }
        public void setStudentId(String studentId) { this.studentId = studentId; }
        public String getCourseId() { return courseId; }
        public void setCourseId(String courseId) { this.courseId = courseId; }
        public String getCourseName() { return courseName; }
        public void setCourseName(String courseName) { this.courseName = courseName; }
        public String getTeacherId() { return teacherId; }
        public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
        public String getTeacherName() { return teacherName; }
        public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
        public Integer getHours() { return hours; }
        public void setHours(Integer hours) { this.hours = hours; }
        public BigDecimal getCredits() { return credits; }
        public void setCredits(BigDecimal credits) { this.credits = credits; }
        public String getSchedule() { return schedule; }
        public void setSchedule(String schedule) { this.schedule = schedule; }
        public String getLocation() { return location; }
        public void setLocation(String location) { this.location = location; }
        public java.time.LocalDateTime getExamTime() { return examTime; }
        public void setExamTime(java.time.LocalDateTime examTime) { this.examTime = examTime; }
        public String getTerm() { return term; }
        public void setTerm(String term) { this.term = term; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    /* ================= 查询 ================= */

    public List<EnrollmentRecord> getEnrollmentRecords(String studentId, String term, String status) {
        String sid = n(studentId);
        String tm  = n(term);
        String st  = n(status);

        List<Enrollment> rows;
        if (!sid.isEmpty() && !tm.isEmpty()) {
            // 改为大小写不敏感查询，避免因为大小写不同查不到
            rows = enrollmentRepository.findByStudentIdAndTermIgnoreCase(sid, tm);
            if (!st.isEmpty()) {
                rows = rows.stream().filter(e -> st.equalsIgnoreCase(n(e.getStatus()))).toList();
            }
        } else {
            rows = List.of();
        }

        List<EnrollmentRecord> list = new ArrayList<>();
        for (Enrollment e : rows) {
            EnrollmentRecord r = new EnrollmentRecord();
            r.setStudentId(e.getStudentId());
            r.setCourseId(e.getCourseId());
            r.setTerm(e.getTerm());
            r.setStatus(e.getStatus());

            courseRepository.findById(e.getCourseId()).ifPresent(c -> {
                r.setCourseName(c.getName());
                r.setTeacherId(c.getTeacherId());
                r.setHours(c.getHours());
                r.setCredits(c.getCredits());
                r.setSchedule(c.getSchedule());
                r.setLocation(c.getLocation());
                r.setExamTime(c.getExamTime());
                if (c.getTeacherId() != null && !c.getTeacherId().isBlank()) {
                    staffRepository.findById(c.getTeacherId()).ifPresent(s -> r.setTeacherName(s.getName()));
                }
            });

            list.add(r);
        }
        return list;
    }

    /* ================= 学分汇总（返回 BigDecimal） ================= */

    public BigDecimal calculateTotalCredits(List<EnrollmentRecord> records) {
        BigDecimal sum = BigDecimal.ZERO;
        if (records == null) return sum;
        for (EnrollmentRecord r : records) {
            if (r == null) continue;
            if (!"selected".equalsIgnoreCase(n(r.getStatus()))) continue;
            BigDecimal cr = r.getCredits();
            if (cr != null) sum = sum.add(cr);
        }
        return sum;
    }

    public BigDecimal calculateTotalCredits(String studentId, String term) {
        String sid = n(studentId);
        String tm  = n(term);
        if (sid.isEmpty() || tm.isEmpty()) return BigDecimal.ZERO;
        // 使用大小写不敏感的查询，统计更准确
        List<Enrollment> rows = enrollmentRepository.findByStudentIdAndTermIgnoreCase(sid, tm);
        BigDecimal sum = BigDecimal.ZERO;
        for (Enrollment e : rows) {
            if (!"selected".equalsIgnoreCase(n(e.getStatus()))) continue;
            Course c = courseRepository.findById(e.getCourseId()).orElse(null);
            if (c != null && c.getCredits() != null) {
                sum = sum.add(c.getCredits());
            }
        }
        return sum;
    }

    /* ================= 管理页列表 ================= */

    public List<Enrollment> listByStudentAndTerm(String studentId, String term) {
        String sid = n(studentId);
        String tm  = n(term);
        if (sid.isEmpty() || tm.isEmpty()) return List.of();
        return enrollmentRepository.findByStudentIdAndTermIgnoreCase(sid, tm);
    }

    public List<Enrollment> listByCourseAndTerm(String courseId, String term) {
        String cid = n(courseId);
        String tm  = n(term);
        if (cid.isEmpty() || tm.isEmpty()) return List.of();
        return enrollmentRepository.findByCourseIdAndTermIgnoreCase(cid, tm);
    }

    /* ================= 单条与批量（含上限校验） ================= */

    @Transactional
    public void enrollStudent(String studentId, String courseId, String term) {
        String sid = n(studentId);
        String cid = n(courseId);
        String tm  = n(term);
        if (sid.isEmpty() || cid.isEmpty() || tm.isEmpty()) {
            throw new IllegalArgumentException("学号/课程号/学期不能为空");
        }

        // 课程存在性
        Course course = courseRepository.findById(cid)
                .orElseThrow(() -> new IllegalArgumentException("所选课程不存在：" + cid));
        BigDecimal courseCredits = course.getCredits() == null ? BigDecimal.ZERO : course.getCredits();

        // 重复选课（openGauss 兼容的原生 exists，大小写不敏感）
        if (enrollmentRepository.existsEnrollmentIgnoreCase(sid, cid, tm)) {
            throw new IllegalStateException("学生已选过该课程：" + cid);
        }

        // 学分上限（仅统计 selected）
        BigDecimal currentTotal = calculateTotalCredits(sid, tm);
        BigDecimal newTotal = currentTotal.add(courseCredits);
        if (newTotal.compareTo(MAX_CREDITS) > 0) {
            throw new IllegalStateException(String.format(
                    "超过学分上限 15：当前已选 %.2f，本课程学分 %.2f，合计 %.2f",
                    currentTotal.doubleValue(),
                    courseCredits.doubleValue(),
                    newTotal.doubleValue()));
        }

        // 保存
        Enrollment e = new Enrollment();
        e.setStudentId(sid);
        e.setCourseId(cid);
        e.setTerm(tm);
        e.setStatus("selected");
        enrollmentRepository.save(e);
    }

    // 返回逐条结果：一个学生选多门课
    @Transactional
    public List<EnrollMatrixResultItem> enrollBatchByStudent(String studentId, String term, List<String> courseIds) {
        List<EnrollMatrixResultItem> results = new ArrayList<>();
        String sid = n(studentId);
        String tm  = n(term);
        if (sid.isEmpty() || tm.isEmpty() || courseIds == null) return results;
        for (String cidRaw : courseIds) {
            String cid = n(cidRaw);
            if (cid.isEmpty()) continue;
            EnrollMatrixResultItem item = new EnrollMatrixResultItem(sid, cid);
            try {
                enrollStudent(sid, cid, tm);
                item.setSuccess(true);
                item.setMessage("OK");
            } catch (Exception ex) {
                item.setSuccess(false);
                item.setMessage(ex.getMessage());
            }
            results.add(item);
        }
        return results;
    }

    // 返回逐条结果：一门课为多个学生选课
    @Transactional
    public List<EnrollMatrixResultItem> enrollBatchByCourse(String courseId, String term, List<String> studentIds) {
        List<EnrollMatrixResultItem> results = new ArrayList<>();
        String cid = n(courseId);
        String tm  = n(term);
        if (cid.isEmpty() || tm.isEmpty() || studentIds == null) return results;
        for (String sidRaw : studentIds) {
            String sid = n(sidRaw);
            if (sid.isEmpty()) continue;
            EnrollMatrixResultItem item = new EnrollMatrixResultItem(sid, cid);
            try {
                enrollStudent(sid, cid, tm);
                item.setSuccess(true);
                item.setMessage("OK");
            } catch (Exception ex) {
                item.setSuccess(false);
                item.setMessage(ex.getMessage());
            }
            results.add(item);
        }
        return results;
    }

    @Transactional
    public void deleteEnrollment(String studentId, String courseId, String term) {
        String sid = n(studentId);
        String cid = n(courseId);
        String tm  = n(term);
        if (sid.isEmpty() || cid.isEmpty() || tm.isEmpty()) return;
        // 使用大小写不敏感的原生删除，避免因大小写不一致删除不到
        enrollmentRepository.deleteIgnoreCase(sid, cid, tm);
    }

    @Transactional
    public void updateEnrollmentStatus(String studentId, String courseId, String term, String status) {
        String sid = n(studentId);
        String cid = n(courseId);
        String tm  = n(term);
        String st  = n(status);
        if (sid.isEmpty() || cid.isEmpty() || tm.isEmpty() || st.isEmpty()) return;

        // 使用原生 LIMIT 1 的大小写不敏感查询，避免 fetch first ? 语法问题
        enrollmentRepository.findOneIgnoreCaseNative(sid, cid, tm).ifPresent(e -> {
            e.setStatus(st);
            enrollmentRepository.save(e);
        });
    }

    /* ================= 矩阵批量（多个学生 × 多门课程） ================= */

    public List<EnrollMatrixResultItem> enrollMatrix(List<String> studentIds, List<String> courseIds, String term) {
        List<EnrollMatrixResultItem> results = new ArrayList<>();
        String t = n(term);
        for (String sidRaw : studentIds) {
            String sid = n(sidRaw);
            if (sid.isEmpty()) continue;
            for (String cidRaw : courseIds) {
                String cid = n(cidRaw);
                if (cid.isEmpty()) continue;

                EnrollMatrixResultItem item = new EnrollMatrixResultItem(sid, cid);
                try {
                    enrollStudent(sid, cid, t);
                    item.setSuccess(true);
                    item.setMessage("OK");
                } catch (Exception ex) {
                    item.setSuccess(false);
                    item.setMessage(ex.getMessage());
                }
                results.add(item);
            }
        }
        return results;
    }

    private String n(String s) { return s == null ? "" : s.trim(); }
}