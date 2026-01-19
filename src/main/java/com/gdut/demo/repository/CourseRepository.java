package com.gdut.demo.repository;

import com.gdut.demo.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CourseRepository extends JpaRepository<Course, String> {

    // 原生 SQL，使用 ILIKE 避免对列做 lower()
    @Query(value = """
        select c.*
        from edu.course c
        left join edu.staff s on s.staff_id = c.teacher_id
        where (:courseId is null or c.course_id ILIKE concat('%', :courseId, '%'))
          and (:name     is null or c.name      ILIKE concat('%', :name,     '%'))
          and (:teacher  is null or s.name      ILIKE concat('%', :teacher,  '%'))
        order by c.course_id asc
    """, nativeQuery = true)
    List<Course> searchByFilters(String courseId, String name, String teacher);
}