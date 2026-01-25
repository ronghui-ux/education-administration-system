package com.gdut.demo.repository;

import com.gdut.demo.model.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StaffRepository extends JpaRepository<Staff, String> {
    long countByDeptIdIgnoreCase(String deptId);
    // 关键字搜索：按职工号/姓名/系号模糊匹配（忽略大小写）
    List<Staff> findByStaffIdContainingIgnoreCaseOrNameContainingIgnoreCaseOrDeptIdContainingIgnoreCase(
            String staffId, String name, String deptId
    );
}