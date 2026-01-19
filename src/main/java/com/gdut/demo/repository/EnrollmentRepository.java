package com.gdut.demo.repository;

import com.gdut.demo.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 查询某学生某学期某状态的选课记录
    List<Enrollment> findByStudentIdAndTermAndStatus(String studentId, String term, String status);

    // 判断某条选课是否已存在
    boolean existsByStudentIdAndCourseIdAndTerm(String studentId, String courseId, String term);

    // 按课程 + 学期 + 状态 查询选课名单
    List<Enrollment> findByCourseIdAndTermAndStatus(String courseId, String term, String status);

    // 按学生 + 学期 查询（不限定状态）
    List<Enrollment> findByStudentIdAndTerm(String studentId, String term);

    // 按课程 + 学期 查询（不限定状态）
    List<Enrollment> findByCourseIdAndTerm(String courseId, String term);

    // 复合条件定位单条
    Optional<Enrollment> findByStudentIdAndCourseIdAndTerm(String studentId, String courseId, String term);

    // 复合条件删除
    void deleteByStudentIdAndCourseIdAndTerm(String studentId, String courseId, String term);

    // 成绩登记表数据行（根据选课名单拼装课程+学生基本信息），用于 ReportsService.exportRegistrationCsv
    @Query(value = """
        select
          c.course_id    as courseId,
          c.name         as courseName,
          c.teacher_id   as teacherId,
          sf.name        as teacherName,
          c.hours        as hours,
          c.credits      as credits,
          c.schedule     as schedule,
          c.location     as location,
          c.exam_time    as examTime,
          e.term         as term,
          e.student_id   as studentId,
          st.name        as studentName,
          st.gender      as gender
        from edu.course c
        left join edu.staff sf on sf.staff_id = c.teacher_id
        join edu.enrollment e  on e.course_id = c.course_id
                              and (:term is null or e.term = :term)
                              and e.status = 'selected'
        join edu.student st    on st.student_id = e.student_id
        where (:courseId   is null or c.course_id  ILIKE concat('%', :courseId,  '%'))
          and (:courseName is null or c.name       ILIKE concat('%', :courseName,'%'))
          and (:teacher    is null or sf.name      ILIKE concat('%', :teacher,   '%'))
          and (:deptId     is null or sf.dept_id   ILIKE concat('%', :deptId,    '%'))
        order by c.course_id asc, e.student_id asc
    """, nativeQuery = true)
    List<RegistrationRow> registrationRows(String term, String courseId, String courseName, String teacher, String deptId);

    // 内嵌投影接口：供 ReportsService 直接使用 EnrollmentRepository.RegistrationRow
    interface RegistrationRow {
        String getCourseId();
        String getCourseName();
        String getTeacherId();
        String getTeacherName();
        Integer getHours();
        java.math.BigDecimal getCredits();
        String getSchedule();
        String getLocation();
        LocalDateTime getExamTime();
        String getTerm();
        String getStudentId();
        String getStudentName();
        String getGender();
    }
}