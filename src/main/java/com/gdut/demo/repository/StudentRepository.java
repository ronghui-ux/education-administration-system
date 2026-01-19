package com.gdut.demo.repository;

import com.gdut.demo.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, String>, JpaSpecificationExecutor<Student> {

    // 关键字搜索（备用）：按学号/姓名/系号模糊匹配（忽略大小写）
    List<Student> findByStudentIdContainingIgnoreCaseOrNameContainingIgnoreCaseOrDeptIdContainingIgnoreCase(
            String studentId, String name, String deptId
    );
}