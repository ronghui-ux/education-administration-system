package com.gdut.demo.model;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "grade", schema = "edu")
public class Grade {

    @EmbeddedId
    private GradeId id;

    @Column(name = "usual_score", precision = 5, scale = 2)
    private BigDecimal usualScore;

    @Column(name = "exam_score", precision = 5, scale = 2)
    private BigDecimal examScore;

    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;

    public GradeId getId() { return id; }
    public void setId(GradeId id) { this.id = id; }

    public BigDecimal getUsualScore() { return usualScore; }
    public void setUsualScore(BigDecimal usualScore) { this.usualScore = usualScore; }

    public BigDecimal getExamScore() { return examScore; }
    public void setExamScore(BigDecimal examScore) { this.examScore = examScore; }

    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
}