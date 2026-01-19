package com.gdut.demo.repository;

import com.gdut.demo.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, String> {

    // 关键字搜索（按系号或系名，忽略大小写）
    List<Department> findByDeptIdContainingIgnoreCaseOrNameContainingIgnoreCase(String deptId, String name);
}