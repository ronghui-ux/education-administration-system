package com.gdut.demo.repository;

import com.gdut.demo.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, String>, JpaSpecificationExecutor<Student> {

    // 单关键字模糊搜索（保留）
    List<Student> findByStudentIdContainingIgnoreCaseOrNameContainingIgnoreCaseOrDeptIdContainingIgnoreCase(
            String studentId, String name, String deptId
    );

    // 精确学号（大小写不敏感）
    List<Student> findByStudentIdIgnoreCase(String studentId);

    // 组合过滤（原生 SQL）：任意条件可选，大小写不敏感，模糊匹配
    // openGauss/PostgreSQL 使用 ILIKE 实现不区分大小写的 like
    @Query(value = """
            select s.*
            from edu.student s
            where (:sid  is null or s.student_id ILIKE concat('%', :sid,  '%'))
              and (:name is null or s.name       ILIKE concat('%', :name, '%'))
              and (:dept is null or s.dept_id    ILIKE concat('%', :dept, '%'))
            order by s.student_id
            """, nativeQuery = true)
    List<Student> searchByFiltersNative(@Param("sid") String studentId,
                                        @Param("name") String name,
                                        @Param("dept") String deptId);
}