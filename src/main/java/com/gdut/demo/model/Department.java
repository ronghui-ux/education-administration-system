package com.gdut.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 系基本信息：系号、系名称、简介
 * 说明：intro 字段映射到数据库的 description 列（ER 图中的列名）
 */
@Entity
@Table(name = "department", schema = "edu")
public class Department {

    @Id
    @NotBlank(message = "系号不能为空")
    @Size(max = 20, message = "系号长度不能超过20")
    @Column(name = "dept_id", length = 20, nullable = false)
    private String deptId;

    @NotBlank(message = "系名称不能为空")
    @Size(max = 100, message = "系名称长度不能超过100")
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    // 映射到数据库列 description（text）
    @Column(name = "description")
    private String intro;

    // getters & setters
    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
}