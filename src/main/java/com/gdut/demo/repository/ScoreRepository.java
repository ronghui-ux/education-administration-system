package com.gdut.demo.repository;

import com.gdut.demo.model.Score;
import com.gdut.demo.model.ScoreId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, ScoreId> {

    List<Score> findByIdCourseIdAndIdTerm(String courseId, String term);
    List<Score> findByIdStudentIdAndIdTerm(String studentId, String term);

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