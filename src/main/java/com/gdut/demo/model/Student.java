package com.gdut.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "student", schema = "edu")
public class Student {

    @Id
    @Column(name = "student_id", length = 20)
    private String studentId;

    @NotBlank(message = "姓名不能为空")
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "gender", length = 10)
    private String gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "admission_score", precision = 5, scale = 2)
    private BigDecimal admissionScore;

    @Column(name = "dept_id", length = 20)
    private String deptId;

    // getters/setters
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public BigDecimal getAdmissionScore() { return admissionScore; }
    public void setAdmissionScore(BigDecimal admissionScore) { this.admissionScore = admissionScore; }

    public String getDeptId() { return deptId; }
    public void setDeptId(String deptId) { this.deptId = deptId; }
}