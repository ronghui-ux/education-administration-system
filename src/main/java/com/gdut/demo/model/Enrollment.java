package com.gdut.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import java.time.LocalDateTime;

@Entity
@Table(name = "enrollment", schema = "edu")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enroll_id")
    private Long enrollId;

    @Column(name = "student_id", length = 20)
    private String studentId;

    @Column(name = "course_id", length = 20)
    private String courseId;

    @Column(name = "term", length = 20)
    private String term;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Optional convenience relations (read-only mapping)
    // @ManyToOne
    // @JoinColumn(name = "student_id", insertable = false, updatable = false)
    // private Student student;
    //
    // @ManyToOne
    // @JoinColumn(name = "course_id", insertable = false, updatable = false)
    // private Course course;

    public Long getEnrollId() { return enrollId; }
    public void setEnrollId(Long enrollId) { this.enrollId = enrollId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }

    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}