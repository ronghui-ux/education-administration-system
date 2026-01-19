package com.gdut.demo.dto;

import java.math.BigDecimal;

public class ScoreUpdateItem {
    private String studentId;
    private String courseId;
    private String term;
    private BigDecimal usualScore; // 平时成绩
    private BigDecimal examScore;  // 考试成绩

    public ScoreUpdateItem() {}

    public ScoreUpdateItem(String studentId, String courseId, String term, BigDecimal usualScore, BigDecimal examScore) {
        this.studentId = studentId;
        this.courseId = courseId;
        this.term = term;
        this.usualScore = usualScore;
        this.examScore = examScore;
    }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getCourseId() { return courseId; }
    public void setCourseId(String courseId) { this.courseId = courseId; }
    public String getTerm() { return term; }
    public void setTerm(String term) { this.term = term; }
    public BigDecimal getUsualScore() { return usualScore; }
    public void setUsualScore(BigDecimal usualScore) { this.usualScore = usualScore; }
    public BigDecimal getExamScore() { return examScore; }
    public void setExamScore(BigDecimal examScore) { this.examScore = examScore; }
}