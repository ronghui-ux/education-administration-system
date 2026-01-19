package com.gdut.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ScoreId implements Serializable {

    @Column(name = "student_id", nullable = false, length = 32)
    private String studentId;

    @Column(name = "course_id", nullable = false, length = 32)
    private String courseId;

    @Column(name = "term", nullable = false, length = 32)
    private String term;

    public ScoreId() {}

    public ScoreId(String studentId, String courseId, String term) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.term = term;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ScoreId)) return false;
        ScoreId that = (ScoreId) o;
        return Objects.equals(studentId, that.studentId)
                && Objects.equals(courseId, that.courseId)
                && Objects.equals(term, that.term);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, courseId, term);
    }
}