package com.gdut.demo.repository;

import com.gdut.demo.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // 查询某学生某学期某状态的选课记录（注意：区分大小写）
    List<Enrollment> findByStudentIdAndTermAndStatus(String studentId, String term, String status);

    // 判断某条选课是否已存在（派生 exists 在 openGauss 上可能生成 fetch first ? rows only，避免直接使用）
    boolean existsByStudentIdAndCourseIdAndTerm(String studentId, String courseId, String term);

    // 按课程 + 学期 + 状态 查询选课名单（区分大小写）
    List<Enrollment> findByCourseIdAndTermAndStatus(String courseId, String term, String status);

    // 按学生 + 学期 查询（区分大小写）
    List<Enrollment> findByStudentIdAndTerm(String studentId, String term);

    // 按课程 + 学期 查询（区分大小写）
    List<Enrollment> findByCourseIdAndTerm(String courseId, String term);

    // 复合条件定位单条（派生 Optional 也可能触发 fetch first ?，避免在服务里直接使用）
    Optional<Enrollment> findByStudentIdAndCourseIdAndTerm(String studentId, String courseId, String term);

    // 复合条件删除（区分大小写）
    void deleteByStudentIdAndCourseIdAndTerm(String studentId, String courseId, String term);

    /* ---------------- openGauss 兼容（忽略大小写 + 避免 fetch first ?）---------------- */

    // 原生 exists：大小写不敏感，避免 Hibernate 生成 fetch first ? rows only
    @Query(value = """
            select exists(
              select 1
              from edu.enrollment
              where upper(student_id) = upper(:sid)
                and upper(course_id)  = upper(:cid)
                and upper(term)       = upper(:term)
            )
            """, nativeQuery = true)
    boolean existsEnrollmentIgnoreCase(@Param("sid") String studentId,
                                       @Param("cid") String courseId,
                                       @Param("term") String term);

    // 原生单条查询：大小写不敏感，使用常量 LIMIT 1
    @Query(value = """
            select e.*
            from edu.enrollment e
            where upper(e.student_id) = upper(:sid)
              and upper(e.course_id)  = upper(:cid)
              and upper(e.term)       = upper(:term)
            limit 1
            """, nativeQuery = true)
    Optional<Enrollment> findOneIgnoreCaseNative(@Param("sid") String studentId,
                                                 @Param("cid") String courseId,
                                                 @Param("term") String term);

    // 原生列表查询：按学生 + 学期（大小写不敏感）
    @Query(value = """
            select e.*
            from edu.enrollment e
            where upper(e.student_id) = upper(:sid)
              and upper(e.term)       = upper(:term)
            order by e.course_id
            """, nativeQuery = true)
    List<Enrollment> findByStudentIdAndTermIgnoreCase(@Param("sid") String studentId,
                                                      @Param("term") String term);

    // 原生列表查询：按课程 + 学期（大小写不敏感）
    @Query(value = """
            select e.*
            from edu.enrollment e
            where upper(e.course_id) = upper(:cid)
              and upper(e.term)      = upper(:term)
            order by e.student_id
            """, nativeQuery = true)
    List<Enrollment> findByCourseIdAndTermIgnoreCase(@Param("cid") String courseId,
                                                     @Param("term") String term);

    // 仅 selected 状态，按课程 + 学期（大小写不敏感），供成绩录入页载入选课学生
    @Query(value = """
            select e.*
            from edu.enrollment e
            where upper(e.course_id) = upper(:cid)
              and upper(e.term)      = upper(:term)
              and lower(e.status)    = 'selected'
            order by e.student_id
            """, nativeQuery = true)
    List<Enrollment> findSelectedByCourseAndTermIgnoreCase(@Param("cid") String courseId,
                                                           @Param("term") String term);

    // 原生删除：大小写不敏感
    @Modifying
    @Query(value = """
            delete from edu.enrollment
            where upper(student_id) = upper(:sid)
              and upper(course_id)  = upper(:cid)
              and upper(term)       = upper(:term)
            """, nativeQuery = true)
    int deleteIgnoreCase(@Param("sid") String studentId,
                         @Param("cid") String courseId,
                         @Param("term") String term);

    /* ---------------- 新增：按课程统计选课引用（大小写不敏感 + 去除两端空格） ---------------- */
    @Query(value = "select count(*) from edu.enrollment where upper(btrim(course_id)) = upper(btrim(:cid))", nativeQuery = true)
    long countByCourseIdIgnoreCase(@Param("cid") String courseId);

    /* ---------------- 成绩登记表投影（保持原实现） ---------------- */

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