package com.gdut.demo.repository;

import com.gdut.demo.model.Score;
import com.gdut.demo.model.ScoreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ScoreRepository extends JpaRepository<Score, ScoreId> {

    // 保留派生查询（大小写敏感，旧代码可能使用）
    List<Score> findByIdCourseIdAndIdTerm(String courseId, String term);
    List<Score> findByIdStudentIdAndIdTerm(String studentId, String term);

    /* ============== 大小写不敏感 + 去除两端空格（openGauss 友好） ============== */

    // 按课程 + 学期
    @Query(value = """
        select g.*
        from edu.grade g
        where upper(btrim(g.course_id)) = upper(btrim(:cid))
          and upper(btrim(g.term))      = upper(btrim(:term))
        order by g.student_id
    """, nativeQuery = true)
    List<Score> findByCourseIdAndTermIgnoreCase(@Param("cid") String courseId,
                                                @Param("term") String term);

    // 按学生 + 学期
    @Query(value = """
        select g.*
        from edu.grade g
        where upper(btrim(g.student_id)) = upper(btrim(:sid))
          and upper(btrim(g.term))       = upper(btrim(:term))
        order by g.course_id
    """, nativeQuery = true)
    List<Score> findByStudentIdAndTermIgnoreCase(@Param("sid") String studentId,
                                                 @Param("term") String term);

    // 单条定位（避免 fetch first ? rows only；并做 btrim 处理）
    @Query(value = """
        select g.*
        from edu.grade g
        where upper(btrim(g.student_id)) = upper(btrim(:sid))
          and upper(btrim(g.course_id))  = upper(btrim(:cid))
          and upper(btrim(g.term))       = upper(btrim(:term))
        limit 1
    """, nativeQuery = true)
    Optional<Score> findOneIgnoreCase(@Param("sid") String studentId,
                                      @Param("cid") String courseId,
                                      @Param("term") String term);

    /* ============== 新增：按课程统计成绩引用（大小写不敏感 + 去除两端空格） ============== */
    @Query(value = "select count(*) from edu.grade where upper(btrim(course_id)) = upper(btrim(:cid))", nativeQuery = true)
    long countByCourseIdIgnoreCase(@Param("cid") String courseId);

    /* ============== 组合搜索与报表（保持原实现不变） ============== */

    @Query(value = """
      select
        g.student_id  as studentId,
        st.name       as studentName,
        st.dept_id    as deptId,
        g.course_id   as courseId,
        c.name        as courseName,
        s.name        as teacherName,
        g.term        as term,
        g.usual_score as usualScore,
        g.exam_score  as examScore,
        g.total_score as totalScore
      from edu.grade g
      join edu.student st on st.student_id = g.student_id
      join edu.course  c  on c.course_id   = g.course_id
      left join edu.staff s on s.staff_id  = c.teacher_id
      where (:studentId   is null or g.student_id  ILIKE concat('%', :studentId,   '%'))
        and (:studentName is null or st.name       ILIKE concat('%', :studentName, '%'))
        and (:deptId      is null or st.dept_id    ILIKE concat('%', :deptId,      '%'))
        and (:courseId    is null or g.course_id   ILIKE concat('%', :courseId,    '%'))
        and (:courseName  is null or c.name        ILIKE concat('%', :courseName,  '%'))
        and (:teacherName is null or s.name        ILIKE concat('%', :teacherName, '%'))
        and (:term        is null or g.term        ILIKE concat('%', :term,        '%'))
      order by g.course_id asc, g.total_score desc, g.student_id asc
    """, nativeQuery = true)
    List<ScoreSearchRowProjection> searchComposite(
            String studentId, String studentName, String deptId,
            String courseId, String courseName, String teacherName,
            String term
    );

    @Query(value = """
      select
        g.student_id  as studentId,
        st.name       as studentName,
        st.dept_id    as deptId,
        g.course_id   as CourseId,
        c.name        as courseName,
        s.name        as teacherName,
        g.term        as term,
        g.usual_score as usualScore,
        g.exam_score  as examScore,
        g.total_score as totalScore
      from edu.grade g
      join edu.student st on st.student_id = g.student_id
      join edu.course  c  on c.course_id   = g.course_id
      left join edu.staff s on s.staff_id  = c.teacher_id
      where (:term is null or g.term = :term)
        and (:courseId    is null or g.course_id   ILIKE concat('%', :courseId,    '%'))
        and (:courseName  is null or c.name        ILIKE concat('%', :courseName,  '%'))
        and (:teacherName is null or s.name        ILIKE concat('%', :teacherName, '%'))
        and (:deptId      is null or s.dept_id     ILIKE concat('%', :deptId,      '%'))
      order by g.course_id asc, g.total_score desc, g.student_id asc
    """, nativeQuery = true)
    List<GradesReportRow> gradesReportRows(String term, String courseId, String courseName, String teacherName, String deptId);

    interface ScoreSearchRowProjection {
        String getStudentId();
        String getStudentName();
        String getDeptId();
        String getCourseId();
        String getCourseName();
        String getTeacherName();
        String getTerm();
        BigDecimal getUsualScore();
        BigDecimal getExamScore();
        BigDecimal getTotalScore();
    }

    interface GradesReportRow {
        String getStudentId();
        String getStudentName();
        String getDeptId();
        String getCourseId();
        String getCourseName();
        String getTeacherName();
        String getTerm();
        BigDecimal getUsualScore();
        BigDecimal getExamScore();
        BigDecimal getTotalScore();
    }
}