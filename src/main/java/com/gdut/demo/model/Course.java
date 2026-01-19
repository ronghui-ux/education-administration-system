package com.gdut.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 课程实体（映射 edu.course）
 * 列：course_id, name, teacher_id, hours, credits, schedule, location, exam_time
 */
@Entity
@Table(name = "course", schema = "edu")
public class Course {

    @Id
    @NotBlank(message = "课程号不能为空")
    @Size(max = 20, message = "课程号长度不能超过20")
    @Column(name = "course_id", length = 20, nullable = false)
    private String courseId;

    @NotBlank(message = "课程名不能为空")
    @Size(max = 200, message = "课程名长度不能超过200")
    @Column(name = "name", length = 200, nullable = false)
    private String name;

    @Size(max = 20, message = "教师号长度不能超过20")
    @Column(name = "teacher_id", length = 20)
    private String teacherId;

    @PositiveOrZero(message = "学时必须为非负")
    @Column(name = "hours")
    private Integer hours;

    @Column(name = "credits", precision = 4, scale = 2)
    private BigDecimal credits;

    @Size(max = 200, message = "上课时间长度不能超过200")
    @Column(name = "schedule", length = 200)
    private String schedule;

    @Size(max = 200, message = "地点长度不能超过200")
    @Column(name = "location", length = 200)
    private String location;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "exam_time")
    private LocalDateTime examTime;

    // getters & setters
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTeacherId() { return teacherId; }
    public void setTeacherId(String teacherId) { this.teacherId = teacherId; }
    public Integer getHours() { return hours; }
    public void setHours(Integer hours) { this.hours = hours; }
    public BigDecimal getCredits() { return credits; }
    public void setCredits(BigDecimal credits) { this.credits = credits; }
    public String getSchedule() { return schedule; }
    public void setSchedule(String schedule) { this.schedule = schedule; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getExamTime() { return examTime; }
    public void setExamTime(LocalDateTime examTime) { this.examTime = examTime; }
}