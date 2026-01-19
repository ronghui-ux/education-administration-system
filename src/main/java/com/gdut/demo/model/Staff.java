package com.gdut.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 教职工信息：职工号、姓名、性别、出生时间、系号、职称、专业
 * ER图对应表：edu.staff
 */
@Entity
@Table(name = "staff", schema = "edu")
public class Staff {

    @Id
    @NotBlank(message = "职工号不能为空")
    @Size(max = 20, message = "职工号长度不能超过20")
    @Column(name = "staff_id", length = 20, nullable = false)
    private String staffId;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100")
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Size(max = 10, message = "性别长度不能超过10")
    @Column(name = "gender", length = 10)
    private String gender;

    // ER图为 timestamp(0)，这里采用 LocalDateTime 并用 datetime-local 输入
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "dob")
    private LocalDateTime dob;

    @NotBlank(message = "系号不能为空")
    @Size(max = 20, message = "系号长度不能超过20")
    @Column(name = "dept_id", length = 20, nullable = false)
    private String deptId;

    @Size(max = 50, message = "职称长度不能超过50")
    @Column(name = "title", length = 50)
    private String title;

    @Size(max = 100, message = "专业长度不能超过100")
    @Column(name = "major", length = 100)
    private String major;

    // 如需“教学方向”，请在数据库增加 direction 列后再添加映射：
    // @Column(name = "direction", length = 100)
    // private String direction;

    // getters & setters
    public String getStaffId() { return staffId; }
    public void setStaffId(String staffId) { this.staffId = staffId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDateTime getDob() { return dob; }
    public void setDob(LocalDateTime dob) { this.dob = dob; }
    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
}