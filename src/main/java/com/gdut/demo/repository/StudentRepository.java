package com.gdut.demo.repository;

import com.gdut.demo.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {

    // 单关键字模糊搜索（保留）
    List<Student> findByStudentIdContainingIgnoreCaseOrNameContainingIgnoreCaseOrDeptIdContainingIgnoreCase(
            String studentId, String name, String deptId
    );

    // 精确学号（大小写不敏感）
    List<Student> findByStudentIdIgnoreCase(String studentId);

    // 统计：按系号大小写不敏感计数（用于删除系时展示引用数）
    long countByDeptIdIgnoreCase(String deptId);

    // 原生SQL：按学号忽略大小写精确查找，使用 LIMIT 1（避免 FETCH FIRST ? ROWS ONLY）
    @Query(value = """
            select s.*
            from edu.student s
            where upper(s.student_id) = upper(:sid)
            limit 1
            """, nativeQuery = true)
    Optional<Student> findOneIgnoreCaseNative(@Param("sid") String studentId);

    // 组合过滤（原生 SQL，openGauss/PostgreSQL ILIKE 大小写不敏感）
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