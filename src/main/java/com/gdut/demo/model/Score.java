package com.gdut.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Entity
@Table(name = "grade", schema = "edu")
public class Score {

    @EmbeddedId
    private ScoreId id;

    @DecimalMin(value = "0.00", message = "平时成绩不能小于0")
    @DecimalMax(value = "100.00", message = "平时成绩不能超过100")
    @Column(name = "usual_score", precision = 5, scale = 2)
    private BigDecimal usualScore;

    @DecimalMin(value = "0.00", message = "考试成绩不能小于0")
    @DecimalMax(value = "100.00", message = "考试成绩不能超过100")
    @Column(name = "exam_score", precision = 5, scale = 2)
    private BigDecimal examScore;

    @Column(name = "total_score", precision = 5, scale = 2)
    private BigDecimal totalScore;

    public Score() {}

    public Score(ScoreId id) { this.id = id; }

    public ScoreId getId() { return id; }
    public void setId(ScoreId id) { this.id = id; }
    public BigDecimal getUsualScore() { return usualScore; }
    public void setUsualScore(BigDecimal usualScore) { this.usualScore = usualScore; }
    public BigDecimal getExamScore() { return examScore; }
    public void setExamScore(BigDecimal examScore) { this.examScore = examScore; }
    public BigDecimal getTotalScore() { return totalScore; }
    public void setTotalScore(BigDecimal totalScore) { this.totalScore = totalScore; }
}