package com.gdut.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class GradeId implements Serializable {

    @Column(name = "student_id", length = 20)
    private String studentId;

    @Column(name = "course_id", length = 20)
    private String courseId;

    @Column(name = "term", length = 20)
    private String term;

    public GradeId() {}

    public GradeId(String studentId, String courseId, String term) {
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
        if (!(o instanceof GradeId)) return false;
        GradeId gradeId = (GradeId) o;
        return Objects.equals(studentId, gradeId.studentId) &&
                Objects.equals(courseId, gradeId.courseId) &&
                Objects.equals(term, gradeId.term);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, courseId, term);
    }
}
